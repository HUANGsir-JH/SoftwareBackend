package org.software.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.software.content.dto.CommentDTO;
import org.software.content.mapper.CommentsMapper;
import org.software.content.service.CommentsService;
import org.software.content.dto.CommentVO;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.exception.SystemException;
import org.software.model.interaction.comment.Comments;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentsServiceImpl extends ServiceImpl<CommentsMapper, Comments> implements CommentsService {

    @Override
    public Integer addComment(CommentDTO commentDTO) {
        // 校验内容ID不为空
        if (commentDTO.getContentId() == null) {
            try {
                throw new SystemException(HttpCodeEnum.CONTENT_ID_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 校验用户ID不为空
        if (commentDTO.getUserId() == null) {
            try {
                throw new SystemException(HttpCodeEnum.USER_ID_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 校验评论内容不为空
        if (commentDTO.getContent() == null || commentDTO.getContent().trim().isEmpty()) {
            try {
                throw new SystemException(HttpCodeEnum.COMMENT_CONTENT_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 正常业务逻辑
        Comments comment = new Comments();
        comment.setContentId(commentDTO.getContentId());
        comment.setParentCommentId(commentDTO.getParentCommentId() == null ? 0 : commentDTO.getParentCommentId());
        comment.setRootCommentId(commentDTO.getRootCommentId() == null ? 0 : commentDTO.getRootCommentId());
        comment.setToUserId(commentDTO.getToUserId());
        comment.setContent(commentDTO.getContent());
        comment.setUserId(commentDTO.getUserId());
        comment.setIsRead(0);
        comment.setCreatedAt(new Date());
        comment.setUpdatedAt(new Date());
        save(comment);
        return comment.getCommentId();
    }

    @Override
    public List<CommentVO> getRootComments(Integer contentId) {
        // 校验内容ID不为空
        if (contentId == null) {
            try {
                throw new SystemException(HttpCodeEnum.CONTENT_ID_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 正常业务逻辑
        LambdaQueryWrapper<Comments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comments::getContentId, contentId)
                .eq(Comments::getParentCommentId, 0)
                .isNull(Comments::getDeletedAt);
        List<Comments> rootComments = list(queryWrapper);
        return rootComments.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            vo.setCommentId(comment.getCommentId());
            vo.setContentId(comment.getContentId());
            vo.setUserId(comment.getUserId());
            vo.setParentCommentId(comment.getParentCommentId());
            vo.setRootCommentId(comment.getRootCommentId());
            vo.setToUserId(comment.getToUserId());
            vo.setContent(comment.getContent());
            vo.setIsRead(comment.getIsRead());
            vo.setCreatedAt(comment.getCreatedAt());
            vo.setChildren(new ArrayList<>());
            return vo;
        }).collect(Collectors.toList());
    }


    @Override
    public List<CommentVO> getChildComments(Integer parentCommentId) {
        // 校验父评论ID不为空
        if (parentCommentId == null) {
            try {
                throw new SystemException(HttpCodeEnum.PARAM_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 校验父评论是否存在
        LambdaQueryWrapper<Comments> parentCheckWrapper = new LambdaQueryWrapper<>();
        parentCheckWrapper.eq(Comments::getCommentId, parentCommentId)
                .isNull(Comments::getDeletedAt);
        if (count(parentCheckWrapper) == 0) {
            try {
                throw new SystemException(HttpCodeEnum.RESOURCE_NOT_EXIST);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 查询该父评论下的所有子评论（未删除）
        LambdaQueryWrapper<Comments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comments::getParentCommentId, parentCommentId)
                .isNull(Comments::getDeletedAt)
                .orderByAsc(Comments::getCreatedAt); // 按创建时间升序排列

        List<Comments> childComments = list(queryWrapper);

        // 转换为VO返回
        return childComments.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            vo.setCommentId(comment.getCommentId());
            vo.setContentId(comment.getContentId());
            vo.setUserId(comment.getUserId());
            vo.setParentCommentId(comment.getParentCommentId());
            vo.setRootCommentId(comment.getRootCommentId());
            vo.setToUserId(comment.getToUserId());
            vo.setContent(comment.getContent());
            vo.setIsRead(comment.getIsRead());
            vo.setCreatedAt(comment.getCreatedAt());
            vo.setChildren(new ArrayList<>()); // 子评论暂不嵌套
            return vo;
        }).collect(Collectors.toList());
    }



    @Override
    public List<CommentVO> getUnreadComments(Integer userId) {
        // 校验用户ID不为空
        if (userId == null) {
            try {
                throw new SystemException(HttpCodeEnum.USER_ID_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 查询未读评论（未删除 + 接收方是当前用户 + 未读）
        LambdaQueryWrapper<Comments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comments::getToUserId, userId)
                .eq(Comments::getIsRead, 0)
                .isNull(Comments::getDeletedAt)
                .orderByDesc(Comments::getCreatedAt); // 按创建时间降序（最新的在前）

        List<Comments> unreadComments = list(queryWrapper);

        // 转换为VO返回
        return unreadComments.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            vo.setCommentId(comment.getCommentId());
            vo.setContentId(comment.getContentId());
            vo.setUserId(comment.getUserId());
            vo.setParentCommentId(comment.getParentCommentId());
            vo.setRootCommentId(comment.getRootCommentId());
            vo.setToUserId(comment.getToUserId());
            vo.setContent(comment.getContent());
            vo.setIsRead(comment.getIsRead());
            vo.setCreatedAt(comment.getCreatedAt());
            vo.setChildren(new ArrayList<>());
            return vo;
        }).collect(Collectors.toList());
    }




    @Override
    public Integer getUnreadCommentCount(Integer userId) {
        // 校验用户ID不为空
        if (userId == null) {
            try {
                throw new SystemException(HttpCodeEnum.USER_ID_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 统计未读评论数量（条件同getUnreadComments）
        LambdaQueryWrapper<Comments> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comments::getToUserId, userId)
                .eq(Comments::getIsRead, 0)
                .isNull(Comments::getDeletedAt);

        return Math.toIntExact(count(queryWrapper));
    }
}