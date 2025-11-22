package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.content.dto.CommentDTO;
import org.software.model.content.vo.CommentVO;
import org.software.model.exception.BusinessException;
import org.software.model.interaction.comment.Comments;

import java.util.List;

public interface CommentsService extends IService<Comments> {
    Integer addComment(CommentDTO commentDTO) throws BusinessException;
    List<CommentVO> getRootComments(Integer contentId) throws BusinessException;
    List<CommentVO> getChildComments(Integer parentCommentId) throws BusinessException;
    List<CommentVO> getUnreadComments(Integer userId) throws BusinessException;
    Integer getUnreadCommentCount(Integer userId) throws BusinessException;
}