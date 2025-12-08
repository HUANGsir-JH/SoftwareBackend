package org.software.content.controller;


import org.software.content.service.CommentsService;
import org.software.model.content.dto.CommentDTO;
import org.software.model.Response;
import org.software.model.content.vo.CommentVO;
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
        Integer commentId = commentsService.addComment(commentDTO);
        return Response.success(commentId);
    }

    // 获取根评论
    @GetMapping("/root")
    public Response getRootComments(@RequestParam Integer contentId) {
        List<CommentVO> rootComments = commentsService.getRootComments(contentId);
        return Response.success(rootComments);
    }
    @GetMapping("/child")
    public Response getChildComments(@RequestParam Integer commentId) {
        List<CommentVO> childComments = commentsService.getChildComments(commentId);
        return Response.success(childComments);
    }
    @GetMapping("/unread")
    public Response getUnreadComments(@RequestParam Integer commentId){
        List<CommentVO> unreadComments = commentsService.getUnreadComments(commentId);
        return Response.success(unreadComments);
    }
    @GetMapping("unread")
    public Response getUnreadCommentsByParentId(@RequestParam Integer parentCommentId){
        Integer count=commentsService.getUnreadCommentCount(parentCommentId);
        return Response.success(count);
    }
}