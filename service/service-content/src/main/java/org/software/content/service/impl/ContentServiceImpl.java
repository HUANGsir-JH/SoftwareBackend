package org.software.content.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.software.content.dto.UserContentDataVO;
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

    //应该传入详情？？也就是postd 可以上传url和标签，但content没这些
    @Override
    public Long create(ContentD contentD) {
        // 创建 帖子 实体？content？content没有标签和media
        Content content=BeanUtil.toBean(contentD, Content.class);
        PostD post = BeanUtil.toBean(contentD, PostD.class);

        content.setContentType(contentD.getContentType());
        content.setTitle(contentD.getTitle());
        content.setDescription(contentD.getDescription());
        content.setIsPublic(contentD.getIsPublic() == null ? 1 : contentD.getIsPublic());
        content.setStatus(contentD.getStatus());
        /*
        post.setContentType(contentD.getContentType());
        post.setTitle(contentD.getTitle());
        post.setDescription(contentD.getDescription());
        post.setIsPublic(contentD.getIsPublic() == null ? 1 : contentD.getIsPublic());
        post.setStatus(contentD.getStatus());
        */


        // TODO: 从 token 中获取当前登录用户的 userId
        Long userId = StpUtil.getLoginIdAsLong();


        /*帖子详情内没有点赞收藏评论数
        //点赞/收藏/评论全部为 0
        post.setLikeCount(0);
        post.setFavoriteCount(0);
        post.setCommentCount(0);*/

        /*
        //写入media 针对详情
        Long contentId = Long.valueOf(post.getContentId());
        for (int i = 0; i < post.getMedias().size(); i++) {
            ContentMedia media = new ContentMedia();
            media.setContentId(Math.toIntExact(contentId));
            media.setFileUrl(post.getMedias().get(i).getFileUrl());
            contentMediaMapper.insert(media);
        }

        //写入标签 针对详情
        for (Tag tag : post.getTags()) {
            ContentTag contentTag = new ContentTag();
            contentTag.setContentId(Math.toIntExact(contentId));
            contentTag.setTagId(Math.toIntExact(tag.getTagId()));
            contentTagMapper.insert(contentTag);
        }
        */

        // 保存到数据库
        boolean success = this.save(content);

        if (success) {
            // TODO: 送入任务队列进行后续处理（如审核、推送等）
            return Long.valueOf(post.getContentId());
        } else {
            throw new RuntimeException("帖子创建失败");
        }


    }


    @Override
    public PostPage getMyContent(PageQuery query, Long userId) {
        Page<PostE> page = new Page<>(query.getPageNum(), query.getPageSize());

        LambdaQueryWrapper<PostE> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostE::getUserId, userId);
        //这个status怎么传？
