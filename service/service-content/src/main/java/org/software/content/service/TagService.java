package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.content.Tag;
import org.software.model.content.dto.TagDTO;
import org.software.model.content.dto.UpdateTagDTO;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.springframework.web.bind.annotation.PathVariable;

public interface TagService extends IService<Tag> {
    Integer addTag(TagDTO tagDTO) throws BusinessException;//添加标签
    Integer updateTag(UpdateTagDTO tagDTO) throws BusinessException;//更新标签
    PageResult getTagList(Integer pageNum,Integer pageSize,String tagName,Integer isActive);//获取标签
    void deleteTag(Integer tagId) throws BusinessException;//删除标签
}
