package org.software.content.controller;

import org.software.content.service.ContentLikeFavoriteService;
import org.software.model.Response;
import org.software.model.content.dto.ContentLikeFavoriteDTO;
import org.software.model.content.vo.ContentLikeFavoriteVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/content-like-favorite")
public class ContentLikeFavoriteController {

    private final ContentLikeFavoriteService likeFavoriteService;

    public ContentLikeFavoriteController(ContentLikeFavoriteService likeFavoriteService) {
        this.likeFavoriteService = likeFavoriteService;
    }

    // 新增或取消点赞/收藏
    @PostMapping
    public Response addOrCancelLike(@RequestBody ContentLikeFavoriteDTO dto) {
        boolean result = likeFavoriteService.addOrCancelLike(dto);
        return Response.success(result);
    }

    // 获取用户的点赞/收藏记录
    @GetMapping
    public Response getLikeFavoriteRecords(
            @RequestParam Integer userId,
            @RequestParam String type) {
        List<ContentLikeFavoriteVO> records = likeFavoriteService.getLikeFavoriteRecords(userId, type);
        return Response.success(records);
    }

    // 标记所有记录为已读
    @PutMapping
    public Response readAll(
            @RequestParam Integer userId,
            @RequestParam String type) {
        boolean result = likeFavoriteService.readAll(userId, type);
        return Response.success(result);
    }

    // 获取未读的点赞/收藏记录
    @GetMapping("/unread")
    public Response getUnreadLikeFavorite(
            @RequestParam Integer userId,
            @RequestParam String type) {
        List<ContentLikeFavoriteVO> unreadRecords = likeFavoriteService.getUnreadLikeFavorite(userId, type);
        return Response.success(unreadRecords);
    }
}