package org.software.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.software.content.dto.TagDTO;
import org.software.content.mapper.TagMapper;
import org.software.content.service.TagService;
import org.software.content.dto.TagVO;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.exception.SystemException;
import org.software.model.content.tag.Tag;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public boolean addTag(TagDTO tagDTO) {
        // 标签名称为空校验
        if (tagDTO.getTagName() == null || tagDTO.getTagName().trim().isEmpty()) {
            try {
                throw new SystemException(HttpCodeEnum.TAG_NAME_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 标签名称重复校验
        LambdaQueryWrapper<Tag> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Tag::getTagName, tagDTO.getTagName())
                .isNull(Tag::getDeletedAt);
        if (count(checkWrapper) > 0) {
            try {
                throw new SystemException(HttpCodeEnum.TAG_NAME_DUPLICATE);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        Tag tag = new Tag();
        tag.setTagName(tagDTO.getTagName());
        tag.setIsActive(tagDTO.getIsActive() == null ? 1 : tagDTO.getIsActive());
        tag.setCreatedAt(new Date());
        tag.setUpdatedAt(new Date());
        return save(tag);
    }

    @Override
    public boolean updateTag(Integer tagId, TagDTO tagDTO) {
        // 标签ID为空校验
        if (tagId == null) {
            try {
                throw new SystemException(HttpCodeEnum.TAG_ID_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 标签不存在校验
        Tag existTag = getById(tagId);
        if (existTag == null || existTag.getDeletedAt() != null) {
            try {
                throw new SystemException(HttpCodeEnum.RESOURCE_NOT_EXIST);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 标签名称为空校验
        if (tagDTO.getTagName() == null || tagDTO.getTagName().trim().isEmpty()) {
            try {
                throw new SystemException(HttpCodeEnum.TAG_NAME_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 标签名称重复校验（排除当前标签）
        LambdaQueryWrapper<Tag> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Tag::getTagName, tagDTO.getTagName())
                .ne(Tag::getTagId, tagId)
                .isNull(Tag::getDeletedAt);
        if (count(checkWrapper) > 0) {
            try {
                throw new SystemException(HttpCodeEnum.TAG_NAME_DUPLICATE);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        existTag.setTagName(tagDTO.getTagName());
        existTag.setIsActive(tagDTO.getIsActive());
        existTag.setUpdatedAt(new Date());
        return updateById(existTag);
    }

    @Override
    public List<TagVO> getTagList() {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNull(Tag::getDeletedAt);
        List<Tag> tagList = list(queryWrapper);
        return tagList.stream().map(tag -> {
            TagVO vo = new TagVO();
            vo.setTagId(tag.getTagId());
            vo.setTagName(tag.getTagName());
            vo.setIsActive(tag.getIsActive());
            vo.setCreatedAt(tag.getCreatedAt());
            vo.setUpdatedAt(tag.getUpdatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean deleteTag(Integer tagId) {
        // 标签ID为空校验
        if (tagId == null) {
            try {
                throw new SystemException(HttpCodeEnum.TAG_ID_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 标签不存在校验
        Tag existTag = getById(tagId);
        if (existTag == null || existTag.getDeletedAt() != null) {
            try {
                throw new SystemException(HttpCodeEnum.RESOURCE_NOT_EXIST);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        existTag.setDeletedAt(new Date());
        return updateById(existTag);
    }
}