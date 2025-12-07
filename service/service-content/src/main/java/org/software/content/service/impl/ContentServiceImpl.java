package org.software.content.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.software.content.dto.ContentDetailDto;
import org.software.content.dto.UserContentDataVO;
import org.software.content.dto.UserSimpleDto;
import org.software.content.mapper.*;
import org.software.content.service.ContentService;
import org.software.model.constants.ContentConstants;
import org.software.model.content.Content;
import org.software.model.content.ContentD;
import org.software.model.content.media.ContentMedia;
import org.software.model.content.post.PostD;
import org.software.model.content.post.PostE;
import org.software.model.content.post.PostPage;
import org.software.model.content.tag.ContentTag;
import org.software.model.content.tag.Tag;
import org.software.model.interaction.ContentLikeFavorite;
import org.software.model.interaction.InteractionRecord;
import org.software.model.page.PageQuery;
import org.software.model.user.User;
import org.software.user.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容主表(Content)表服务实现类
 *
 * @author Ra1nbot
 * @since 2025-11-11 09:40:54
 */
@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content> implements ContentService {

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private ContentMediaMapper contentMediaMapper;

    @Autowired
    private ContentTagMapper contentTagMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private ContentLikeFavoriteMapper contentLikeFavoriteMapper;

    @Autowired
    private InteractionRecordMapper interactionRecordMapper;

    @Autowired
    private UserMapper userMapper;


    @Override
    public Long create(ContentD contentD) {
        // 创建 帖子
        Content content=BeanUtil.toBean(contentD, Content.class);


        content.setContentType(contentD.getContentType());
        content.setTitle(contentD.getTitle());
        content.setDescription(contentD.getDescription());
        content.setIsPublic(contentD.getIsPublic() == null ? 1 : contentD.getIsPublic());
        content.setStatus(contentD.getStatus());




        Integer userId = StpUtil.getLoginIdAsInt();



        content.setLikeCount(0);
        content.setFavoriteCount(0);
        content.setCommentCount(0);


        //写入media
        Long contentId = content.getContentId();

        for (String url : content.getMedias()) {
            ContentMedia media = new ContentMedia();
            media.setContentId(Math.toIntExact(contentId));
            media.setFileUrl(url);
            contentMediaMapper.insert(media);
        }


        //写入标签 针对详情
        for (Tag tag : content.getTags()) {
            ContentTag contentTag = new ContentTag();
            contentTag.setContentId(Math.toIntExact(contentId));
            contentTag.setTagId(Math.toIntExact(tag.getTagId()));
            contentTagMapper.insert(contentTag);
        }


        // 保存到数据库
        boolean success = this.save(content);

        if (success) {
            // TODO: 送入任务队列进行后续处理（如审核、推送等）
            return content.getContentId();
        } else {
            throw new RuntimeException("帖子创建失败");
        }


    }


    @Override
    public PostPage<PostE> getMyContent(PageQuery query, Long userId) {
        Page<Content> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Content::getUserId, userId);
        if (query.getStartTime() != null) {
            wrapper.ge(Content::getCreatedAt, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(Content::getCreatedAt, query.getEndTime());
        }
        wrapper.orderByDesc(Content::getCreatedAt);

        Page<Content> contentPage = contentMapper.selectPage(page, wrapper);

        List<PostE> records = contentPage.getRecords().stream().map(content -> {
            PostE post = new PostE();
            post.setContentId(Math.toIntExact(content.getContentId()));
            post.setUserId(content.getUserId());
            post.setContentType(content.getContentType());
            post.setTitle(content.getTitle());
            post.setCreatedAt(content.getCreatedAt());
            post.setUpdatedAt(content.getUpdatedAt());
            post.setDeletedAt(content.getDeletedAt());

            // 填充首条媒体
            List<ContentMedia> medias = contentMediaMapper.selectList(
                    new LambdaQueryWrapper<ContentMedia>()
                            .eq(ContentMedia::getContentId, content.getContentId())
                            .orderByAsc(ContentMedia::getMediaId)
            );
            if (!medias.isEmpty()) {
                post.setFirstMedia(medias.get(0));
            }

            // 从 Content 冗余字段直接填充点赞数
            post.setLikeCount(content.getLikeCount());

            return post;
        }).toList();

        return new PostPage<PostE>(
                (int) contentPage.getTotal(),
                query.getPageNum(),
                query.getPageSize(),
                records
        );
    }

    //TODO:部分ai生成
    @Override
    public PostPage<ContentDetailDto> getAllContent(PageQuery query, Long userId) {

        Page<Content> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();

        // 时间筛选
        if (query.getStartTime() != null) {
            wrapper.ge(Content::getCreatedAt, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(Content::getCreatedAt, query.getEndTime());
        }

        // 按时间倒序
        wrapper.orderByDesc(Content::getCreatedAt);

        // 分页查 content
        Page<Content> contentPage = contentMapper.selectPage(page, wrapper);

        // 将 Content -> ContentDetailDto
        List<ContentDetailDto> dtoList = contentPage.getRecords().stream().map(content -> {

            ContentDetailDto dto = new ContentDetailDto();
            BeanUtils.copyProperties(content, dto);


            User user = userMapper.selectById(content.getUserId());
            if (user != null) {
                dto.setUser(new UserSimpleDto(
                        user.getUserId(),
                        user.getUsername(),
                        user.getAvatar(),
                        user.getNickname()
                ));
            }


            List<ContentMedia> medias = contentMediaMapper.selectList(
                    new LambdaQueryWrapper<ContentMedia>()
                            .eq(ContentMedia::getContentId, content.getContentId())
                            .orderByAsc(ContentMedia::getMediaId)
            );
            dto.setMedias(medias);


            List<ContentTag> ctList = contentTagMapper.selectList(
                    new LambdaQueryWrapper<ContentTag>()
                            .eq(ContentTag::getContentId, content.getContentId())
            );

            List<Tag> tags = ctList.stream()
                    .map(ct -> tagMapper.selectById(ct.getTagId()))
                    .collect(Collectors.toList());

            dto.setTags(tags);

            return dto;
        }).collect(Collectors.toList());


        PostPage<ContentDetailDto> result = new PostPage<>();
        result.setTotal((int) contentPage.getTotal());
        result.setPageNum((int) contentPage.getCurrent());
        result.setPageSize((int) contentPage.getSize());
        result.setRecords(dtoList);

        return result;
    }


    //TODO：部分ai生成，待斟酌
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Content content) {

        Integer contentId = Math.toIntExact(content.getContentId());

        Content toUpdate = new Content();
        toUpdate.setContentId(content.getContentId());
        toUpdate.setTitle(content.getTitle());
        toUpdate.setDescription(content.getDescription());
        toUpdate.setIsPublic(content.getIsPublic());
        toUpdate.setUpdatedAt(new Date());

        contentMapper.updateById(toUpdate);


        List<ContentMedia> dbMedias = contentMediaMapper.selectList(
                new LambdaQueryWrapper<ContentMedia>()
                        .eq(ContentMedia::getContentId, contentId)
        );

        List<String> dbUrls = dbMedias.stream()
                .map(ContentMedia::getFileUrl)
                .toList();

        List<String> newUrls = content.getMedias() != null ? content.getMedias() : List.of();


        List<String> toDelete = dbUrls.stream()
                .filter(url -> !newUrls.contains(url))
                .collect(Collectors.toList());

        if (!toDelete.isEmpty()) {
            contentMediaMapper.delete(
                    new LambdaQueryWrapper<ContentMedia>()
                            .eq(ContentMedia::getContentId, contentId)
                            .in(ContentMedia::getFileUrl, toDelete)
            );
        }


        List<String> toInsert = newUrls.stream()
                .filter(url -> !dbUrls.contains(url))
                .collect(Collectors.toList());

        for (String url : toInsert) {
            ContentMedia media = new ContentMedia();
            media.setContentId(contentId);
            media.setFileUrl(url);
            contentMediaMapper.insert(media);
        }



        contentTagMapper.delete(
                new LambdaQueryWrapper<ContentTag>()
                        .eq(ContentTag::getContentId, contentId)
        );


        if (content.getTags() == null || content.getTags().length == 0) {
            return;
        }


        for (Tag tag : content.getTags()) {

            Integer tagId;


            if (tag.getTagId() != null) {
                tagId = tag.getTagId();
            }

            else {

                Tag dbTag = tagMapper.selectOne(
                        new LambdaQueryWrapper<Tag>()
                                .eq(Tag::getTagName, tag.getTagName())
                                .last("limit 1")
                );

                if (dbTag != null) {
                    tagId = dbTag.getTagId();
                } else {

                    Tag newTag = new Tag();
                    newTag.setTagName(tag.getTagName());
                    newTag.setIsActive(1);
                    newTag.setCreatedAt(new Date());
                    newTag.setUpdatedAt(new Date());

                    tagMapper.insert(newTag);
                    tagId = newTag.getTagId();
                }
            }


            ContentTag contentTag = new ContentTag();
            contentTag.setContentId(contentId);
            contentTag.setTagId(tagId);
            contentTag.setCreatedAt(new Date());
            contentTag.setUpdatedAt(new Date());

            contentTagMapper.insert(contentTag);
        }

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContent(Integer contentId) {

        // 1. 软删除 content 表
        Content content = new Content();
        content.setContentId(Long.valueOf(contentId));
        content.setDeletedAt(new Date());
        contentMapper.updateById(content);

        // 2. 删除媒体
        contentMediaMapper.delete(
                new LambdaQueryWrapper<ContentMedia>()
                        .eq(ContentMedia::getContentId, contentId)
        );

        // 3. 删除标签关联
        contentTagMapper.delete(
                new LambdaQueryWrapper<ContentTag>()
                        .eq(ContentTag::getContentId, contentId)
        );

        // 4. 删除点赞/收藏关联
        contentLikeFavoriteMapper.delete(
                new LambdaQueryWrapper<ContentLikeFavorite>()
                        .eq(ContentLikeFavorite::getContentId, contentId)
        );
    }


    @Override
    public ContentDetailDto viewContent(Integer contentId) {
        // 1. 查询内容
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            return null;
        }

        // 2. 查询用户
        User user = userMapper.selectById(content.getUserId());
        UserSimpleDto userDto = null;
        if (user != null) {
            userDto = new UserSimpleDto(
                    user.getUserId(),
                    user.getUsername(),
                    user.getAvatar(),
                    user.getNickname()
            );
        }

        // 3. 查询媒体（严格按 contentId）
        List<ContentMedia> medias = contentMediaMapper.selectList(
                new LambdaQueryWrapper<ContentMedia>()
                        .eq(ContentMedia::getContentId, contentId)
                        .orderByAsc(ContentMedia::getMediaId)
        );

        // 4. 查询标签
        List<ContentTag> ctList = contentTagMapper.selectList(
                new LambdaQueryWrapper<ContentTag>()
                        .eq(ContentTag::getContentId, contentId)
        );

        List<Integer> tagIds = ctList.stream()
                .map(ContentTag::getTagId)
                .collect(Collectors.toList());

        List<Tag> tags = tagIds.isEmpty()
                ? new ArrayList<>()
                : tagMapper.selectList(
                new LambdaQueryWrapper<Tag>().in(Tag::getTagId, tagIds)
        );

        // 5. 组装返回 DTO
        ContentDetailDto dto = new ContentDetailDto();

        // 将 content 基础字段复制进去
        BeanUtils.copyProperties(content, dto);

        // 保持 JSON 返回字段名一致
        dto.setUser(userDto);
        dto.setMedias(medias);
        dto.setTags(tags);

        dto.setDeletedAt(new Date());

        return dto;
    }

    @Override
    public PostPage<ContentDetailDto> getAllContentForAdmin(PageQuery pageQuery,
                                          String status,
                                          String contentType,
                                          String startTime,
                                          String title) {

        Page<Content> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();

        if (title != null && !title.isEmpty()) {
            wrapper.like(Content::getTitle, title);
        }
        if (contentType != null && !contentType.isEmpty()) {
            wrapper.eq(Content::getContentType, contentType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Content::getStatus, status);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(Content::getCreatedAt, LocalDateTime.parse(startTime));
        }


        Page<Content> contentPage = contentMapper.selectPage(page, wrapper);


        List<ContentDetailDto> dtoList = contentPage.getRecords().stream().map(content -> {

            ContentDetailDto dto = new ContentDetailDto();
            BeanUtils.copyProperties(content, dto);


            User user = userMapper.selectById(content.getUserId());
            if (user != null) {
                dto.setUser(new UserSimpleDto(
                        user.getUserId(),
                        user.getUsername(),
                        user.getAvatar(),
                        user.getNickname()
                ));
            }


            List<ContentMedia> medias = contentMediaMapper.selectList(
                    new LambdaQueryWrapper<ContentMedia>()
                            .eq(ContentMedia::getContentId, content.getContentId())
                            .orderByAsc(ContentMedia::getMediaId)
            );
            dto.setMedias(medias);


            List<ContentTag> ctList = contentTagMapper.selectList(
                    new LambdaQueryWrapper<ContentTag>()
                            .eq(ContentTag::getContentId, content.getContentId())
            );
            List<Tag> tags = ctList.stream()
                    .map(ct -> tagMapper.selectById(ct.getTagId()))
                    .collect(Collectors.toList());

            dto.setTags(tags);

            return dto;

        }).collect(Collectors.toList());

        // 3) 构造分页返回
        PostPage<ContentDetailDto> result = new PostPage<>();
        result.setTotal((int) contentPage.getTotal());
        result.setPageNum((int) contentPage.getCurrent());
        result.setPageSize((int) contentPage.getSize());
        result.setRecords(dtoList);

        return result;
    }


    @Override
    public void approveContent(Integer contentId) {


        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new RuntimeException("内容不存在");
        }

        Content update = new Content();
        update.setContentId(Long.valueOf(contentId));
        update.setStatus("publish");
        update.setUpdatedAt(new Date());

        int rows = contentMapper.updateById(update);

        if (rows == 0) {
            throw new RuntimeException("审核失败，数据库未更新");
        }
    }

    @Override
    public boolean banContent(Integer contentId){
        Content update = new Content();
        update.setStatus("banned");   // 禁用
        update.setUpdatedAt(new Date()); // 更新时间（你说这个字段在 content 内）

        int rows = contentMapper.update(update,
                new LambdaQueryWrapper<Content>()
                        .eq(Content::getContentId, contentId)
        );

        return rows > 0;
    }


    @Override
    public UserContentDataVO getContentData(Integer contentId){

        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new RuntimeException("内容不存在");
        }

        Integer totalLike = content.getLikeCount();
        Integer totalFavorite = content.getFavoriteCount();

        return new UserContentDataVO(
                totalLike == null ? 0 : totalLike,
                totalFavorite == null ? 0 : totalFavorite
        );
    }


}

