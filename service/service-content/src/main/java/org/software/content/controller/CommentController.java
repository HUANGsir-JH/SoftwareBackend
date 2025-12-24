package org.software.content.controller;


import org.software.content.service.CommentsService;
import org.software.model.content.dto.CommentDTO;
import org.software.model.Response;
import org.software.model.content.vo.CommentChildVO;
import org.software.model.content.vo.CommentUnreadVO;
import org.software.model.content.vo.CommentVO;
import org.software.model.page.PageResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentsService commentsService;

    public CommentController(CommentsService commentsService) {
        this.commentsService = commentsService;
    }

    // 新增评论
    @PostMapping
    public Response addComment(@RequestBody CommentDTO commentDTO) {
        Long commentId = commentsService.addComment(commentDTO);
        return Response.success(commentId);
    }

    // 获取根评论
    @GetMapping("/root")
    public Response getRootComments(@RequestParam Long contentId) {
        List<CommentVO> rootComments = commentsService.getRootComments(contentId);
        return Response.success(rootComments);
    }
    @GetMapping("/child")
    public Response getChildComments(@RequestParam Integer rootCommentId,@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        PageResult childComments = commentsService.getChildComments(rootCommentId,pageNum,pageSize);
        return Response.success(childComments);
    }
    @GetMapping("/unread")
    public Response getUnreadComments(){
        List<CommentUnreadVO> unreadComments = commentsService.getUnreadComments();
        return Response.success(unreadComments);
    }
    @GetMapping("/unread/count")
    public Response getUnreadCommentsCount(){
        Long count=commentsService.getUnreadCommentCount();
        return Response.success(count);
    }
    @DeleteMapping("/{commentId}")
    public Response deleteComments(@PathVariable Long commentId) {
        commentsService.deleteComments(commentId);
        return Response.success();
    }
}
