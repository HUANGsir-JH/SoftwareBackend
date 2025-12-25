package org.software.content.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.software.content.mapper.CommentsMapper;
import org.software.feign.UserFeignClient;
import org.software.model.Response;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.content.dto.CommentDTO;
import org.software.model.content.vo.CommentChildVO;
import org.software.model.content.vo.CommentUnreadVO;
import org.software.model.content.vo.CommentVO;
import org.software.model.exception.BusinessException;
import org.software.model.interaction.comment.Comments;
import org.software.model.page.PageResult;
import org.software.model.user.UserStatusV;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CommentsServiceImpl 白盒测试
 * 测试重点：评论管理核心业务逻辑分支覆盖
 */
@ExtendWith(MockitoExtension.class)
class CommentsServiceImplTest {

    private CommentsMapper commentsMapper;
    private UserFeignClient userFeignClient;
    private CommentsServiceImpl commentsService;

    private Comments testComment;
    private CommentDTO commentDTO;

    @BeforeEach
    void setUp() {
        // 创建 Mock 对象
        commentsMapper = mock(CommentsMapper.class);
        userFeignClient = mock(UserFeignClient.class);

        // 手动创建 CommentsServiceImpl 并注入依赖
        commentsService = new CommentsServiceImpl() {
            @Override
            public boolean save(Comments entity) {
                int result = commentsMapper.insert(entity);
                return result > 0;
            }

            @Override
            public Comments getOne(LambdaQueryWrapper<Comments> queryWrapper) {
                return commentsMapper.selectOne(queryWrapper);
            }

            @Override
            public List<Comments> list(LambdaQueryWrapper<Comments> queryWrapper) {
                return commentsMapper.selectList(queryWrapper);
            }

            @Override
            public long count(LambdaQueryWrapper<Comments> queryWrapper) {
                return commentsMapper.selectCount(queryWrapper);
            }

            @Override
            public Page<Comments> page(Page<Comments> page, LambdaQueryWrapper<Comments> queryWrapper) {
                return commentsMapper.selectPage(page, queryWrapper);
            }

            @Override
            public Comments getById(java.io.Serializable id) {
                return commentsMapper.selectById(id);
            }

            @Override
            public boolean updateById(Comments entity) {
                int result = commentsMapper.updateById(entity);
                return result > 0;
            }

            @Override
            public boolean update(Comments entity, LambdaQueryWrapper<Comments> updateWrapper) {
                int result = commentsMapper.update(entity, updateWrapper);
                return result > 0;
            }
        };

        // 使用反射注入 Mock 依赖
        try {
            java.lang.reflect.Field commentsMapperField = CommentsServiceImpl.class.getDeclaredField("commentsMapper");
            commentsMapperField.setAccessible(true);
            commentsMapperField.set(commentsService, commentsMapper);

            java.lang.reflect.Field userFeignClientField = CommentsServiceImpl.class.getDeclaredField("userFeignClient");
            userFeignClientField.setAccessible(true);
            userFeignClientField.set(commentsService, userFeignClient);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }

        // 准备测试数据
        testComment = Comments.builder()
                .commentId(1L)
                .contentId(100L)
                .userId(1L)
                .parentCommentId(0L)
                .rootCommentId(0L)
                .content("测试评论")
                .isRead(0)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        commentDTO = new CommentDTO();
        commentDTO.setContentId(100L);
        commentDTO.setContent("测试评论内容");
    }

    /**
     * 测试添加评论 - 正常流程（根评论）
     */
    @Test
    void testAddComment_RootComment_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            // Mock 插入评论
            when(commentsMapper.insert(any(Comments.class))).thenAnswer(invocation -> {
                Comments comment = invocation.getArgument(0);
                comment.setCommentId(1L);
                assertEquals(0L, comment.getParentCommentId());
                assertEquals(0L, comment.getRootCommentId());
                return 1;
            });

            // 执行添加评论
            Long commentId = commentsService.addComment(commentDTO);

            // 验证结果
            assertNotNull(commentId);
            assertEquals(1L, commentId);

