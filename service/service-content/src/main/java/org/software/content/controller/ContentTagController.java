package org.software.content.controller;

import org.software.model.content.dto.ContentTagDTO;
import org.software.content.service.ContentTagService;
import org.software.model.Response;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/content-tag")
public class ContentTagController {

    private final ContentTagService contentTagService;

    public ContentTagController(ContentTagService contentTagService) {
        this.contentTagService = contentTagService;
    }

    // 上传内容标签关联
    @PostMapping
    public Response uploadContentTag(@RequestBody ContentTagDTO contentTagDTO) {
        boolean result = contentTagService.uploadContentTag(contentTagDTO);
        return Response.success(result);
    }

    // 更新内容标签关联
    @PutMapping
    public Response updateContentTag(@RequestBody ContentTagDTO contentTagDTO) {
        boolean result = contentTagService.updateContentTag(contentTagDTO);
        return Response.success(result);
    }
}