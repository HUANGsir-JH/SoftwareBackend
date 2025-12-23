package org.software.content.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.web.bind.annotation.RequestParam;

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
    public boolean addOrCancelLike(Integer contentId, String type) throws BusinessException {
        Integer userId=StpUtil.getLoginIdAsInt();
        // 校验内容ID不为空
        if (contentId == null) {
            log.warn("{} | userId: {}", HttpCodeEnum.PARAM_ERROR.getMsg(), userId);
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }



        // 校验操作类型不为空
        if (type == null) {
            log.warn("{} | userId: {} | contentId: {}", HttpCodeEnum.PARAM_ERROR.getMsg(),
                userId, contentId);
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }



        // 检查是否已存在该记录
        LambdaQueryWrapper<ContentLikeFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentLikeFavorite::getContentId, contentId)
                .eq(ContentLikeFavorite::getUserId, userId)
                .eq(ContentLikeFavorite::getType, type)
                .isNull(ContentLikeFavorite::getDeletedAt);
        ContentLikeFavorite exist = getOne(queryWrapper);

        if (exist == null) {
            // 情况一：从未存在 → 新增
            ContentLikeFavorite like = new ContentLikeFavorite();
            like.setContentId(Long.valueOf(contentId));
            like.setUserId(Long.valueOf(userId));
            like.setType(type);
            like.setIsRead(0);
            like.setCreatedAt(new Date());
            like.setUpdatedAt(new Date());
            like.setDeletedAt(null);

            boolean result = save(like);
            log.info("添加点赞 | likeId: {} | userId: {} | contentId: {} | result: {}",
                    like.getLikeId(), userId, contentId, result);

            updateContentCounter(Long.valueOf(contentId), type, +1);
            return result;

        } else if (exist.getDeletedAt() == null) {
            // 情况二：存在且未删除 → 取消点赞
            exist.setDeletedAt(new Date());
            exist.setUpdatedAt(new Date());

            boolean result = updateById(exist);
            log.info("取消点赞 | likeId: {} | userId: {} | contentId: {} | result: {}",
                    exist.getLikeId(), userId, contentId, result);

            updateContentCounter(Long.valueOf(contentId), type, -1);
            return result;

        } else {
            // 情况三：存在但被软删除 → 恢复点赞
            exist.setDeletedAt(null);
            exist.setUpdatedAt(new Date());

            boolean result = updateById(exist);
            log.info("恢复点赞 | likeId: {} | userId: {} | contentId: {} | result: {}",
                    exist.getLikeId(), userId, contentId, result);

            updateContentCounter(Long.valueOf(contentId), type, +1);
            return result;
        }
    }

    @Override
    public List<ContentLikeFavoriteVO> getLikeFavoriteRecords(Integer pageNum,
                                                              Integer pageSize,
                                                              String type) throws BusinessException {
        Integer userId= StpUtil.getLoginIdAsInt();


        // 校验类型不为空
        if (type == null || type.trim().isEmpty()) {
            log.warn("{} | userId: {}", HttpCodeEnum.PARAM_ERROR.getMsg(), userId);
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }
        Page<ContentLikeFavorite> page = new Page<>(pageNum, pageSize);

        // 查询记录
        LambdaQueryWrapper<ContentLikeFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentLikeFavorite::getUserId, userId)
                .eq(ContentLikeFavorite::getType, type)
                .isNull(ContentLikeFavorite::getDeletedAt);
        Page<ContentLikeFavorite> resultPage = page(page, queryWrapper);

        log.info("分页查询点赞收藏 | userId: {} | type: {} | page: {} | size: {} | total: {}",
                userId, type, pageNum, pageSize, resultPage.getTotal());

        return resultPage.getRecords()
                .stream()
                .map(like -> {
                    ContentLikeFavoriteVO vo = new ContentLikeFavoriteVO();
                    vo.setLikeId(like.getLikeId());
                    vo.setContentId(like.getContentId());
                    vo.setUserId(like.getUserId());
                    vo.setIsRead(like.getIsRead());
                    vo.setType(like.getType());
                    vo.setCreatedAt(like.getCreatedAt());
                    return vo;
                })
                .collect(Collectors.toList());

    }

    @Override
    @Transactional
    public boolean readAll() throws BusinessException {

        Integer userId= StpUtil.getLoginIdAsInt();


        // 查询未读记录并标记为已读
        LambdaQueryWrapper<ContentLikeFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentLikeFavorite::getUserId, userId)
                .eq(ContentLikeFavorite::getIsRead, 0)
                .isNull(ContentLikeFavorite::getDeletedAt);
        List<ContentLikeFavorite> list = list(queryWrapper);

        list.forEach(like -> {
            like.setIsRead(1);
            like.setUpdatedAt(new Date());
        });
        boolean result = updateBatchById(list);
        log.info("标记全部已读 | userId: {} | count: {} | result: {}",
            userId,  list.size(), result);
        return result;
    }

    @Override
    public List<ContentLikeFavoriteVO> getUnreadLikeFavorite(Integer pageNum,
                                                             Integer pageSize,
                                                             String type) throws BusinessException {
        Integer userId= StpUtil.getLoginIdAsInt();


        // 校验类型不为空
        if (type == null || type.trim().isEmpty()) {
            log.warn("{} | userId: {}", HttpCodeEnum.PARAM_ERROR.getMsg(), userId);
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }
        Page<ContentLikeFavorite> page = new Page<>(pageNum, pageSize);

        // 查询未读记录
        LambdaQueryWrapper<ContentLikeFavorite> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContentLikeFavorite::getUserId, userId)
                .eq(ContentLikeFavorite::getType, type)
                .eq(ContentLikeFavorite::getIsRead, 0)
                .isNull(ContentLikeFavorite::getDeletedAt);
        Page<ContentLikeFavorite> resultPage = page(page, queryWrapper);

        log.info("分页查询未读点赞收藏 | userId: {} | type: {} | total: {}",
                userId, type, resultPage.getTotal());

        return resultPage.getRecords()
                .stream()
                .map(like -> {
                    ContentLikeFavoriteVO vo = new ContentLikeFavoriteVO();
                    vo.setLikeId(like.getLikeId());
                    vo.setContentId(like.getContentId());
                    vo.setUserId(like.getUserId());
                    vo.setIsRead(like.getIsRead());
                    vo.setType(like.getType());
                    vo.setCreatedAt(like.getCreatedAt());
                    return vo;
                })
                .collect(Collectors.toList());
    }
}
