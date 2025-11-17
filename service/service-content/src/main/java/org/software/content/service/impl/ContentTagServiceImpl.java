package org.software.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.software.content.dto.ContentTagDTO;
import org.software.content.mapper.ContentTagMapper;
import org.software.content.service.ContentTagService;
import org.software.content.mapper.TagMapper;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.exception.SystemException;
import org.software.model.content.tag.ContentTag;
import org.software.model.content.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentTagServiceImpl extends ServiceImpl<ContentTagMapper, ContentTag> implements ContentTagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    @Transactional
    public boolean uploadContentTag(ContentTagDTO contentTagDTO) {
        // 校验内容ID不为空
        if (contentTagDTO.getContentId() == null) {
            try {
                throw new SystemException(HttpCodeEnum.CONTENT_ID_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 校验标签ID列表不为空
        if (contentTagDTO.getTagIds() == null || contentTagDTO.getTagIds().isEmpty()) {
            try {
                throw new SystemException(HttpCodeEnum.TAG_IDS_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

//        // 校验标签ID不包含null
//        if (contentTagDTO.getTagIds().contains(null)) {
//            try {
//                throw new SystemException(HttpCodeEnum.TAG_ID_INVALID);
//            } catch (SystemException e) {
//                throw new RuntimeException(e);
//            }
//        }

        // 校验标签是否有效
        LambdaQueryWrapper<Tag> tagCheckWrapper = new LambdaQueryWrapper<>();
        tagCheckWrapper.in(Tag::getTagId, contentTagDTO.getTagIds())
                .isNull(Tag::getDeletedAt);
        long validTagCount = tagMapper.selectCount(tagCheckWrapper);
        if (validTagCount != contentTagDTO.getTagIds().size()) {
            try {
                throw new SystemException(HttpCodeEnum.INVALID_TAG);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 先删除原有标签关联
        LambdaQueryWrapper<ContentTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentTag::getContentId, contentTagDTO.getContentId())
                .isNull(ContentTag::getDeletedAt);
        List<ContentTag> oldTags = list(queryWrapper);
        if (!oldTags.isEmpty()) {
            oldTags.forEach(tag -> tag.setDeletedAt(new Date()));
            updateBatchById(oldTags);
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
        return saveBatch(newTags);
    }

    @Override
    @Transactional
    public boolean updateContentTag(ContentTagDTO contentTagDTO) {
        // 复用上传逻辑
        return uploadContentTag(contentTagDTO);
    }
}