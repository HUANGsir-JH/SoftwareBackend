package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.content.Tag;
import org.software.model.content.dto.TagDTO;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;

public interface TagService extends IService<Tag> {
    boolean addTag(TagDTO tagDTO) throws BusinessException;//添加标签
    boolean updateTag(Integer tagId, TagDTO tagDTO) throws BusinessException;//更新标签
    PageResult getTagList(PageQuery query, String tagName, Integer isActive);//获取标签
    boolean deleteTag(Integer tagId) throws BusinessException;//删除标签
}
