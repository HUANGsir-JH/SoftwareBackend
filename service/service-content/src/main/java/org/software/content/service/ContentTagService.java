package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.content.dto.ContentTagDTO;
import org.software.model.content.tag.ContentTag;

public interface ContentTagService extends IService<ContentTag> {
    boolean uploadContentTag(ContentTagDTO contentTagDTO);
    boolean updateContentTag(ContentTagDTO contentTagDTO);
}