package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.content.dto.CommentDTO;
import org.software.model.content.vo.CommentUnreadVO;
import org.software.model.content.vo.CommentVO;
import org.software.model.exception.BusinessException;
import org.software.model.interaction.comment.Comments;
import org.software.model.page.PageResult;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CommentsService extends IService<Comments> {
    Long addComment(CommentDTO commentDTO) throws BusinessException;
    List<CommentVO> getRootComments(Long contentId) throws BusinessException;
    PageResult getChildComments(Integer rootCommentId, Integer pageNum, Integer pageSize) throws BusinessException;
    List<CommentUnreadVO> getUnreadComments() throws BusinessException;
    Long getUnreadCommentCount() throws BusinessException;

    void deleteComments(Long commentId);

}
