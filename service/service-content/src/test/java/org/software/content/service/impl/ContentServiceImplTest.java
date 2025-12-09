package org.software.content.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.software.content.mapper.ContentMapper;
import org.software.content.mapper.ContentMediaMapper;
import org.software.content.mapper.ContentTagMapper;
import org.software.content.mapper.TagMapper;
import org.software.feign.UserFeignClient;
import org.software.model.Response;
import org.software.model.constants.ContentConstants;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.content.Content;
import org.software.model.content.ContentTag;
import org.software.model.content.Tag;
import org.software.model.content.dto.ContentDTO;
import org.software.model.content.vo.ContentDetailVO;
import org.software.model.exception.BusinessException;
import org.software.model.media.ContentMedia;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.user.User;
import org.software.model.user.UserV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ContentServiceImpl 白盒测试
 * 测试重点：核心业务逻辑分支覆盖
 */
@ExtendWith(MockitoExtension.class)
class ContentServiceImplTest {

    @Mock
    private ContentMapper contentMapper;

    @Mock
    private TagMapper tagMapper;

    @Mock
    private ContentTagMapper contentTagMapper;

    @Mock
    private ContentMediaMapper contentMediaMapper;

    @Mock
    private UserFeignClient userFeignClient;

    private ContentServiceImpl contentService;

    private ContentDTO testContentDTO;
    private Content testContent;

