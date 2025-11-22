package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.content.dto.TagDTO;
import org.software.model.content.vo.TagVO;
import org.software.model.content.tag.Tag;
import org.software.model.exception.BusinessException;

import java.util.List;

public interface TagService extends IService<Tag> {
    boolean addTag(TagDTO tagDTO) throws BusinessException;//添加标签
    boolean updateTag(Integer tagId, TagDTO tagDTO) throws BusinessException;//更新标签
    List<TagVO> getTagList();//获取标签
    boolean deleteTag(Integer tagId) throws BusinessException;//删除标签
}
