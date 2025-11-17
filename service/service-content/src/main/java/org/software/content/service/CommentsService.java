package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.content.dto.CommentDTO;
import org.software.content.dto.CommentVO;
import org.software.model.interaction.comment.Comments;

import java.util.List;

public interface CommentsService extends IService<Comments> {
    Integer addComment(CommentDTO commentDTO);
    List<CommentVO> getRootComments(Integer contentId);
    List<CommentVO> getChildComments(Integer parentCommentId);
    List<CommentVO> getUnreadComments(Integer userId);
    Integer getUnreadCommentCount(Integer userId);
}