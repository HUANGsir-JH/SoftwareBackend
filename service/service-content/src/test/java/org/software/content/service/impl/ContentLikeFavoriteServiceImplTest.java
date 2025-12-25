package org.software.content.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.software.content.mapper.ContentLikeFavoriteMapper;
import org.software.content.mapper.ContentMapper;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.content.Content;
import org.software.model.content.vo.ContentLikeFavoriteVO;
import org.software.model.exception.BusinessException;
import org.software.model.interaction.ContentLikeFavorite;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ContentLikeFavoriteServiceImpl 白盒测试
 * 测试重点：点赞收藏管理核心业务逻辑分支覆盖
 */
@ExtendWith(MockitoExtension.class)
class ContentLikeFavoriteServiceImplTest {

    private ContentLikeFavoriteMapper contentLikeFavoriteMapper;
    private ContentMapper contentMapper;
    private ContentLikeFavoriteServiceImpl contentLikeFavoriteService;

    private ContentLikeFavorite testLikeFavorite;
    private Content testContent;

    @BeforeEach
    void setUp() {
        // 创建 Mock 对象
        contentLikeFavoriteMapper = mock(ContentLikeFavoriteMapper.class);
        contentMapper = mock(ContentMapper.class);

        // 手动创建 ContentLikeFavoriteServiceImpl 并注入依赖
        contentLikeFavoriteService = new ContentLikeFavoriteServiceImpl() {
            @Override
            public ContentLikeFavorite getOne(LambdaQueryWrapper<ContentLikeFavorite> queryWrapper) {
                return contentLikeFavoriteMapper.selectOne(queryWrapper);
            }

            @Override
            public boolean save(ContentLikeFavorite entity) {
                int result = contentLikeFavoriteMapper.insert(entity);
                return result > 0;
            }

            @Override
            public boolean updateById(ContentLikeFavorite entity) {
                int result = contentLikeFavoriteMapper.updateById(entity);
                return result > 0;
            }

            @Override
            public Page<ContentLikeFavorite> page(Page<ContentLikeFavorite> page, LambdaQueryWrapper<ContentLikeFavorite> queryWrapper) {
                return contentLikeFavoriteMapper.selectPage(page, queryWrapper);
            }

            @Override
            public List<ContentLikeFavorite> list(LambdaQueryWrapper<ContentLikeFavorite> queryWrapper) {
                return contentLikeFavoriteMapper.selectList(queryWrapper);
            }

            @Override
            public boolean updateBatchById(java.util.Collection<ContentLikeFavorite> entityList) {
                entityList.forEach(entity -> contentLikeFavoriteMapper.updateById(entity));
                return true;
            }
        };

        // 使用反射注入 Mock 依赖
        try {
            java.lang.reflect.Field contentLikeFavoriteMapperField = ContentLikeFavoriteServiceImpl.class.getDeclaredField("contentLikeFavoriteMapper");
            contentLikeFavoriteMapperField.setAccessible(true);
            contentLikeFavoriteMapperField.set(contentLikeFavoriteService, contentLikeFavoriteMapper);

            java.lang.reflect.Field contentMapperField = ContentLikeFavoriteServiceImpl.class.getDeclaredField("contentMapper");
            contentMapperField.setAccessible(true);
            contentMapperField.set(contentLikeFavoriteService, contentMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }

        // 准备测试数据
        testLikeFavorite = ContentLikeFavorite.builder()
                .likeId(1L)
                .contentId(100L)
                .userId(1L)
                .type("like")
                .isRead(0)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        testContent = Content.builder()
                .contentId(100L)
                .likeCount(10)
                .favoriteCount(5)
                .build();
    }

    /**
     * 测试添加/取消点赞 - 新增点赞
     */
    @Test
    void testAddOrCancelLike_AddNew_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            Integer contentId = 100;
            String type = "like";

            // Mock 查询不存在的记录
            when(contentLikeFavoriteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Mock 插入新记录
            when(contentLikeFavoriteMapper.insert(any(ContentLikeFavorite.class))).thenReturn(1);

            // Mock 更新内容计数器
            when(contentMapper.selectById(anyLong())).thenReturn(testContent);
            when(contentMapper.updateById(any(Content.class))).thenReturn(1);

            // 执行添加点赞
            boolean result = contentLikeFavoriteService.addOrCancelLike(contentId, type);

            // 验证结果
            assertTrue(result);

            // 验证调用
            verify(contentLikeFavoriteMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
            verify(contentLikeFavoriteMapper, times(1)).insert(any(ContentLikeFavorite.class));
            verify(contentMapper, times(1)).updateById(any(Content.class));
        }
    }

    /**
     * 测试添加/取消点赞 - 取消点赞
     */
    @Test
    void testAddOrCancelLike_Cancel_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            Integer contentId = 100;
            String type = "like";

            // Mock 查询已存在且未删除的记录
            ContentLikeFavorite existingLike = ContentLikeFavorite.builder()
                    .likeId(1L)
                    .contentId(100L)
                    .userId(1L)
                    .type("like")
                    .deletedAt(null)
                    .build();
            when(contentLikeFavoriteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingLike);

            // Mock 更新记录
            when(contentLikeFavoriteMapper.updateById(any(ContentLikeFavorite.class))).thenReturn(1);

            // Mock 更新内容计数器
            when(contentMapper.selectById(anyLong())).thenReturn(testContent);
            when(contentMapper.updateById(any(Content.class))).thenReturn(1);

            // 执行取消点赞
            boolean result = contentLikeFavoriteService.addOrCancelLike(contentId, type);

            // 验证结果
            assertTrue(result);

            // 验证调用
            verify(contentLikeFavoriteMapper, times(1)).updateById(any(ContentLikeFavorite.class));
            verify(contentMapper, times(1)).updateById(any(Content.class));
        }
    }

    /**
     * 测试添加/取消点赞 - 恢复点赞
     */
    @Test
    void testAddOrCancelLike_Restore_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            Integer contentId = 100;
            String type = "like";

            // Mock 查询已软删除的记录
            ContentLikeFavorite existingLike = ContentLikeFavorite.builder()
                    .likeId(1L)
                    .contentId(100L)
                    .userId(1L)
                    .type("like")
                    .deletedAt(new Date())
                    .build();
            when(contentLikeFavoriteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingLike);

            // Mock 更新记录
            when(contentLikeFavoriteMapper.updateById(any(ContentLikeFavorite.class))).thenReturn(1);

            // Mock 更新内容计数器
            when(contentMapper.selectById(anyLong())).thenReturn(testContent);
            when(contentMapper.updateById(any(Content.class))).thenReturn(1);

            // 执行恢复点赞
            boolean result = contentLikeFavoriteService.addOrCancelLike(contentId, type);

            // 验证结果
            assertTrue(result);

            // 验证调用
            verify(contentLikeFavoriteMapper, times(1)).updateById(any(ContentLikeFavorite.class));
        }
    }

    /**
     * 测试添加/取消点赞 - 内容ID为空
     */
    @Test
    void testAddOrCancelLike_ContentIdNull() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                contentLikeFavoriteService.addOrCancelLike(null, "like");
            });

            assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
        }
    }

    /**
     * 测试添加/取消点赞 - 类型为空
     */
    @Test
    void testAddOrCancelLike_TypeNull() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                contentLikeFavoriteService.addOrCancelLike(100, null);
            });

            assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
        }
    }

    /**
     * 测试添加/取消收藏 - 新增收藏
     */
    @Test
    void testAddOrCancelFavorite_AddNew_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            Integer contentId = 100;
            String type = "favorite";

            // Mock 查询不存在的记录
            when(contentLikeFavoriteMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // Mock 插入新记录
            when(contentLikeFavoriteMapper.insert(any(ContentLikeFavorite.class))).thenReturn(1);

            // Mock 更新内容计数器
            when(contentMapper.selectById(anyLong())).thenReturn(testContent);
            when(contentMapper.updateById(any(Content.class))).thenReturn(1);

            // 执行添加收藏
            boolean result = contentLikeFavoriteService.addOrCancelLike(contentId, type);

            // 验证结果
            assertTrue(result);

            // 验证调用
            verify(contentLikeFavoriteMapper, times(1)).insert(any(ContentLikeFavorite.class));
        }
    }

    /**
     * 测试获取点赞收藏记录 - 正常流程
     */
    @Test
    void testGetLikeFavoriteRecords_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            Integer pageNum = 1;
            Integer pageSize = 10;
            String type = "like";

            // Mock 分页查询
            Page<ContentLikeFavorite> page = new Page<>(pageNum, pageSize);
            List<ContentLikeFavorite> records = new ArrayList<>();
            records.add(testLikeFavorite);
            page.setRecords(records);
            page.setTotal(1);

            when(contentLikeFavoriteMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            // 执行查询
            List<ContentLikeFavoriteVO> result = contentLikeFavoriteService.getLikeFavoriteRecords(pageNum, pageSize, type);

            // 验证结果
            assertNotNull(result);
            assertEquals(1, result.size());

            // 验证调用
            verify(contentLikeFavoriteMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    /**
     * 测试获取点赞收藏记录 - 类型为空
     */
    @Test
    void testGetLikeFavoriteRecords_TypeNull() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                contentLikeFavoriteService.getLikeFavoriteRecords(1, 10, null);
            });

            assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
        }
    }

    /**
     * 测试获取点赞收藏记录 - 类型为空字符串
     */
    @Test
    void testGetLikeFavoriteRecords_TypeEmpty() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                contentLikeFavoriteService.getLikeFavoriteRecords(1, 10, "");
            });

            assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
        }
    }

    /**
     * 测试全部标记已读 - 正常流程
     */
    @Test
    void testReadAll_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            // Mock 查询未读记录
            List<ContentLikeFavorite> unreadList = new ArrayList<>();
            ContentLikeFavorite unread1 = ContentLikeFavorite.builder()
                    .likeId(1L)
                    .userId(1L)
                    .isRead(0)
                    .build();
            ContentLikeFavorite unread2 = ContentLikeFavorite.builder()
                    .likeId(2L)
                    .userId(1L)
                    .isRead(0)
                    .build();
            unreadList.add(unread1);
            unreadList.add(unread2);

            when(contentLikeFavoriteMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(unreadList);

            // Mock 批量更新
            when(contentLikeFavoriteMapper.updateById(any(ContentLikeFavorite.class))).thenReturn(1);

            // 执行全部标记已读
            boolean result = contentLikeFavoriteService.readAll();

            // 验证结果
            assertTrue(result);

            // 验证调用
            verify(contentLikeFavoriteMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
            verify(contentLikeFavoriteMapper, times(2)).updateById(any(ContentLikeFavorite.class));
        }
    }

    /**
     * 测试全部标记已读 - 无未读记录
     */
    @Test
    void testReadAll_NoUnreadRecords() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            // Mock 查询未读记录返回空
            when(contentLikeFavoriteMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            // 执行全部标记已读
            boolean result = contentLikeFavoriteService.readAll();

            // 验证结果
            assertTrue(result);

            // 验证未调用更新
            verify(contentLikeFavoriteMapper, never()).updateById(any(ContentLikeFavorite.class));
        }
    }

    /**
     * 测试获取未读点赞收藏 - 正常流程
     */
    @Test
    void testGetUnreadLikeFavorite_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            Integer pageNum = 1;
            Integer pageSize = 10;
            String type = "like";

            // Mock 分页查询未读记录
            Page<ContentLikeFavorite> page = new Page<>(pageNum, pageSize);
            List<ContentLikeFavorite> records = new ArrayList<>();
            ContentLikeFavorite unread = ContentLikeFavorite.builder()
                    .likeId(1L)
                    .contentId(100L)
                    .userId(1L)
                    .type("like")
                    .isRead(0)
                    .createdAt(new Date())
                    .build();
            records.add(unread);
            page.setRecords(records);
            page.setTotal(1);

            when(contentLikeFavoriteMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            // 执行查询
            List<ContentLikeFavoriteVO> result = contentLikeFavoriteService.getUnreadLikeFavorite(pageNum, pageSize, type);

            // 验证结果
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(0, result.get(0).getIsRead());

            // 验证调用
            verify(contentLikeFavoriteMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    /**
     * 测试获取未读点赞收藏 - 类型为空
     */
    @Test
    void testGetUnreadLikeFavorite_TypeNull() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                contentLikeFavoriteService.getUnreadLikeFavorite(1, 10, null);
            });

            assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
        }
    }

    /**
     * 测试获取未读点赞收藏 - 类型为空字符串
     */
    @Test
    void testGetUnreadLikeFavorite_TypeEmpty() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsInt).thenReturn(1);

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                contentLikeFavoriteService.getUnreadLikeFavorite(1, 10, "  ");
            });

            assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
        }
    }
}
