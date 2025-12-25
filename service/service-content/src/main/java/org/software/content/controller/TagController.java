// org.software.content.tag.controller.TagController
package org.software.content.controller;

import org.software.content.service.TagService;
import org.software.model.Response;
import org.software.model.content.dto.TagDTO;
import org.software.model.content.vo.TagVO;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public Response addTag(@RequestBody TagDTO tagDTO) throws BusinessException {
        boolean result = tagService.addTag(tagDTO);
        return Response.success(result);
    }

    @PutMapping
    public Response updateTag(@RequestBody TagDTO tagDTO) throws BusinessException {
        Integer tagId = tagDTO.getTagId();
        boolean result = tagService.updateTag(tagId, tagDTO);
        return Response.success(result);
    }

    @GetMapping
    public Response getTagList(PageQuery query, String tagName, Integer isActive) {
        return Response.success(tagService.getTagList(query, tagName, isActive));
    }

    @DeleteMapping("/{tagId}")
    public Response deleteTag(@PathVariable Integer tagId) throws BusinessException {
        boolean result = tagService.deleteTag(tagId);
        return Response.success(result);
    }
}