//        if ("draft".equals(status)) {
//            wrapper.eq(PostE::getStatus, "draft");
//        } else {
//            wrapper.ne(PostE::getStatus, "draft");
//        }
        //时间过滤
        if (query.getStartTime() != null) {
            wrapper.ge(PostE::getCreatedAt, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(PostE::getCreatedAt, query.getEndTime());
        }

        wrapper.orderByDesc(PostE::getCreatedAt);

        Page<PostE> result = postMapper.selectSimplePage(page, wrapper);
        return new PostPage(
                (int) result.getTotal(),
                query.getPageNum(),
                query.getPageSize(),
                result.getRecords()
        );

    }
    @Override
    public PostPage getAllFriendContent(PageQuery query, Long userId){
        Page<PostE> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<PostE> wrapper = new LambdaQueryWrapper<>();
        wrapper.inSql(PostE::getUserId,
                "SELECT friend_id FROM friends WHERE user_id = " + userId + " AND status = 'accepted'");
        if (query.getStartTime() != null) {
            wrapper.ge(PostE::getCreatedAt, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(PostE::getCreatedAt, query.getEndTime());
        }
        wrapper.orderByDesc(PostE::getCreatedAt);

        Page<PostE> result = postMapper.selectFriendPage(page, wrapper);

        return new PostPage(
                (int) result.getTotal(),
                query.getPageNum(),
                query.getPageSize(),
                result.getRecords()
        );
    }

    //到底是post还是content，也可以更新media和tag，但content里没有这些
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Content content){
        Integer contentId = Math.toIntExact(content.getContentId());
        PostD post = new PostD();
        post.setContentId(contentId);
        post.setTitle(content.getTitle());
        post.setDescription(content.getDescription());
        post.setIsPublic(content.getIsPublic());
        post.setUpdatedAt(new Date());

        //这里不太会写，用ai生成的，待斟酌
        /*
        List<ContentMedia> dbMedias = contentMediaMapper.selectList(
                new LambdaQueryWrapper<ContentMedia>().eq(ContentMedia::getContentId, contentId)
        );

        List<Integer> dbIds = dbMedias.stream()
                .map(ContentMedia::getId)
                .collect(Collectors.toList());

        List<Integer> newIds = dto.getMedias().stream()
                .filter(m -> m.getId() != null)
                .map(ContentMedia::getId)
                .collect(Collectors.toList());

        // 删除数据库中已存在但未在新列表里的
        List<Integer> toDelete = dbIds.stream()
                .filter(id -> !newIds.contains(id))
                .collect(Collectors.toList());
        if (!toDelete.isEmpty()) {
            mediaMapper.deleteBatchIds(toDelete);
        }

        // 插入新的媒体
        List<ContentMedia> toInsert = content.getMedias().stream()
                .filter(m -> m.getId() == null)
                .peek(m -> m.setContentId(contentId))
                .collect(Collectors.toList());
        if (!toInsert.isEmpty()) {
        mediaMapper.insertBatch(toInsert); // 自己写 batch insert 方法
        }

        // 更新 content 表封面字段 cover_url
        if (!content.getMedias().isEmpty()) {
            postMapper.updateCoverUrl(contentId, dto.getMedias().get(0).getFileUrl());
         }
        */

        //只有post详情才有tag，用content无法更新？
        /*
        tagMapper.delete(new LambdaQueryWrapper<Tag>().eq(Tag::getContentId, contentId));
        // 插入新标签
        if (content.getTags() != null && !content.getTags().isEmpty()) {
            content.getTags().forEach(tag -> tag.setContentId(content.getContentId()));
            tagMapper.insertBatch(dto.getTags());
        }
         */


        //PostMapper中需要补充updateCoverUrl、updateById
        //ContentMediaMapper中需要补充selectList，deleteBatchIds、insertBatch
        //TagMapper中需要补充delete（按照固定条件）、insertBatch

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteContent(Integer contentId) {

        PostD post = new PostD();
        post.setContentId(contentId);
        post.setDeletedAt(new Date());
        postMapper.updateById(post);


        contentMediaMapper.delete(new LambdaQueryWrapper<ContentMedia>()
                .eq(ContentMedia::getContentId, contentId));


        tagMapper.delete(new LambdaQueryWrapper<ContentMedia>()
                .eq(ContentTag::getContentId, contentId));


        contentLikeFavoriteMapper.delete(new LambdaQueryWrapper<ContentLikeFavorite>()
                .eq(ContentLikeFavorite::getContentId, contentId));
    }

    public PostD viewContent(Integer contentId){
        PostD post = (PostD) postMapper.selectById(contentId);
        if (post == null || post.getDeletedAt() != null) {
            return null;
        }
        List<ContentMedia> medias = contentMediaMapper.selectList(
                new LambdaQueryWrapper<ContentMedia>()
                        .eq(ContentMedia::getContentId, contentId)
                        .orderByAsc(ContentMedia::getMediaId)
        );
        List<ContentTag> tags = contentTagMapper.selectList(
                new LambdaQueryWrapper<ContentTag>()
                        .eq(ContentTag::getContentId, contentId)
        );
        List<Integer> tagIds = tags.stream()
                .map(ContentTag::getTagId)
                .collect(Collectors.toList());

        List<Tag> t = new ArrayList<>();
        if (!tagIds.isEmpty()) {
            t = tagMapper.selectList(
                    new LambdaQueryWrapper<Tag>()
                            .in(Tag::getTagId, tagIds)
            );
        }



        PostD d = new PostD();
        BeanUtils.copyProperties(post, d);
        d.setMedias(medias);
        d.setTags(t);

        //postd中没有这些
        /*
        // 点赞/收藏/评论数直接用冗余字段
        d.setLikeCount(post.getLikeCount());
        d.setFavoriteCount(post.getFavoriteCount());
        d.setCommentCount(post.getCommentCount());
         */
        return d;
    }
    public PostPage getAllContentForAdmin(PageQuery pageQuery, String status, String contentType,String startTime,String title) {
        Page<PostD> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());

        LambdaQueryWrapper<PostE> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(PostD::getTitle, title);
        }
        if (contentType != null && !contentType.isEmpty()) {
            wrapper.eq(PostD::getContentType, contentType);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PostD::getStatus, status);
        }
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(PostD::getCreatedAt, LocalDateTime.parse(startTime));
        }
        Page<PostD> postPage = (Page<PostD>) postMapper.selectPage(page, wrapper);
        List<PostD> records = postPage.getRecords().stream().map(post -> {
            List<ContentMedia> medias = contentMediaMapper.selectList(
                    new LambdaQueryWrapper<ContentMedia>()
                            .eq(ContentMedia::getContentId, post.getContentId())
                            .orderByAsc(ContentMedia::getMediaId)
            );
            post.setMedias(medias);

            // 查询标签列表
            List<ContentTag> contentTags = contentTagMapper.selectList(
                    new LambdaQueryWrapper<ContentTag>()
                            .eq(ContentTag::getContentId, post.getContentId())
            );

            List<Tag> tags = contentTags.stream().map(ct -> {
                Tag tag = tagMapper.selectById(ct.getTagId());
                return tag;
            }).collect(Collectors.toList());
            post.setTags(tags);
            return post;
        }).collect(Collectors.toList());
        PostPage result = new PostPage();
        result.setTotal((int) postPage.getTotal());
        result.setPageNum((int) postPage.getCurrent());
        result.setPageSize((int) postPage.getSize());
        //postpage里是poste类型，无法放返回数据要求的media和tag列表
        //result.setRecords(records);

        return result;
    }
    public void approveContent(Integer contentId){
        PostD post = (PostD) postMapper.selectById(contentId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }

        // 修改状态为 publish
        PostD update = new PostD();
        update.setContentId(contentId);
        update.setStatus("publish");
        Date now = new Date();
        update.setUpdatedAt(now);

        int rows = postMapper.updateById(update);

        if (rows == 0) {
            throw new RuntimeException("审核失败，数据库未更新");
        }
    }
    public boolean banContent(Integer contentId){
        PostD update = new PostD();
        update.setStatus("banned");
        update.setUpdatedAt(new Date());

        int rows = postMapper.update(update,
                new LambdaQueryWrapper<PostD>()
                        .eq(PostD::getContentId, contentId)
        );

        return rows > 0;
    }

    public UserContentDataVO getContentData(Integer contentId){
        // 统计点赞
        Integer totalLike = interactionRecordMapper.selectCount(
                new LambdaQueryWrapper<InteractionRecord>()
                        .inSql(InteractionRecord::getContentId,
                                "SELECT content_id FROM content WHERE user_id = " + contentId)
                        .eq(InteractionRecord::getType, "like")
        );

        // 统计收藏
        Integer totalFavorite = Math.toIntExact(interactionRecordMapper.selectCount(
                new LambdaQueryWrapper<InteractionRecord>()
                        .inSql(InteractionRecord::getContentId,
                                "SELECT content_id FROM content WHERE user_id = " + contentId)
                        .eq(InteractionRecord::getType, "collect")
        ));
        return new UserContentDataVO(
                totalLike == null ? 0 : totalLike,
                totalFavorite == null ? 0 : totalFavorite
        );
    }


}

