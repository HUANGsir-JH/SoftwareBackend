package org.software.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.software.content.mapper.ContentLikeFavoriteMapper;
import org.software.content.mapper.ContentMapper;
import org.software.content.service.ContentLikeFavoriteService;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.content.Content;
import org.software.model.content.dto.ContentLikeFavoriteDTO;
import org.software.model.content.vo.ContentLikeFavoriteVO;
import org.software.model.exception.BusinessException;
import org.software.model.interaction.ContentLikeFavorite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ContentLikeFavoriteServiceImpl extends ServiceImpl<ContentLikeFavoriteMapper, ContentLikeFavorite> implements ContentLikeFavoriteService {

    @Autowired
    private ContentMapper contentMapper;

    private void updateContentCounter(Long contentId, String type, int delta) {

        Content content = contentMapper.selectById(contentId);
        if (content == null) return;

        if ("like".equals(type)) {
            content.setLikeCount(content.getLikeCount() + delta);
        } else {
            content.setFavoriteCount(content.getFavoriteCount() + delta);
        }

        contentMapper.updateById(content);
    }
    
    @Override
    @Transactional
    public boolean addOrCancelLike(ContentLikeFavoriteDTO dto) throws BusinessException {
        // 校验内容ID不为空
        if (dto.getContentId() == null) {
            log.warn("{} | userId: {}", HttpCodeEnum.PARAM_ERROR.getMsg(), dto.getUserId());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 校验用户ID不为空
        if (dto.getUserId() == null) {
            log.warn("{} | contentId: {}", HttpCodeEnum.PARAM_ERROR.getMsg(), dto.getContentId());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 校验操作类型不为空
        if (dto.getType() == null || dto.getType().trim().isEmpty()) {
            log.warn("{} | userId: {} | contentId: {}", HttpCodeEnum.PARAM_ERROR.getMsg(), 
                dto.getUserId(), dto.getContentId());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 校验操作类型有效
        if (!"like".equals(dto.getType())) {
            log.warn("{} | type: {} | userId: {} | contentId: {}", HttpCodeEnum.INVALID_TYPE.getMsg(), 
                dto.getType(), dto.getUserId(), dto.getContentId());
            throw new BusinessException(HttpCodeEnum.INVALID_TYPE);
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
            boolean result = updateById(exist);
            log.info("取消点赞 | likeId: {} | userId: {} | contentId: {} | result: {}", 
                exist.getLikeId(), dto.getUserId(), dto.getContentId(), result);
            updateContentCounter(dto.getContentId(), dto.getType(), +1);
            return result;
        } else {
            // 不存在：新增
            ContentLikeFavorite like = new ContentLikeFavorite();
            like.setContentId(dto.getContentId());
            like.setUserId(dto.getUserId());
            like.setType(dto.getType());
            like.setIsRead(0);
            like.setCreatedAt(new Date());
            like.setUpdatedAt(new Date());
            boolean result = save(like);
            log.info("添加点赞 | likeId: {} | userId: {} | contentId: {} | result: {}", 
                like.getLikeId(), dto.getUserId(), dto.getContentId(), result);
            updateContentCounter(dto.getContentId(), dto.getType(), +1);
            return result;
        }
    }

    @Override
    public List<ContentLikeFavoriteVO> getLikeFavoriteRecords(Integer userId, String type) throws BusinessException {
        // 校验用户ID不为空
        if (userId == null) {
            log.warn("{}", HttpCodeEnum.PARAM_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 校验类型不为空
        if (type == null || type.trim().isEmpty()) {
            log.warn("{} | userId: {}", HttpCodeEnum.PARAM_ERROR.getMsg(), userId);
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 查询记录
        LambdaQueryWrapper<ContentLikeFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentLikeFavorite::getUserId, userId)
                .eq(ContentLikeFavorite::getType, type)
                .isNull(ContentLikeFavorite::getDeletedAt);
        List<ContentLikeFavorite> list = list(queryWrapper);
        
        log.info("查询点赞收藏记录 | userId: {} | type: {} | count: {}", userId, type, list.size());

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
    public boolean readAll(Integer userId, String type) throws BusinessException {
        // 校验用户ID不为空
        if (userId == null) {
            log.warn("{}", HttpCodeEnum.PARAM_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 校验类型不为空
        if (type == null || type.trim().isEmpty()) {
            log.warn("{} | userId: {}", HttpCodeEnum.PARAM_ERROR.getMsg(), userId);
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
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
        boolean result = updateBatchById(list);
        log.info("标记全部已读 | userId: {} | type: {} | count: {} | result: {}", 
            userId, type, list.size(), result);
        return result;
    }

    @Override
    public List<ContentLikeFavoriteVO> getUnreadLikeFavorite(Integer userId, String type) throws BusinessException {
        // 校验用户ID不为空
        if (userId == null) {
            log.warn("{}", HttpCodeEnum.PARAM_ERROR.getMsg());
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 校验类型不为空
        if (type == null || type.trim().isEmpty()) {
            log.warn("{} | userId: {}", HttpCodeEnum.PARAM_ERROR.getMsg(), userId);
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }

        // 查询未读记录
        LambdaQueryWrapper<ContentLikeFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentLikeFavorite::getUserId, userId)
                .eq(ContentLikeFavorite::getType, type)
                .eq(ContentLikeFavorite::getIsRead, 0)
                .isNull(ContentLikeFavorite::getDeletedAt);
        List<ContentLikeFavorite> list = list(queryWrapper);
        
        log.info("查询未读点赞收藏 | userId: {} | type: {} | count: {}", userId, type, list.size());

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
