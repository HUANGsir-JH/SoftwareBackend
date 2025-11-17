package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.content.dto.TagDTO;
import org.software.content.dto.TagVO;
import org.software.model.content.tag.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {
    boolean addTag(TagDTO tagDTO);//添加标签
    boolean updateTag(Integer tagId, TagDTO tagDTO);//更新标签
    List<TagVO> getTagList();//获取标签
    boolean deleteTag(Integer tagId);//删除标签
}
