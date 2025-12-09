package org.software.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.software.content.mapper.TagMapper;
import org.software.content.service.TagService;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.content.dto.TagDTO;
import org.software.model.content.vo.TagVO;
import org.software.model.exception.BusinessException;
import org.software.model.content.Tag;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public boolean addTag(TagDTO tagDTO) throws BusinessException {
        // 标签名称为空校验
        if (tagDTO.getTagName() == null || tagDTO.getTagName().trim().isEmpty()) {
            log.warn("{} | tagName: null", HttpCodeEnum.PARAM_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 标签名称重复校验
        LambdaQueryWrapper<Tag> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Tag::getTagName, tagDTO.getTagName())
                .isNull(Tag::getDeletedAt);
        if (count(checkWrapper) > 0) {
            log.warn("{} | tagName: {}", HttpCodeEnum.TAG_NAME_DUPLICATE.getMsg(), tagDTO.getTagName());
            throw new BusinessException(HttpCodeEnum.TAG_NAME_DUPLICATE);
        }

        Tag tag = new Tag();
        tag.setTagName(tagDTO.getTagName());
        tag.setIsActive(tagDTO.getIsActive() == null ? 1 : tagDTO.getIsActive());
        tag.setCreatedAt(new Date());
        tag.setUpdatedAt(new Date());
        boolean result = save(tag);
        if (result) {
            log.info("标签添加成功 | tagName: {}, tagId: {}", tagDTO.getTagName(), tag.getTagId());
        }
        return result;
    }

    @Override
    public boolean updateTag(Integer tagId, TagDTO tagDTO) throws BusinessException {
        // 标签ID为空校验
        if (tagId == null) {
            log.warn("{} | tagId: null", HttpCodeEnum.PARAM_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 标签不存在校验
        Tag existTag = getById(tagId);
        if (existTag == null || existTag.getDeletedAt() != null) {
            log.warn("{} | tagId: {}", HttpCodeEnum.RESOURCE_NOT_FOUND.getMsg(), tagId);
            throw new BusinessException(HttpCodeEnum.RESOURCE_NOT_FOUND);
        }

        // 标签名称为空校验
        if (tagDTO.getTagName() == null || tagDTO.getTagName().trim().isEmpty()) {
            log.warn("{} | tagName: null", HttpCodeEnum.PARAM_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 标签名称重复校验(排除当前标签)
        LambdaQueryWrapper<Tag> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(Tag::getTagName, tagDTO.getTagName())
                .ne(Tag::getTagId, tagId)
                .isNull(Tag::getDeletedAt);
        if (count(checkWrapper) > 0) {
            log.warn("{} | tagName: {}, tagId: {}", HttpCodeEnum.TAG_NAME_DUPLICATE.getMsg(), tagDTO.getTagName(), tagId);
            throw new BusinessException(HttpCodeEnum.TAG_NAME_DUPLICATE);
        }

        existTag.setTagName(tagDTO.getTagName());
        existTag.setIsActive(tagDTO.getIsActive());
        existTag.setUpdatedAt(new Date());
        boolean result = updateById(existTag);
        if (result) {
            log.info("标签更新成功 | tagId: {}, tagName: {}", tagId, tagDTO.getTagName());
        }
        return result;
    }

    @Override
    public PageResult getTagList(PageQuery query, String tagName, Integer isActive) {
        Page<Tag> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        if (tagName != null && !tagName.trim().isEmpty()) {
            queryWrapper.eq(Tag::getTagName, tagName);
        }
        if (isActive != null) {
            queryWrapper.eq(Tag::getIsActive, isActive);
        }
        queryWrapper.isNull(Tag::getDeletedAt);
        page = page(page, queryWrapper);

        List<TagVO> tagVOS = page.getRecords().stream()
                        .map(tag -> BeanUtil.toBean(tag, TagVO.class)).toList();
        log.info("查询标签列表成功 | count: {}", page.getRecords().size());
        return PageResult.builder()
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .records(tagVOS)
                .total(page.getTotal())
                .build();
    }

    @Override
    public boolean deleteTag(Integer tagId) throws BusinessException {
        // 标签ID为空校验
        if (tagId == null) {
            log.warn("{} | tagId: null", HttpCodeEnum.PARAM_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 标签不存在校验
        Tag existTag = getById(tagId);
        if (existTag == null || existTag.getDeletedAt() != null) {
            log.warn("{} | tagId: {}", HttpCodeEnum.RESOURCE_NOT_FOUND.getMsg(), tagId);
            throw new BusinessException(HttpCodeEnum.RESOURCE_NOT_FOUND);
        }

        existTag.setDeletedAt(new Date());
        boolean result = updateById(existTag);
        if (result) {
            log.info("标签删除成功 | tagId: {}", tagId);
        }
        return result;
    }
}