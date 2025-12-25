// org.software.content.tag.controller.TagController
package org.software.content.controller;

import org.software.content.service.TagService;
import org.software.model.Response;
import org.software.model.content.dto.TagDTO;
import org.software.model.content.dto.UpdateTagDTO;
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
        Integer result = tagService.addTag(tagDTO);
        return Response.success(result);
    }

    @PutMapping
    public Response updateTag(@RequestBody UpdateTagDTO tagDTO) throws BusinessException {
        Integer result = tagService.updateTag(tagDTO);
        return Response.success(result);
    }

    @GetMapping
    public Response getTagList(@RequestParam Integer pageNum,@RequestParam Integer pageSize,@RequestParam String tagName, @RequestParam Integer isActive) {
        return Response.success(tagService.getTagList(pageNum,pageSize,tagName,isActive));
    }

    @DeleteMapping("/{tagId}")
    public Response deleteTag(@PathVariable Integer tagId) throws BusinessException {
        tagService.deleteTag(tagId);
        return Response.success(null);
    }
}
