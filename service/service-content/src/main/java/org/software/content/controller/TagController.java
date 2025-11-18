// org.software.content.tag.controller.TagController
package org.software.content.controller;

import org.software.content.dto.TagDTO;
import org.software.content.service.TagService;
import org.software.content.dto.TagVO;
import org.software.model.Response;
import org.springframework.http.ResponseEntity;
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
    public Response  addTag(@RequestBody TagDTO tagDTO) {
        boolean result = tagService.addTag(tagDTO);
        return Response.success(result);
    }

    @PutMapping
    public Response updateTag(@PathVariable Integer tagId, @RequestBody TagDTO tagDTO) {
        boolean result = tagService.updateTag(tagId, tagDTO);
        return Response.success(result);
    }

    @GetMapping
    public Response getTagList() {
        List<TagVO> tagList = tagService.getTagList();
        return Response.success(tagList);
    }

    @DeleteMapping("/{tagId}")
    public Response deleteTag(@PathVariable Integer tagId) {
        boolean result = tagService.deleteTag(tagId);
        return Response.success(result);
    }
}