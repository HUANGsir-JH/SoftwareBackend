package org.software.content.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.software.content.mapper.ContentMapper;
import org.software.content.mapper.ContentMediaMapper;
import org.software.content.mapper.ContentTagMapper;
import org.software.content.mapper.TagMapper;
import org.software.content.service.ContentService;
import org.software.feign.UserFeignClient;
import org.software.model.Response;
import org.software.model.constants.ContentConstants;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.constants.TagConstants;
import org.software.model.content.Content;
import org.software.model.content.ContentTag;
import org.software.model.content.FirstMedia;
import org.software.model.content.Tag;
import org.software.model.content.dto.ContentDTO;
import org.software.model.content.vo.ContentDetailVO;
import org.software.model.content.vo.ContentVO;
import org.software.model.exception.BusinessException;
import org.software.model.media.ContentMedia;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.user.User;
import org.software.model.user.UserV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容主表(Content)表服务实现类
 *
 * @author Ra1nbot
 * @since 2025-12-08 14:03:09
 */
@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content> implements ContentService {

    @Autowired
    private ContentMapper contentMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private ContentTagMapper contentTagMapper;
    @Autowired
    private ContentMediaMapper contentMediaMapper;
    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ContentDTO contentDTO) {
        // 转换DTO为实体
        Content content = BeanUtil.toBean(contentDTO, Content.class);
        
        // 设置用户ID（从当前登录用户获取）
        long userId = StpUtil.getLoginIdAsLong();
        content.setUserId(userId);
        
        // 如果status为null，设置默认状态
        if (content.getStatus() == null || content.getStatus().isEmpty()) {
            content.setStatus(ContentConstants.STATUS_PUBLISH);
        }
        
        // 如果isPublic为null，设置默认为公开
        if (content.getIsPublic() == null) {
            content.setIsPublic(1);
        }
        
        // 设置 cover_url（使用第一个媒体文件）
        if (contentDTO.getMedias() != null && contentDTO.getMedias().length > 0) {
            content.setCoverUrl(contentDTO.getMedias()[0]);
        }
        
        // 初始化计数器
        content.setLikeCount(0);
        content.setFavoriteCount(0);
        content.setCommentCount(0);
        
        // 1. 先插入 content 主表（MP 会自动生成 contentId 和填充时间）
        this.save(content);
        
        Long contentId = content.getContentId();
        
        // 2. 批量插入标签关联
        if (contentDTO.getTags() != null && contentDTO.getTags().length > 0) {
            // 去重后再验证
            Long[] uniqueTags = Arrays.stream(contentDTO.getTags())
                    .distinct()
                    .toArray(Long[]::new);

            QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
            tagQueryWrapper.in("tag_id", (Object[]) uniqueTags);
            Long existCount = tagMapper.selectCount(tagQueryWrapper);

            if (existCount != uniqueTags.length) {
                throw new BusinessException(HttpCodeEnum.INVALID_TAG);
            }

            contentMapper.batchInsertTags(contentId, uniqueTags);
        }
        
        // 3. 批量插入媒体文件
        if (contentDTO.getMedias() != null && contentDTO.getMedias().length > 0) {
            List<ContentMedia> mediaList = new ArrayList<>();
            for (int i = 0; i < contentDTO.getMedias().length; i++) {
                ContentMedia media = new ContentMedia();
                media.setContentId(contentId.intValue());
                media.setFileUrl(contentDTO.getMedias()[i]);
                // 根据 contentType 判断 media type
                if ("video".equals(contentDTO.getContentType())) {
                    media.setType("video");
                } else {
                    media.setType("image");
                }
                mediaList.add(media);
            }
            contentMapper.batchInsertMedias(contentId, mediaList);
        }
        
        return contentId;
    }

    @Override
    public PageResult pageContent(PageQuery pageQuery, Integer userId, String status) {
        Page<Content> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        QueryWrapper<Content> wrapper = new QueryWrapper<>();
        wrapper.eq("userId", userId)
                .eq("status", status == null ? ContentConstants.STATUS_PUBLISH : ContentConstants.STATUS_DRAFT);
        page = contentMapper.selectPage(page, wrapper);

        List<ContentVO> contentVOS = page.getRecords().stream()
                .map(content -> {
                    ContentVO vo = BeanUtil.toBean(content, ContentVO.class);
                    vo.getFirstMedia().setType(content.getContentType());
                    vo.getFirstMedia().setFileUrl(content.getCoverUrl());

                    return vo;
                }).toList();

        return PageResult.builder()
                .total(page.getTotal())
                .records(contentVOS)
                .pageNum(pageQuery.getPageNum())
                .pageSize(pageQuery.getPageSize())
                .build();
    }

    @Override
    public PageResult getAllContent(PageQuery pageQuery, Integer tag) {
        Page<Content> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        
        List<Long> contentIds = null;
        
        // 如果指定了 tag，先查询该 tag 关联的所有 contentId
        if (tag != null) {
            QueryWrapper<ContentTag> contentTagWrapper = new QueryWrapper<>();
            contentTagWrapper.eq("tag_id", tag)
                            .isNull("deleted_at");
            List<ContentTag> contentTags = contentTagMapper.selectList(contentTagWrapper);
            
            if (contentTags.isEmpty()) {
                // 如果该标签没有关联任何内容，返回空结果
                return PageResult.builder()
                        .total(0L)
                        .records(new ArrayList<>())
                        .pageNum(pageQuery.getPageNum())
                        .pageSize(pageQuery.getPageSize())
                        .build();
            }
            
            contentIds = contentTags.stream()
                    .map(ct -> ct.getContentId().longValue())
                    .collect(Collectors.toList());
        }
        
        // 构建查询条件
        QueryWrapper<Content> wrapper = new QueryWrapper<>();
        wrapper.eq("status", ContentConstants.STATUS_PUBLISH)  // 只查询已发布的
               .eq("is_public", 1)  // 只查询公开的
               .isNull("deleted_at")  // 未删除的
               .orderByDesc("created_at");  // 按创建时间降序
        
        // 如果有 tag 筛选，添加 contentId 过滤条件
        if (contentIds != null && !contentIds.isEmpty()) {
            wrapper.in("content_id", contentIds);
        }
        
        page = contentMapper.selectPage(page, wrapper);
        
        // 转换为 VO
        List<ContentVO> contentVOList = page.getRecords().stream()
                .map(content -> {
                    ContentVO vo = new ContentVO();
                    vo.setContentId(content.getContentId());
                    vo.setContentType(content.getContentType());
                    vo.setTitle(content.getTitle());
                    vo.setLikeCount(content.getLikeCount().longValue());
                    vo.setCreatedAt(content.getCreatedAt());
                    vo.setUpdatedAt(content.getUpdatedAt());
                    
                    // 设置 firstMedia
                    FirstMedia firstMedia = new FirstMedia();
                    firstMedia.setFileUrl(content.getCoverUrl());
                    firstMedia.setType(content.getContentType());
                    vo.setFirstMedia(firstMedia);
                    
                    // 关联查询用户信息（需要通过 OpenFeign 或本地查询）
                    Response response = userFeignClient.getUser(content.getUserId());
                    User user = (User) response.getData();
                    vo.setUser(BeanUtil.toBean(user, UserV.class));
                    
                    return vo;
                }).collect(Collectors.toList());

        return PageResult.builder()
                .total(page.getTotal())
                .records(contentVOList)
                .pageNum(pageQuery.getPageNum())
                .pageSize(pageQuery.getPageSize())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(ContentDTO contentDTO) {
        // 验证 contentId 是否存在
        if (contentDTO.getContentId() == null) {
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }
        
        long contentId = contentDTO.getContentId();
        
        // 查询原有内容
        Content existContent = this.getById(contentId);
        if (existContent == null) {
            throw new BusinessException(HttpCodeEnum.CONTENT_NOT_FOUND);
        }
        
        // 验证权限：只能修改自己的内容
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (!existContent.getUserId().equals(currentUserId)) {
            throw new BusinessException(HttpCodeEnum.NO_PERMISSION);
        }
        
        // 1. 更新 content 主表
        Content content = BeanUtil.toBean(contentDTO, Content.class);
        content.setContentId(contentId);
        content.setUserId(currentUserId);  // 保持原有用户ID
        content.setUpdatedAt(new Date());
        
        // 如果 status 为 null，保持原状态
        if (content.getStatus() == null || content.getStatus().isEmpty()) {
            content.setStatus(existContent.getStatus());
        }
        
        // 如果 isPublic 为 null，保持原设置
        if (content.getIsPublic() == null) {
            content.setIsPublic(existContent.getIsPublic());
        }
        
        // 更新 cover_url（使用第一个媒体文件）
        if (contentDTO.getMedias() != null && contentDTO.getMedias().length > 0) {
            content.setCoverUrl(contentDTO.getMedias()[0]);
        } else {
            // 如果没有提供新的 medias，保持原有 coverUrl
            content.setCoverUrl(existContent.getCoverUrl());
        }
        
        // 保持原有的计数器
        content.setLikeCount(existContent.getLikeCount());
        content.setFavoriteCount(existContent.getFavoriteCount());
        content.setCommentCount(existContent.getCommentCount());
        content.setCreatedAt(existContent.getCreatedAt());
        
        this.updateById(content);
        
        // 2. 处理标签关联
        if (contentDTO.getTags() != null) {
            // 删除旧的标签关联（软删除）
            contentMapper.deleteTagsByContentId(contentId);
            
            // 插入新的标签关联
            if (contentDTO.getTags().length > 0) {
                // 去重后再验证
                Long[] uniqueTags = Arrays.stream(contentDTO.getTags())
                        .distinct()
                        .toArray(Long[]::new);

                QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
                tagQueryWrapper.in("tag_id", (Object[]) uniqueTags)
                              .isNull("deleted_at");
                Long existCount = tagMapper.selectCount(tagQueryWrapper);

                if (existCount != uniqueTags.length) {
                    throw new BusinessException(HttpCodeEnum.INVALID_TAG);
                }

                contentMapper.batchInsertTags(contentId, uniqueTags);
            }
        }
        
        // 3. 处理媒体文件
        if (contentDTO.getMedias() != null) {
            // 删除旧的媒体文件（软删除）
            contentMapper.deleteMediasByContentId(contentId);
            
            // 插入新的媒体文件
            if (contentDTO.getMedias().length > 0) {
                List<ContentMedia> mediaList = new ArrayList<>();
                for (int i = 0; i < contentDTO.getMedias().length; i++) {
                    ContentMedia media = new ContentMedia();
                    media.setContentId((int) contentId);
                    media.setFileUrl(contentDTO.getMedias()[i]);
                    if ("video".equals(contentDTO.getContentType())) {
                        media.setType("video");
                    } else {
                        media.setType("image");
                    }
                    mediaList.add(media);
                }
                contentMapper.batchInsertMedias(contentId, mediaList);
            }
        }
    }

    @Override
    public ContentDetailVO viewContent(Integer contentId) {
        Content content = this.getById(contentId);
        ContentDetailVO contentDetailVO = BeanUtil.toBean(content, ContentDetailVO.class);

        Response response = userFeignClient.getUser(content.getUserId());
        User user = (User) response.getData();
        UserV userV = BeanUtil.toBean(user, UserV.class);
        contentDetailVO.setUser(userV);

        QueryWrapper<Tag> tagQueryWrapper = new QueryWrapper<>();
        tagQueryWrapper.eq("content_id", contentId)
                .ne("is_active", TagConstants.ACTIVE);
        List<Tag> tags = tagMapper.selectList(tagQueryWrapper);
        contentDetailVO.setTags(tags);

        QueryWrapper<ContentMedia> mediaQueryWrapper = new QueryWrapper<>();
        mediaQueryWrapper.eq("content_id", contentId);
        List<ContentMedia> medias = contentMediaMapper.selectList(mediaQueryWrapper);
        contentDetailVO.setMedias(medias);

        return contentDetailVO;
    }
}

