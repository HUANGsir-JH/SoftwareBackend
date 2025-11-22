package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.content.dto.ContentTagDTO;
import org.software.model.content.tag.ContentTag;
import org.software.model.exception.BusinessException;

public interface ContentTagService extends IService<ContentTag> {
    boolean uploadContentTag(ContentTagDTO contentTagDTO) throws BusinessException;
    boolean updateContentTag(ContentTagDTO contentTagDTO) throws BusinessException;
}