            // 验证调用
            verify(commentsMapper, times(1)).insert(any(Comments.class));
        }
    }

    /**
     * 测试添加评论 - 子评论（父评论是根评论）
     */
    @Test
    void testAddComment_ChildOfRootComment_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(2L);

            commentDTO.setParentCommentId(1L);

            // Mock 查询父评论（父评论是根评论）
            Comments parentComment = Comments.builder()
                    .commentId(1L)
                    .userId(1L)
                    .rootCommentId(0L)
                    .build();
            when(commentsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(parentComment);

            // Mock 插入评论
            when(commentsMapper.insert(any(Comments.class))).thenAnswer(invocation -> {
                Comments comment = invocation.getArgument(0);
                comment.setCommentId(2L);
                assertEquals(1L, comment.getRootCommentId());
                assertEquals(1L, comment.getToUserId());
                return 1;
            });

            // 执行添加评论
            Long commentId = commentsService.addComment(commentDTO);

            // 验证结果
            assertNotNull(commentId);
            assertEquals(2L, commentId);
        }
    }

    /**
     * 测试添加评论 - 子评论（父评论不是根评论）
     */
    @Test
    void testAddComment_ChildOfChildComment_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(3L);

            commentDTO.setParentCommentId(2L);

            // Mock 查询父评论（父评论不是根评论）
            Comments parentComment = Comments.builder()
                    .commentId(2L)
                    .userId(2L)
                    .rootCommentId(1L)
                    .build();
            when(commentsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(parentComment);

            // Mock 插入评论
            when(commentsMapper.insert(any(Comments.class))).thenAnswer(invocation -> {
                Comments comment = invocation.getArgument(0);
                comment.setCommentId(3L);
                assertEquals(1L, comment.getRootCommentId());
                assertEquals(2L, comment.getToUserId());
                return 1;
            });

            // 执行添加评论
            Long commentId = commentsService.addComment(commentDTO);

            // 验证结果
            assertNotNull(commentId);
            assertEquals(3L, commentId);
        }
    }

    /**
     * 测试添加评论 - 内容ID为空
     */
    @Test
    void testAddComment_ContentIdNull() {
        commentDTO.setContentId(null);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentsService.addComment(commentDTO);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试添加评论 - 评论内容为空
     */
    @Test
    void testAddComment_ContentEmpty() {
        commentDTO.setContent("");

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentsService.addComment(commentDTO);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试添加评论 - 评论内容为null
     */
    @Test
    void testAddComment_ContentNull() {
        commentDTO.setContent(null);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentsService.addComment(commentDTO);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试添加评论 - 父评论不存在
     */
    @Test
    void testAddComment_ParentCommentNotFound() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(2L);

            commentDTO.setParentCommentId(999L);

            // Mock 查询父评论返回null
            when(commentsMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                commentsService.addComment(commentDTO);
            });

            assertEquals(HttpCodeEnum.PARENT_COMMENT_NOT_FOUND.getCode(), exception.getCode());
        }
    }

    /**
     * 测试获取根评论 - 正常流程
     */
    @Test
    void testGetRootComments_Success() {
        Long contentId = 100L;

        // Mock 查询根评论
        List<Comments> rootComments = new ArrayList<>();
        rootComments.add(testComment);
        when(commentsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(rootComments);

        // Mock 用户信息查询
        UserStatusV userStatusV = new UserStatusV();
        userStatusV.setUserId(1L);
        userStatusV.setNickname("测试用户");
        when(userFeignClient.getUser(anyLong())).thenReturn(Response.success(userStatusV));

        // Mock 子评论数量查询
        when(commentsMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        // 执行查询
        List<CommentVO> result = commentsService.getRootComments(contentId);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.size());

        // 验证调用
        verify(commentsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取根评论 - 内容ID为空
     */
    @Test
    void testGetRootComments_ContentIdNull() {
        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentsService.getRootComments(null);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试获取子评论 - 正常流程
     */
    @Test
    void testGetChildComments_Success() {
        Integer rootCommentId = 1;
        Integer pageNum = 1;
        Integer pageSize = 10;

        // Mock 父评论存在检查
        when(commentsMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // Mock 分页查询子评论
        List<Comments> childComments = new ArrayList<>();
        Comments childComment = Comments.builder()
                .commentId(2L)
                .contentId(100L)
                .userId(2L)
                .parentCommentId(1L)
                .rootCommentId(1L)
                .toUserId(1L)
                .content("子评论")
                .createdAt(new Date())
                .build();
        childComments.add(childComment);

        Page<Comments> page = new Page<>(pageNum, pageSize);
        page.setRecords(childComments);
        page.setTotal(1);

        when(commentsMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // Mock 用户信息查询
        UserStatusV userStatusV = new UserStatusV();
        userStatusV.setUserId(2L);
        userStatusV.setNickname("测试用户2");
        when(userFeignClient.getUser(anyLong())).thenReturn(Response.success(userStatusV));

        // 执行查询
        PageResult result = commentsService.getChildComments(rootCommentId, pageNum, pageSize);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());

        // 验证调用
        verify(commentsMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取子评论 - 父评论ID为空
     */
    @Test
    void testGetChildComments_ParentIdNull() {
        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentsService.getChildComments(null, 1, 10);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试获取子评论 - 父评论不存在
     */
    @Test
    void testGetChildComments_ParentNotFound() {
        Integer rootCommentId = 999;

        // Mock 父评论不存在
        when(commentsMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentsService.getChildComments(rootCommentId, 1, 10);
        });

        assertEquals(HttpCodeEnum.PARENT_COMMENT_NOT_FOUND.getCode(), exception.getCode());
    }

    /**
     * 测试获取未读评论 - 正常流程
     */
    @Test
    void testGetUnreadComments_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            // Mock 查询未读评论
            List<Comments> unreadComments = new ArrayList<>();
            Comments unreadComment = Comments.builder()
                    .commentId(2L)
                    .contentId(100L)
                    .userId(2L)
                    .toUserId(1L)
                    .content("未读评论")
                    .isRead(0)
                    .createdAt(new Date())
                    .build();
            unreadComments.add(unreadComment);

            when(commentsMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(unreadComments);

            // Mock 用户信息查询
            UserStatusV userStatusV = new UserStatusV();
            userStatusV.setUserId(2L);
            userStatusV.setNickname("测试用户2");
            when(userFeignClient.getUser(anyLong())).thenReturn(Response.success(userStatusV));

            // 执行查询
            List<CommentUnreadVO> result = commentsService.getUnreadComments();

            // 验证结果
            assertNotNull(result);
            assertEquals(1, result.size());

            // 验证调用
            verify(commentsMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        }
    }

    /**
     * 测试获取未读评论数量 - 正常流程
     */
    @Test
    void testGetUnreadCommentCount_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1L);

            // Mock 统计未读评论数量
            when(commentsMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            // 执行统计
            Long count = commentsService.getUnreadCommentCount();

            // 验证结果
            assertEquals(5L, count);

            // 验证调用
            verify(commentsMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        }
    }

    /**
     * 测试删除评论 - 删除根评论
     */
    @Test
    void testDeleteComments_RootComment_Success() {
        Long commentId = 1L;

        // Mock 查询评论
        when(commentsMapper.selectById(commentId)).thenReturn(testComment);

        // Mock 更新评论
        when(commentsMapper.update(any(Comments.class), any(LambdaQueryWrapper.class))).thenReturn(1);

        // 执行删除
        assertDoesNotThrow(() -> commentsService.deleteComments(commentId));

        // 验证调用
        verify(commentsMapper, times(1)).selectById(commentId);
        verify(commentsMapper, times(1)).update(any(Comments.class), any(LambdaQueryWrapper.class));
    }

    /**
     * 测试删除评论 - 删除子评论
     */
    @Test
    void testDeleteComments_ChildComment_Success() {
        Long commentId = 2L;

        // Mock 查询评论（子评论）
        Comments childComment = Comments.builder()
                .commentId(2L)
                .parentCommentId(1L)
                .rootCommentId(1L)
                .contentId(100L)
                .userId(2L)
                .content("子评论")
                .build();
        when(commentsMapper.selectById(commentId)).thenReturn(childComment);

        // Mock 更新评论
        when(commentsMapper.updateById(any(Comments.class))).thenReturn(1);

        // 执行删除
        assertDoesNotThrow(() -> commentsService.deleteComments(commentId));

        // 验证调用
        verify(commentsMapper, times(1)).selectById(commentId);
        verify(commentsMapper, times(1)).updateById(any(Comments.class));
    }

    /**
     * 测试删除评论 - 评论ID为空
     */
    @Test
    void testDeleteComments_CommentIdNull() {
        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentsService.deleteComments(null);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试删除评论 - 评论不存在
     */
    @Test
    void testDeleteComments_CommentNotFound() {
        Long commentId = 999L;

        // Mock 查询评论返回null
        when(commentsMapper.selectById(commentId)).thenReturn(null);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentsService.deleteComments(commentId);
        });

        assertEquals(HttpCodeEnum.COMMENT_NOT_FOUND.getCode(), exception.getCode());
    }

    /**
     * 测试删除评论 - 评论已删除
     */
    @Test
    void testDeleteComments_CommentAlreadyDeleted() {
        Long commentId = 1L;

        // Mock 查询评论（已删除）
        Comments deletedComment = Comments.builder()
                .commentId(1L)
                .contentId(100L)
                .deletedAt(new Date())
                .build();
        when(commentsMapper.selectById(commentId)).thenReturn(deletedComment);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentsService.deleteComments(commentId);
        });

        assertEquals(HttpCodeEnum.COMMENT_NOT_FOUND.getCode(), exception.getCode());
    }
}
