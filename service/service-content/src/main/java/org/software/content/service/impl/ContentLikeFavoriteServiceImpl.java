package org.software.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.software.content.dto.ContentLikeFavoriteDTO;
import org.software.content.mapper.ContentLikeFavoriteMapper;
import org.software.content.service.ContentLikeFavoriteService;
import org.software.content.dto.ContentLikeFavoriteVO;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.exception.SystemException;
import org.software.model.interaction.ContentLikeFavorite;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContentLikeFavoriteServiceImpl extends ServiceImpl<ContentLikeFavoriteMapper, ContentLikeFavorite> implements ContentLikeFavoriteService {

    @Override
    @Transactional
    public boolean addOrCancelLike(ContentLikeFavoriteDTO dto) {
        // 校验内容ID不为空
        if (dto.getContentId() == null) {
            try {
                throw new SystemException(HttpCodeEnum.CONTENT_ID_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 校验用户ID不为空
        if (dto.getUserId() == null) {
            try {
                throw new SystemException(HttpCodeEnum.USER_ID_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 校验操作类型不为空
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            try {
                throw new SystemException(HttpCodeEnum.TYPE_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 校验操作类型有效
        if (!"like".equals(dto.getType())) {
            try {
                throw new SystemException(HttpCodeEnum.INVALID_TYPE);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 检查是否已存在该记录
        LambdaQueryWrapper<ContentLikeFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentLikeFavorite::getContentId, dto.getContentId())
                .eq(ContentLikeFavorite::getUserId, dto.getUserId())
                .eq(ContentLikeFavorite::getType, dto.getType())
                .isNull(ContentLikeFavorite::getDeletedAt);
        ContentLikeFavorite exist = getOne(queryWrapper);

        if (exist != null) {
            // 已存在：取消（标记删除）
            exist.setDeletedAt(new Date());
            return updateById(exist);
        } else {
            // 不存在：新增
            ContentLikeFavorite like = new ContentLikeFavorite();
            like.setContentId(dto.getContentId());
            like.setUserId(dto.getUserId());
            like.setType(dto.getType());
            like.setIsRead(0);
            like.setCreatedAt(new Date());
            like.setUpdatedAt(new Date());
            return save(like);
        }
    }

    @Override
    public List<ContentLikeFavoriteVO> getLikeFavoriteRecords(Integer userId, String type) {
        // 校验用户ID不为空
        if (userId == null) {
            try {
                throw new SystemException(HttpCodeEnum.USER_ID_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 校验类型不为空
        if (type == null || type.trim().isEmpty()) {
            try {
                throw new SystemException(HttpCodeEnum.TYPE_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 查询记录
        LambdaQueryWrapper<ContentLikeFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentLikeFavorite::getUserId, userId)
                .eq(ContentLikeFavorite::getType, type)
                .isNull(ContentLikeFavorite::getDeletedAt);
        List<ContentLikeFavorite> list = list(queryWrapper);

        // 转换为VO
        return list.stream().map(like -> {
            ContentLikeFavoriteVO vo = new ContentLikeFavoriteVO();
            vo.setLikeId(like.getLikeId());
            vo.setContentId(like.getContentId());
            vo.setUserId(like.getUserId());
            vo.setIsRead(like.getIsRead());
            vo.setType(like.getType());
            vo.setCreatedAt(like.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean readAll(Integer userId, String type) {
        // 校验用户ID不为空
        if (userId == null) {
            try {
                throw new SystemException(HttpCodeEnum.USER_ID_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 校验类型不为空
        if (type == null || type.trim().isEmpty()) {
            try {
                throw new SystemException(HttpCodeEnum.TYPE_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 查询未读记录并标记为已读
        LambdaQueryWrapper<ContentLikeFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentLikeFavorite::getUserId, userId)
                .eq(ContentLikeFavorite::getType, type)
                .eq(ContentLikeFavorite::getIsRead, 0)
                .isNull(ContentLikeFavorite::getDeletedAt);
        List<ContentLikeFavorite> list = list(queryWrapper);

        list.forEach(like -> {
            like.setIsRead(1);
            like.setUpdatedAt(new Date());
        });
        return updateBatchById(list);
    }

    @Override
    public List<ContentLikeFavoriteVO> getUnreadLikeFavorite(Integer userId, String type) {
        // 校验用户ID不为空
        if (userId == null) {
            try {
                throw new SystemException(HttpCodeEnum.USER_ID_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 校验类型不为空
        if (type == null || type.trim().isEmpty()) {
            try {
                throw new SystemException(HttpCodeEnum.TYPE_NULL);
            } catch (SystemException e) {
                throw new RuntimeException(e);
            }
        }

        // 查询未读记录
        LambdaQueryWrapper<ContentLikeFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentLikeFavorite::getUserId, userId)
                .eq(ContentLikeFavorite::getType, type)
                .eq(ContentLikeFavorite::getIsRead, 0)
                .isNull(ContentLikeFavorite::getDeletedAt);
        List<ContentLikeFavorite> list = list(queryWrapper);

        // 转换为VO
        return list.stream().map(like -> {
            ContentLikeFavoriteVO vo = new ContentLikeFavoriteVO();
            vo.setLikeId(like.getLikeId());
            vo.setContentId(like.getContentId());
            vo.setUserId(like.getUserId());
            vo.setIsRead(like.getIsRead());
            vo.setType(like.getType());
            vo.setCreatedAt(like.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }
}