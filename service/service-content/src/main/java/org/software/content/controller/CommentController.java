package org.software.content.controller;


import org.software.content.dto.CommentDTO;
import org.software.content.service.CommentsService;
import org.software.content.dto.CommentVO;
import org.software.model.Response;
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
    @PostMapping("/add")
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
}