    @BeforeEach
    void setUp() {
        // 手动创建 ContentServiceImpl 并注入依赖
        contentService = new ContentServiceImpl() {
            // 重写 save 方法以避免 BaseMapper 为 null 的问题
            @Override
            public boolean save(Content entity) {
                int result = contentMapper.insert(entity);
                return result > 0;
            }
            
            @Override
            public boolean updateById(Content entity) {
                int result = contentMapper.updateById(entity);
                return result > 0;
            }
            
            @Override
            public Content getById(java.io.Serializable id) {
                return contentMapper.selectById(id);
            }
            
            @Override
            public boolean removeById(java.io.Serializable id) {
                int result = contentMapper.deleteById(id);
                return result > 0;
            }
        };
        
        // 使用反射注入 Mock 依赖
        try {
            java.lang.reflect.Field contentMapperField = ContentServiceImpl.class.getDeclaredField("contentMapper");
            contentMapperField.setAccessible(true);
            contentMapperField.set(contentService, contentMapper);
            
            java.lang.reflect.Field tagMapperField = ContentServiceImpl.class.getDeclaredField("tagMapper");
            tagMapperField.setAccessible(true);
            tagMapperField.set(contentService, tagMapper);
            
            java.lang.reflect.Field contentTagMapperField = ContentServiceImpl.class.getDeclaredField("contentTagMapper");
            contentTagMapperField.setAccessible(true);
            contentTagMapperField.set(contentService, contentTagMapper);
            
            java.lang.reflect.Field contentMediaMapperField = ContentServiceImpl.class.getDeclaredField("contentMediaMapper");
            contentMediaMapperField.setAccessible(true);
            contentMediaMapperField.set(contentService, contentMediaMapper);
            
            java.lang.reflect.Field userFeignClientField = ContentServiceImpl.class.getDeclaredField("userFeignClient");
            userFeignClientField.setAccessible(true);
            userFeignClientField.set(contentService, userFeignClient);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
        
        // 准备测试数据
        testContentDTO = new ContentDTO();
        testContentDTO.setContentType("image");
        testContentDTO.setTitle("测试标题");
        testContentDTO.setDescription("测试描述");
        testContentDTO.setIsPublic(1);
        testContentDTO.setStatus(ContentConstants.STATUS_PUBLISH);
        testContentDTO.setMedias(List.of("http://example.com/image1.jpg"));
        testContentDTO.setTags(Arrays.asList(1L, 2L));

        testContent = new Content();
        testContent.setContentId(1L);
        testContent.setUserId(100L);
        testContent.setContentType("image");
        testContent.setTitle("测试标题");
        testContent.setDescription("测试描述");
        testContent.setIsPublic(1);
        testContent.setStatus(ContentConstants.STATUS_PUBLISH);
        testContent.setCoverUrl("http://example.com/image1.jpg");
        testContent.setLikeCount(0);
        testContent.setFavoriteCount(0);
        testContent.setCommentCount(0);
        testContent.setCreatedAt(new Date());
        testContent.setUpdatedAt(new Date());
    }

    /**
     * 测试创建内容 - 正常流程（包含标签和媒体）
     */
    @Test
    void testCreate_Success_WithTagsAndMedias() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class);
             MockedStatic<BeanUtil> beanUtilMock = mockStatic(BeanUtil.class)) {

            // Mock 用户登录
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            // Mock BeanUtil转换
            Content mockContent = new Content();
            mockContent.setContentType(testContentDTO.getContentType());
            mockContent.setTitle(testContentDTO.getTitle());
            mockContent.setDescription(testContentDTO.getDescription());
            mockContent.setIsPublic(testContentDTO.getIsPublic());
            mockContent.setStatus(testContentDTO.getStatus());
            beanUtilMock.when(() -> BeanUtil.toBean(any(ContentDTO.class), eq(Content.class)))
                        .thenReturn(mockContent);

            // Mock 标签验证
            when(tagMapper.selectCount(any(QueryWrapper.class))).thenReturn(2L);

            // Mock content保存，模拟自动生成ID
            when(contentMapper.insert(any(Content.class))).thenAnswer(invocation -> {
                Content content = invocation.getArgument(0);
                content.setContentId(1L);
                return 1;
            });

            // 执行创建
            Long contentId = contentService.create(testContentDTO);

            // 验证结果
            assertNotNull(contentId);
            assertEquals(1L, contentId);

            // 验证调用
            verify(contentMapper, times(1)).insert(any(Content.class));
            verify(contentMapper, times(1)).batchInsertTags(eq(1L), anyList());
            verify(contentMapper, times(1)).batchInsertMedias(eq(1L), anyList());
        }
    }

    /**
     * 测试创建内容 - 标签不存在抛出异常
     */
    @Test
    void testCreate_InvalidTag_ThrowsException() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class);
             MockedStatic<BeanUtil> beanUtilMock = mockStatic(BeanUtil.class)) {

            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            // Mock BeanUtil转换
            Content mockContent = new Content();
            mockContent.setContentType(testContentDTO.getContentType());
            mockContent.setTitle(testContentDTO.getTitle());
            beanUtilMock.when(() -> BeanUtil.toBean(any(ContentDTO.class), eq(Content.class)))
                        .thenReturn(mockContent);

            // Mock 标签验证失败（只找到1个标签，但提交了2个）
            when(tagMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);

            when(contentMapper.insert(any(Content.class))).thenAnswer(invocation -> {
                Content content = invocation.getArgument(0);
                content.setContentId(1L);
                return 1;
            });

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                contentService.create(testContentDTO);
            });

            assertEquals(HttpCodeEnum.INVALID_TAG.getCode(), exception.getCode());
        }
    }

    /**
     * 测试创建内容 - 无标签和媒体（测试分支覆盖）
     */
    @Test
    void testCreate_NoTagsAndMedias() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class);
             MockedStatic<BeanUtil> beanUtilMock = mockStatic(BeanUtil.class)) {

            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            testContentDTO.setTags(null);
            testContentDTO.setMedias(null);

            // Mock BeanUtil转换
            Content mockContent = new Content();
            mockContent.setContentType(testContentDTO.getContentType());
            mockContent.setTitle(testContentDTO.getTitle());
            beanUtilMock.when(() -> BeanUtil.toBean(any(ContentDTO.class), eq(Content.class)))
                        .thenReturn(mockContent);

            when(contentMapper.insert(any(Content.class))).thenAnswer(invocation -> {
                Content content = invocation.getArgument(0);
                content.setContentId(1L);
                return 1;
            });

            Long contentId = contentService.create(testContentDTO);

            assertNotNull(contentId);
            // 验证未调用标签和媒体批量插入
            verify(contentMapper, never()).batchInsertTags(anyLong(), anyList());
            verify(contentMapper, never()).batchInsertMedias(anyLong(), anyList());
        }
    }

    /**
     * 测试更新内容 - 正常流程
     */
    @Test
    void testUpdatePost_Success() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class);
             MockedStatic<BeanUtil> beanUtilMock = mockStatic(BeanUtil.class)) {

            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            testContentDTO.setContentId(1L);
            testContentDTO.setTitle("更新后的标题");

            // Mock 查询原内容
            when(contentMapper.selectById(1L)).thenReturn(testContent);

            // Mock BeanUtil转换
            Content mockContent = new Content();
            mockContent.setContentId(1L);
            mockContent.setTitle("更新后的标题");
            mockContent.setContentType(testContentDTO.getContentType());
            beanUtilMock.when(() -> BeanUtil.toBean(any(ContentDTO.class), eq(Content.class)))
                        .thenReturn(mockContent);

            // Mock 标签验证
            when(tagMapper.selectCount(any(QueryWrapper.class))).thenReturn(2L);

            // Mock 更新操作
            when(contentMapper.updateById(any(Content.class))).thenReturn(1);

            // 执行更新
            assertDoesNotThrow(() -> contentService.updatePost(testContentDTO));

            // 验证调用
            verify(contentMapper, times(1)).updateById(any(Content.class));
            verify(contentMapper, times(1)).deleteTagsByContentId(1L);
            verify(contentMapper, times(1)).batchInsertTags(eq(1L), anyList());
        }
    }

    /**
     * 测试更新内容 - 内容不存在
     */
    @Test
    void testUpdatePost_ContentNotFound() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(100L);

            testContentDTO.setContentId(999L);

            // Mock 查询不到原内容
            when(contentMapper.selectById(999L)).thenReturn(null);

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                contentService.updatePost(testContentDTO);
            });

            assertEquals(HttpCodeEnum.CONTENT_NOT_FOUND.getCode(), exception.getCode());
        }
    }

    /**
     * 测试更新内容 - 无权限修改
     */
    @Test
    void testUpdatePost_NoPermission() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(999L); // 不同的用户ID

            testContentDTO.setContentId(1L);

            // Mock 查询原内容（userId=100）
            when(contentMapper.selectById(1L)).thenReturn(testContent);

            // 验证异常
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                contentService.updatePost(testContentDTO);
            });

            assertEquals(HttpCodeEnum.NO_PERMISSION.getCode(), exception.getCode());
        }
    }

    /**
     * 测试分页查询 - 指定标签过滤
     */
    @Test
    void testGetAllContent_WithTagFilter() {
        try (MockedStatic<BeanUtil> beanUtilMock = mockStatic(BeanUtil.class)) {
            PageQuery pageQuery = new PageQuery(1, 10);
            Long tagId = 1L;

            // Mock 标签关联查询
            List<ContentTag> contentTags = Arrays.asList(
                createContentTag(1L, 1L),
                createContentTag(2L, 1L)
            );
            when(contentTagMapper.selectList(any(QueryWrapper.class))).thenReturn(contentTags);

            // Mock 分页查询
            Page<Content> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testContent));
            page.setTotal(1);
            when(contentMapper.selectPage(any(Page.class), any(QueryWrapper.class))).thenReturn(page);

            // Mock 用户信息
            User user = new User();
            user.setUserId(100L);
            user.setUsername("testuser");
            Response response = Response.success(user);
            when(userFeignClient.getUser(100L)).thenReturn(response);

            // Mock BeanUtil转换
            beanUtilMock.when(() -> BeanUtil.toBean(any(User.class), any()))
                        .thenReturn(new UserV());

            // 执行查询
            PageResult result = contentService.getAllContent(pageQuery, tagId);

            // 验证结果
            assertNotNull(result);
            assertEquals(1, result.getTotal());
            verify(contentTagMapper, times(1)).selectList(any(QueryWrapper.class));
        }
    }

    /**
     * 测试分页查询 - 标签无关联内容返回空结果
     */
    @Test
    void testGetAllContent_TagWithNoContent() {
        PageQuery pageQuery = new PageQuery(1, 10);
        Long tagId = 999L;

        // Mock 标签关联查询为空
        when(contentTagMapper.selectList(any(QueryWrapper.class))).thenReturn(new ArrayList<>());

        // 执行查询
        PageResult result = contentService.getAllContent(pageQuery, tagId);

        // 验证返回空结果
        assertNotNull(result);
        assertEquals(0, result.getTotal());

        // 验证未调用内容查询
        verify(contentMapper, never()).selectPage(any(Page.class), any(QueryWrapper.class));
    }

    /**
     * 测试删除内容 - 级联删除
     */
    @Test
    void testRemove_CascadeDelete() {
        Long contentId = 1L;

        // Mock 删除操作
        when(contentMapper.deleteById(contentId)).thenReturn(1);

        // 执行删除
        contentService.remove(contentId);

        // 验证级联删除调用
        verify(contentMapper, times(1)).deleteById(contentId);
        verify(contentMapper, times(1)).deleteTagsByContentId(contentId);
        verify(contentMapper, times(1)).deleteMediasByContentId(contentId);
    }

    /**
     * 测试查看内容详情
     */
    @Test
    void testViewContent_Success() {
        try (MockedStatic<BeanUtil> beanUtilMock = mockStatic(BeanUtil.class)) {
            Long contentId = 1L;

            // Mock 内容查询
            when(contentMapper.selectById(contentId)).thenReturn(testContent);

            // Mock BeanUtil转换
            ContentDetailVO mockDetailVO = new ContentDetailVO();
            mockDetailVO.setContentId(contentId);
            mockDetailVO.setUserId(100L);
            beanUtilMock.when(() -> BeanUtil.toBean(any(Content.class), eq(ContentDetailVO.class)))
                        .thenReturn(mockDetailVO);

            // Mock 用户信息
            User user = new User();
            user.setUserId(100L);
            user.setUsername("testuser");
            Response response = Response.success(user);
            when(userFeignClient.getUser(100L)).thenReturn(response);

            // Mock BeanUtil转换用户
            beanUtilMock.when(() -> BeanUtil.toBean(any(User.class), any()))
                        .thenReturn(new UserV());

            // Mock 标签查询
            List<Tag> tags = Arrays.asList(
                createTag(1L, "标签1"),
                createTag(2L, "标签2")
            );
            when(tagMapper.listByContentId(contentId)).thenReturn(tags);

            // Mock 媒体查询
            List<ContentMedia> medias = Arrays.asList(new ContentMedia());
            when(contentMediaMapper.selectList(any(QueryWrapper.class))).thenReturn(medias);

            // 执行查询
            ContentDetailVO result = contentService.viewContent(contentId);

            // 验证结果
            assertNotNull(result);
            assertNotNull(result.getUser());
            assertNotNull(result.getTags());
            assertNotNull(result.getMedias());
            assertEquals(2, result.getTags().size());
        }
    }

    // 辅助方法
    private ContentTag createContentTag(Long contentId, Long tagId) {
        ContentTag ct = new ContentTag();
        ct.setContentId(contentId);
        ct.setTagId(tagId);
        return ct;
    }

    private Tag createTag(Long tagId, String tagName) {
        Tag tag = new Tag();
        tag.setTagId(tagId);
        tag.setTagName(tagName);
        return tag;
    }
}
