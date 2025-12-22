package org.software.content.controller;

import org.software.content.service.ContentLikeFavoriteService;
import org.software.model.Response;
import org.software.model.content.dto.ContentLikeFavoriteDTO;
import org.software.model.content.vo.ContentLikeFavoriteVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class ContentLikeFavoriteController {

    private final ContentLikeFavoriteService likeFavoriteService;

    public ContentLikeFavoriteController(ContentLikeFavoriteService likeFavoriteService) {
        this.likeFavoriteService = likeFavoriteService;
    }

    // 新增或取消点赞/收藏
    @PostMapping("/content-like-favorite")
    public Response addOrCancelLike(@RequestBody ContentLikeFavoriteDTO dto) {
        boolean result = likeFavoriteService.addOrCancelLike(dto);
        return Response.success(result);
    }

    // 获取用户的分页点赞/收藏记录
    @GetMapping("/content-like-favorite")
    public Response getLikeFavoriteRecords(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam String type) {

        return Response.success(
                likeFavoriteService.getLikeFavoriteRecords(pageNum, pageSize, type)
        );
    }


    // 标记所有记录为已读
    @PutMapping("/content-like-favorite")
    public Response readAll() {
        boolean result = likeFavoriteService.readAll();
        return Response.success(result);
    }

    // 获取未读的分页点赞/收藏记录
    @GetMapping("/content-like-favorite/unread")
    public Response getUnreadLikeFavorite(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize,
            @RequestParam String type) {

        return Response.success(
                likeFavoriteService.getUnreadLikeFavorite(pageNum, pageSize, type)
        );
    }

}
