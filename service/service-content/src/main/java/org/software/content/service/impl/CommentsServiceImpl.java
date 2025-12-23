package org.software.content.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.db.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.software.content.mapper.CommentsMapper;
import org.software.content.service.CommentsService;
import org.software.feign.UserFeignClient;
import org.software.model.Response;
import org.software.model.content.dto.CommentDTO;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.content.vo.CommentChildVO;
import org.software.model.content.vo.CommentUnreadVO;
import org.software.model.content.vo.CommentVO;
import org.software.model.exception.BusinessException;
import org.software.model.interaction.comment.Comments;
import org.software.model.user.UserStatusV;
import org.software.model.user.UserV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments> implements CommentsService {
    
    @Autowired
    private UserFeignClient userFeignClient;
    
//    public CommentsServiceImpl(UserFeignClient userFeignClient) {
//    }
    
    @Override
    public Long addComment(CommentDTO commentDTO) throws BusinessException {
        // 校验内容ID不为空
        if (commentDTO.getContentId() == null) {
            log.warn("{} | userId: {}", HttpCodeEnum.PARAM_ERROR.getMsg(), commentDTO.getUserId());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

//        // 校验用户ID不为空
//        if (commentDTO.getUserId() == null) {
//            log.warn("{} | contentId: {}", HttpCodeEnum.PARAM_ERROR.getMsg(), commentDTO.getContentId());
//            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
//        }
        

        // 校验评论内容不为空
        if (commentDTO.getContent() == null || commentDTO.getContent().trim().isEmpty()) {
            log.warn("{} | userId: {} | contentId: {}", HttpCodeEnum.PARAM_ERROR.getMsg(), 
                commentDTO.getUserId(), commentDTO.getContentId());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 正常业务逻辑
        Comments comment = new Comments();
        comment.setContentId(commentDTO.getContentId());
        comment.setParentCommentId(commentDTO.getParentCommentId() == null ? 0 : commentDTO.getParentCommentId());
//        comment.setRootCommentId(commentDTO.getRootCommentId() == null ? 0 : commentDTO.getRootCommentId());
        if(commentDTO.getParentCommentId() == null || commentDTO.getParentCommentId() == 0) {
            // 根评论
            comment.setRootCommentId(0L);
        }else{
            // 不是根评论，需要查询父评论的rootCommentId，以及parentCommentId对应的userId
            LambdaQueryWrapper<Comments> parentQueryWrapper = new LambdaQueryWrapper<>();
            parentQueryWrapper.eq(Comments::getCommentId, commentDTO.getParentCommentId())
                    .isNull(Comments::getDeletedAt);
            Comments parentComment = getOne(parentQueryWrapper);
            if (parentComment == null) {
                log.warn("{} | parentCommentId: {}", HttpCodeEnum.PARENT_COMMENT_NOT_FOUND.getMsg(), commentDTO.getParentCommentId());
                throw new BusinessException(HttpCodeEnum.PARENT_COMMENT_NOT_FOUND);
            }
            if (parentComment.getRootCommentId() == 0) {
                // 父评论是根评论
                comment.setRootCommentId(parentComment.getCommentId());
                comment.setToUserId(parentComment.getUserId());
            } else {
                // 父评论不是根评论
                comment.setRootCommentId(parentComment.getRootCommentId());
                comment.setToUserId(parentComment.getUserId());
            }
        }
//        comment.setToUserId(commentDTO.getToUserId());
        comment.setContent(commentDTO.getContent());
        comment.setUserId(StpUtil.getLoginIdAsLong());
        comment.setIsRead(0);
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(new Date());
        save(comment);
        
        log.info("{} | commentId: {} | userId: {} | contentId: {} | parentCommentId: {}", 
            HttpCodeEnum.COMMENT_ADDED_SUCCESS.getMsg(), comment.getCommentId(), 
            commentDTO.getUserId(), commentDTO.getContentId(), comment.getParentCommentId());
        return comment.getCommentId();
    }

    @Override
    public List<CommentVO> getRootComments(Long contentId) throws BusinessException {
        // 校验内容ID不为空
        if (contentId == null) {
            log.warn("{}", HttpCodeEnum.PARAM_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 正常业务逻辑
        LambdaQueryWrapper<Comments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comments::getContentId, contentId)
                .eq(Comments::getParentCommentId, 0)
                .isNull(Comments::getDeletedAt);
        List<Comments> rootComments = list(queryWrapper);
        
        log.info("查询根评论 | contentId: {} | count: {}", contentId, rootComments.size());
        return rootComments.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            vo.setCommentId(comment.getCommentId());
            vo.setContentId(comment.getContentId());
            vo.setUserId(comment.getUserId());
            // 查询用户信息
            Response user = userFeignClient.getUser(comment.getUserId());
            Object data = user.getData();
            System.out.println(data);
            UserStatusV userData;
            if (data instanceof UserStatusV) {
                userData = (UserStatusV) data;
            } else {
                userData = BeanUtil.toBean(data, UserStatusV.class);
            }
            UserV userV = new UserV();
            userV.setUserV(userData);
            vo.setUser(userV);
            vo.setParentCommentId(comment.getParentCommentId());
            vo.setRootCommentId(comment.getRootCommentId());
            vo.setToUserId(comment.getToUserId());
            // 查询被回复用户信息
            if (comment.getToUserId() != null) {
                Response toUser = userFeignClient.getUser(comment.getToUserId());
                Object toData = toUser.getData();
                UserStatusV toUserData;
                if (toData instanceof UserStatusV) {
                    toUserData = (UserStatusV) toData;
                } else {
                    toUserData = BeanUtil.toBean(toData, UserStatusV.class);
                }
                UserV toUserV = new UserV();
                toUserV.setUserV(toUserData);
                vo.setToUser(toUserV);
            }else{
                vo.setToUser(new UserV());
            }
            vo.setContent(comment.getContent());
            vo.setCreatedAt(comment.getCreatedAt());
            vo.setUpdatedAt(comment.getUpdatedAt());
            vo.setDeletedAt(comment.getDeletedAt());
            long childCount = count(
                    new LambdaQueryWrapper<Comments>()
                            .eq(Comments::getParentCommentId, comment.getCommentId())
                            .isNull(Comments::getDeletedAt)
            );
            vo.setChildCount(Math.toIntExact(childCount));
            return vo;
        }).collect(Collectors.toList());
    }


    @Override
    public org.software.model.page.PageResult getChildComments(Integer rootCommentId, Integer pageNum, Integer pageSize) throws BusinessException {
        // 校验父评论ID不为空
        if (rootCommentId == null) {
            log.warn("{}", HttpCodeEnum.PARAM_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 校验父评论是否存在
        LambdaQueryWrapper<Comments> parentCheckWrapper = new LambdaQueryWrapper<>();
        parentCheckWrapper.eq(Comments::getCommentId, rootCommentId)
                .isNull(Comments::getDeletedAt);
        if (count(parentCheckWrapper) == 0) {
            log.warn("{} | parentCommentId: {}", HttpCodeEnum.PARENT_COMMENT_NOT_FOUND.getMsg(), rootCommentId);
            throw new BusinessException(HttpCodeEnum.PARENT_COMMENT_NOT_FOUND);
        }

        // 查询该父评论下的所有子评论（未删除）
        LambdaQueryWrapper<Comments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comments::getParentCommentId, rootCommentId)
                .isNull(Comments::getDeletedAt)
                .orderByAsc(Comments::getCreatedAt); // 按创建时间升序排列

        List<Comments> childComments = list(queryWrapper);
        
        log.info("查询子评论 | parentCommentId: {} | count: {}", rootCommentId, childComments.size());

        //------------------------
        Page<Comments> page = new Page<>(pageNum, pageSize);

        Page<Comments> resultPage = page(page, queryWrapper);
        List<CommentChildVO> records = resultPage.getRecords().stream().map(comment -> {
            CommentChildVO vo = new CommentChildVO();

            vo.setCommentId(comment.getCommentId());
            vo.setContentId(comment.getContentId());
            vo.setUserId(comment.getUserId());

            Response user = userFeignClient.getUser(comment.getUserId());
            Object data = user.getData();
            System.out.println(data);
            UserStatusV userData;
            if (data instanceof UserStatusV) {
                userData = (UserStatusV) data;
            } else {
                userData = BeanUtil.toBean(data, UserStatusV.class);
            }
            UserV userV = new UserV();
            userV.setUserV(userData);
            vo.setUser(userV);
            vo.setParentCommentId(comment.getParentCommentId());
            vo.setRootCommentId(comment.getRootCommentId());
            vo.setToUserId(comment.getToUserId());
            // 查询被回复用户信息
            if (comment.getToUserId() != null) {
                Response toUser = userFeignClient.getUser(comment.getToUserId());
                Object toData = toUser.getData();
                UserStatusV toUserData;
                if (toData instanceof UserStatusV) {
                    toUserData = (UserStatusV) toData;
                } else {
                    toUserData = BeanUtil.toBean(toData, UserStatusV.class);
                }
                UserV toUserV = new UserV();
                toUserV.setUserV(toUserData);
                vo.setToUser(toUserV);
            }else{
                vo.setToUser(new UserV());
            }

            vo.setParentCommentId(comment.getParentCommentId());
            vo.setRootCommentId(comment.getRootCommentId());
            vo.setContent(comment.getContent());
            vo.setCreatedAt(comment.getCreatedAt());
            vo.setUpdatedAt(comment.getUpdatedAt());
            vo.setDeletedAt(comment.getDeletedAt());

            return vo;
        }).collect(Collectors.toList());

        org.software.model.page.PageResult pageresult = new org.software.model.page.PageResult();
        pageresult.setRecords(records);
        pageresult.setPageNum(pageNum);
        pageresult.setPageSize(pageSize);
        return pageresult;

    }



    @Override
    public List<CommentUnreadVO> getUnreadComments() throws BusinessException {
        Long userId= StpUtil.getLoginIdAsLong();


        // 查询未读评论（未删除 + 接收方是当前用户 + 未读）
        LambdaQueryWrapper<Comments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comments::getToUserId, userId)
                .eq(Comments::getIsRead, 0)
                .isNull(Comments::getDeletedAt)
                .orderByDesc(Comments::getCreatedAt); // 按创建时间降序（最新的在前）

        List<Comments> unreadComments = list(queryWrapper);
        
        log.info("查询未读评论 | userId: {} | count: {}", userId, unreadComments.size());

        // 转换为VO返回
        return unreadComments.stream().map(comment -> {
            CommentUnreadVO vo = new CommentUnreadVO();
            vo.setCommentId(comment.getCommentId());
            vo.setContentId(comment.getContentId());
            vo.setUserId(comment.getUserId());
            // 查询用户信息
            Response user = userFeignClient.getUser(comment.getUserId());
            Object data = user.getData();
            UserStatusV userData;
            if (data instanceof UserStatusV) {
                userData = (UserStatusV) data;
            } else {
                userData = BeanUtil.toBean(data, UserStatusV.class);
            }
            UserV userV = new UserV();
            userV.setUserV(userData);
            vo.setUser(userV);
            vo.setParentCommentId(comment.getParentCommentId());
            vo.setRootCommentId(comment.getRootCommentId());
            vo.setToUserId(comment.getToUserId());
            // 查询被回复用户信息
            if (comment.getToUserId() != null) {
                Response toUser = userFeignClient.getUser(comment.getToUserId());
                Object toData = toUser.getData();
                UserStatusV toUserData;
                if (toData instanceof UserStatusV) {
                    toUserData = (UserStatusV) toData;
                } else {
                    toUserData = BeanUtil.toBean(toData, UserStatusV.class);
                }
                UserV toUserV = new UserV();
                toUserV.setUserV(toUserData);
                vo.setToUser(toUserV);
            }else{
                vo.setToUser(new UserV());
            }
            //TODO:firstMedia和mediatype从何来
            vo.setContent(comment.getContent());
            vo.setIsRead(comment.getIsRead());
            vo.setCreatedAt(comment.getCreatedAt());
            vo.setUpdatedAt(comment.getUpdatedAt());
            vo.setDeletedAt(comment.getDeletedAt());
            return vo;
        }).collect(Collectors.toList());
    }




    @Override
    public Long getUnreadCommentCount() throws BusinessException {
        Long userId= StpUtil.getLoginIdAsLong();

        // 统计未读评论数量（条件同getUnreadComments）
        LambdaQueryWrapper<Comments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comments::getToUserId, userId)
                .eq(Comments::getIsRead, 0)
                .isNull(Comments::getDeletedAt);

        Long count = count(queryWrapper);
        log.info("统计未读评论数量 | userId: {} | count: {}", userId, count);
        return count;
    }

    @Override
    public void deleteComments(Long commentId) throws BusinessException {
        //参数校验
        if (commentId == null) {
            log.warn("{}", HttpCodeEnum.PARAM_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 查询评论是否存在
        Comments comment = getById(commentId);
        if (comment == null || comment.getDeletedAt() != null) {
            log.warn("{} | commentId: {}", HttpCodeEnum.COMMENT_NOT_FOUND.getMsg(), commentId);
            throw new BusinessException(HttpCodeEnum.COMMENT_NOT_FOUND);
        }

        Date now = new Date();

        // 判断是否是根评论
        if (comment.getParentCommentId() == 0) {
            // 根评论
            LambdaQueryWrapper<Comments> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Comments::getRootCommentId, commentId)
                    .or()
                    .eq(Comments::getCommentId, commentId);

            Comments updateEntity = new Comments();
            updateEntity.setDeletedAt(now);
            updateEntity.setUpdatedAt(now);

            update(updateEntity, wrapper);

            log.info("删除根评论及其子评论 | rootCommentId: {}", commentId);
        } else {
            // 子评论
            comment.setDeletedAt(now);
            comment.setUpdatedAt(now);
            updateById(comment);

            log.info("删除子评论 | commentId: {}", commentId);
        }
    }

}
