package org.software.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.software.content.mapper.ContentTagMapper;
import org.software.content.service.ContentTagService;
import org.software.content.mapper.TagMapper;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.content.dto.ContentTagDTO;
import org.software.model.exception.BusinessException;
import org.software.model.content.ContentTag;
import org.software.model.content.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ContentTagServiceImpl extends ServiceImpl<ContentTagMapper, ContentTag> implements ContentTagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    @Transactional
    public boolean uploadContentTag(ContentTagDTO contentTagDTO) throws BusinessException {
        // 校验内容ID不为空
        if (contentTagDTO.getContentId() == null) {
            log.warn("{} | contentId: null", HttpCodeEnum.PARAM_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 校验标签ID列表不为空
        if (contentTagDTO.getTagIds() == null || contentTagDTO.getTagIds().isEmpty()) {
            log.warn("{} | tagIds: empty", HttpCodeEnum.PARAM_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 校验标签是否有效
        LambdaQueryWrapper<Tag> tagCheckWrapper = new LambdaQueryWrapper<>();
        tagCheckWrapper.in(Tag::getTagId, contentTagDTO.getTagIds())
                .isNull(Tag::getDeletedAt);
        long validTagCount = tagMapper.selectCount(tagCheckWrapper);
        if (validTagCount != contentTagDTO.getTagIds().size()) {
            log.warn("{} | contentId: {}, requestedTags: {}, validTags: {}", 
                    HttpCodeEnum.INVALID_TAG.getMsg(), 
                    contentTagDTO.getContentId(), 
                    contentTagDTO.getTagIds().size(), 
                    validTagCount);
            throw new BusinessException(HttpCodeEnum.INVALID_TAG);
        }

        // 先删除原有标签关联
        LambdaQueryWrapper<ContentTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentTag::getContentId, contentTagDTO.getContentId())
                .isNull(ContentTag::getDeletedAt);
        List<ContentTag> oldTags = list(queryWrapper);
        if (!oldTags.isEmpty()) {
            oldTags.forEach(tag -> tag.setDeletedAt(new Date()));
            updateBatchById(oldTags);
            log.info("删除原有标签关联 | contentId: {}, count: {}", contentTagDTO.getContentId(), oldTags.size());
        }

        // 新增标签关联
        List<ContentTag> newTags = contentTagDTO.getTagIds().stream().map(tagId -> {
            ContentTag contentTag = new ContentTag();
            contentTag.setContentId(contentTagDTO.getContentId());
            contentTag.setTagId(tagId);
            contentTag.setCreatedAt(new Date());
            contentTag.setUpdatedAt(new Date());
            return contentTag;
        }).collect(Collectors.toList());
        boolean result = saveBatch(newTags);
        if (result) {
            log.info("内容标签上传成功 | contentId: {}, tagCount: {}", contentTagDTO.getContentId(), newTags.size());
        }
        return result;
    }

    @Override
    @Transactional
    public boolean updateContentTag(ContentTagDTO contentTagDTO) throws BusinessException {
        // 复用上传逻辑
        log.info("更新内容标签 | contentId: {}", contentTagDTO.getContentId());
        return uploadContentTag(contentTagDTO);
    }
}