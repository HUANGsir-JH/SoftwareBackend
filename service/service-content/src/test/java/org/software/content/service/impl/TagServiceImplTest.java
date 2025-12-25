package org.software.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.software.content.mapper.TagMapper;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.content.Tag;
import org.software.model.content.dto.TagDTO;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TagServiceImpl 白盒测试
 * 测试重点：标签管理核心业务逻辑分支覆盖
 */
@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    private TagMapper tagMapper;
    private TagServiceImpl tagService;

    private Tag testTag;
    private TagDTO tagDTO;

    @BeforeEach
    void setUp() {
        // 创建 Mock 对象
        tagMapper = mock(TagMapper.class);

        // 手动创建 TagServiceImpl 并注入依赖
        tagService = new TagServiceImpl() {
            @Override
            public long count(Wrapper<Tag> queryWrapper) {
                return tagMapper.selectCount(queryWrapper);
            }

            @Override
            public boolean save(Tag entity) {
                int result = tagMapper.insert(entity);
                return result > 0;
            }

            @Override
            public Tag getById(java.io.Serializable id) {
                return tagMapper.selectById(id);
            }

            @Override
            public boolean updateById(Tag entity) {
                int result = tagMapper.updateById(entity);
                return result > 0;
            }
        };

        // 使用反射注入 Mock 依赖
        try {
            java.lang.reflect.Field tagMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
            tagMapperField.setAccessible(true);
            tagMapperField.set(tagService, tagMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }

        // 准备测试数据
        testTag = Tag.builder()
                .tagId(1L)
                .tagName("测试标签")
                .isActive(1)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        tagDTO = new TagDTO();
        tagDTO.setTagName("新标签");
        tagDTO.setIsActive(1);
    }

    /**
     * 测试添加标签 - 正常流程
     */
    @Test
    void testAddTag_Success() {
        // Mock 标签名称不重复
        when(tagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // Mock 插入标签
        when(tagMapper.insert(any(Tag.class))).thenAnswer(invocation -> {
            Tag tag = invocation.getArgument(0);
            tag.setTagId(1L);
            return 1;
        });

        // 执行添加标签
        boolean result = tagService.addTag(tagDTO);

        // 验证结果
        assertTrue(result);

        // 验证调用
        verify(tagMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(tagMapper, times(1)).insert(any(Tag.class));
    }

    /**
     * 测试添加标签 - 标签名称为空
     */
    @Test
    void testAddTag_TagNameNull() {
        tagDTO.setTagName(null);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.addTag(tagDTO);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试添加标签 - 标签名称为空字符串
     */
    @Test
    void testAddTag_TagNameEmpty() {
        tagDTO.setTagName("");

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.addTag(tagDTO);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试添加标签 - 标签名称为空白字符
     */
    @Test
    void testAddTag_TagNameBlank() {
        tagDTO.setTagName("   ");

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.addTag(tagDTO);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试添加标签 - 标签名称重复
     */
    @Test
    void testAddTag_TagNameDuplicate() {
        // Mock 标签名称重复
        when(tagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.addTag(tagDTO);
        });

        assertEquals(HttpCodeEnum.TAG_NAME_DUPLICATE.getCode(), exception.getCode());
    }

    /**
     * 测试添加标签 - isActive为null（使用默认值）
     */
    @Test
    void testAddTag_IsActiveNull() {
        tagDTO.setIsActive(null);

        // Mock 标签名称不重复
        when(tagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // Mock 插入标签
        when(tagMapper.insert(any(Tag.class))).thenAnswer(invocation -> {
            Tag tag = invocation.getArgument(0);
            // 验证isActive默认值为1
            assertEquals(1, tag.getIsActive());
            return 1;
        });

        // 执行添加标签
        boolean result = tagService.addTag(tagDTO);

        // 验证结果
        assertTrue(result);
    }

    /**
     * 测试更新标签 - 正常流程
     */
    @Test
    void testUpdateTag_Success() {
        Integer tagId = 1;

        // Mock 查询标签
        when(tagMapper.selectById(tagId)).thenReturn(testTag);

        // Mock 标签名称不重复
        when(tagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // Mock 更新标签
        when(tagMapper.updateById(any(Tag.class))).thenReturn(1);

        // 执行更新标签
        boolean result = tagService.updateTag(tagId, tagDTO);

        // 验证结果
        assertTrue(result);

        // 验证调用
        verify(tagMapper, times(1)).selectById(tagId);
        verify(tagMapper, times(1)).updateById(any(Tag.class));
    }

    /**
     * 测试更新标签 - 标签ID为空
     */
    @Test
    void testUpdateTag_TagIdNull() {
        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.updateTag(null, tagDTO);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试更新标签 - 标签不存在
     */
    @Test
    void testUpdateTag_TagNotFound() {
        Integer tagId = 999;

        // Mock 查询标签返回null
        when(tagMapper.selectById(tagId)).thenReturn(null);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.updateTag(tagId, tagDTO);
        });

        assertEquals(HttpCodeEnum.RESOURCE_NOT_FOUND.getCode(), exception.getCode());
    }

    /**
     * 测试更新标签 - 标签已删除
     */
    @Test
    void testUpdateTag_TagDeleted() {
        Integer tagId = 1;

        // Mock 查询标签（已删除）
        Tag deletedTag = Tag.builder()
                .tagId(1L)
                .tagName("已删除标签")
                .deletedAt(new Date())
                .build();
        when(tagMapper.selectById(tagId)).thenReturn(deletedTag);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.updateTag(tagId, tagDTO);
        });

        assertEquals(HttpCodeEnum.RESOURCE_NOT_FOUND.getCode(), exception.getCode());
    }

    /**
     * 测试更新标签 - 标签名称为空
     */
    @Test
    void testUpdateTag_TagNameNull() {
        Integer tagId = 1;
        tagDTO.setTagName(null);

        // Mock 查询标签
        when(tagMapper.selectById(tagId)).thenReturn(testTag);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.updateTag(tagId, tagDTO);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试更新标签 - 标签名称重复（排除自己）
     */
    @Test
    void testUpdateTag_TagNameDuplicate() {
        Integer tagId = 1;

        // Mock 查询标签
        when(tagMapper.selectById(tagId)).thenReturn(testTag);

        // Mock 标签名称重复（排除当前标签后仍有重复）
        when(tagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.updateTag(tagId, tagDTO);
        });

        assertEquals(HttpCodeEnum.TAG_NAME_DUPLICATE.getCode(), exception.getCode());
    }

    /**
     * 测试获取标签列表 - 正常流程
     */
    @Test
    void testGetTagList_Success() {
        PageQuery pageQuery = new PageQuery(1, 10);

        // Mock 分页查询
        Page<Tag> page = new Page<>(1, 10);
        List<Tag> tags = new ArrayList<>();
        tags.add(testTag);
        page.setRecords(tags);
        page.setTotal(1);

        when(tagMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // 执行查询
        PageResult result = tagService.getTagList(pageQuery, null, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());

        // 验证调用
        verify(tagMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    /**
     * 测试获取标签列表 - 带标签名称过滤
     */
    @Test
    void testGetTagList_WithTagName() {
        PageQuery pageQuery = new PageQuery(1, 10);
        String tagName = "测试";

        // Mock 分页查询
        Page<Tag> page = new Page<>(1, 10);
        List<Tag> tags = new ArrayList<>();
        tags.add(testTag);
        page.setRecords(tags);
        page.setTotal(1);

        when(tagMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // 执行查询
        PageResult result = tagService.getTagList(pageQuery, tagName, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    /**
     * 测试获取标签列表 - 带isActive过滤
     */
    @Test
    void testGetTagList_WithIsActive() {
        PageQuery pageQuery = new PageQuery(1, 10);
        Integer isActive = 1;

        // Mock 分页查询
        Page<Tag> page = new Page<>(1, 10);
        List<Tag> tags = new ArrayList<>();
        tags.add(testTag);
        page.setRecords(tags);
        page.setTotal(1);

        when(tagMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // 执行查询
        PageResult result = tagService.getTagList(pageQuery, null, isActive);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    /**
     * 测试获取标签列表 - 带多个过滤条件
     */
    @Test
    void testGetTagList_WithMultipleFilters() {
        PageQuery pageQuery = new PageQuery(1, 10);
        String tagName = "测试";
        Integer isActive = 1;

        // Mock 分页查询
        Page<Tag> page = new Page<>(1, 10);
        List<Tag> tags = new ArrayList<>();
        tags.add(testTag);
        page.setRecords(tags);
        page.setTotal(1);

        when(tagMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // 执行查询
        PageResult result = tagService.getTagList(pageQuery, tagName, isActive);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    /**
     * 测试获取标签列表 - 空结果
     */
    @Test
    void testGetTagList_EmptyResult() {
        PageQuery pageQuery = new PageQuery(1, 10);

        // Mock 分页查询返回空
        Page<Tag> page = new Page<>(1, 10);
        page.setRecords(new ArrayList<>());
        page.setTotal(0);

        when(tagMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // 执行查询
        PageResult result = tagService.getTagList(pageQuery, null, null);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getTotal());
    }

    /**
     * 测试删除标签 - 正常流程
     */
    @Test
    void testDeleteTag_Success() {
        Integer tagId = 1;

        // Mock 查询标签
        when(tagMapper.selectById(tagId)).thenReturn(testTag);

        // Mock 更新标签（软删除）
        when(tagMapper.updateById(any(Tag.class))).thenReturn(1);

        // 执行删除标签
        boolean result = tagService.deleteTag(tagId);

        // 验证结果
        assertTrue(result);

        // 验证调用
        verify(tagMapper, times(1)).selectById(tagId);
        verify(tagMapper, times(1)).updateById(any(Tag.class));
    }

    /**
     * 测试删除标签 - 标签ID为空
     */
    @Test
    void testDeleteTag_TagIdNull() {
        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.deleteTag(null);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试删除标签 - 标签不存在
     */
    @Test
    void testDeleteTag_TagNotFound() {
        Integer tagId = 999;

        // Mock 查询标签返回null
        when(tagMapper.selectById(tagId)).thenReturn(null);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.deleteTag(tagId);
        });

        assertEquals(HttpCodeEnum.RESOURCE_NOT_FOUND.getCode(), exception.getCode());
    }

    /**
     * 测试删除标签 - 标签已删除
     */
    @Test
    void testDeleteTag_TagAlreadyDeleted() {
        Integer tagId = 1;

        // Mock 查询标签（已删除）
        Tag deletedTag = Tag.builder()
                .tagId(1L)
                .tagName("已删除标签")
                .deletedAt(new Date())
                .build();
        when(tagMapper.selectById(tagId)).thenReturn(deletedTag);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tagService.deleteTag(tagId);
        });

        assertEquals(HttpCodeEnum.RESOURCE_NOT_FOUND.getCode(), exception.getCode());
    }
}
