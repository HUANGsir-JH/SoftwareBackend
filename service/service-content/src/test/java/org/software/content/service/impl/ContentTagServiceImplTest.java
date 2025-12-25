package org.software.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.software.content.mapper.ContentTagMapper;
import org.software.content.mapper.TagMapper;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.content.ContentTag;
import org.software.model.content.Tag;
import org.software.model.content.dto.ContentTagDTO;
import org.software.model.exception.BusinessException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ContentTagServiceImpl 白盒测试
 * 测试重点：内容标签关联管理核心业务逻辑分支覆盖
 */
@ExtendWith(MockitoExtension.class)
class ContentTagServiceImplTest {

    private ContentTagMapper contentTagMapper;
    private TagMapper tagMapper;
    private ContentTagServiceImpl contentTagService;

    private ContentTag testContentTag;
    private ContentTagDTO contentTagDTO;

    @BeforeEach
    void setUp() {
        // 创建 Mock 对象
        contentTagMapper = mock(ContentTagMapper.class);
        tagMapper = mock(TagMapper.class);

        // 手动创建 ContentTagServiceImpl 并注入依赖
        contentTagService = new ContentTagServiceImpl() {
            @Override
            public List<ContentTag> list(LambdaQueryWrapper<ContentTag> queryWrapper) {
                return contentTagMapper.selectList(queryWrapper);
            }

            @Override
            public boolean updateBatchById(java.util.Collection<ContentTag> entityList) {
                entityList.forEach(entity -> contentTagMapper.updateById(entity));
                return true;
            }

            @Override
            public boolean saveBatch(java.util.Collection<ContentTag> entityList) {
                entityList.forEach(entity -> contentTagMapper.insert(entity));
                return true;
            }
        };

        // 使用反射注入 Mock 依赖
        try {
            java.lang.reflect.Field contentTagMapperField = ContentTagServiceImpl.class.getDeclaredField("contentTagMapper");
            contentTagMapperField.setAccessible(true);
            contentTagMapperField.set(contentTagService, contentTagMapper);

            java.lang.reflect.Field tagMapperField = ContentTagServiceImpl.class.getDeclaredField("tagMapper");
            tagMapperField.setAccessible(true);
            tagMapperField.set(contentTagService, tagMapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }

        // 准备测试数据
        testContentTag = ContentTag.builder()
                .contentTagId(1L)
                .contentId(100L)
                .tagId(1L)
                .createdAt(new Date())
                .build();

        contentTagDTO = new ContentTagDTO();
        contentTagDTO.setContentId(100L);
        contentTagDTO.setTagIds(Arrays.asList(1L, 2L, 3L));
    }

    /**
     * 测试上传内容标签 - 正常流程（新增）
     */
    @Test
    void testUploadContentTag_Success() {
        // Mock 标签验证
        when(tagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        // Mock 查询原有标签（无）
        when(contentTagMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // Mock 插入新标签
        when(contentTagMapper.insert(any(ContentTag.class))).thenReturn(1);

        // 执行上传标签
        boolean result = contentTagService.uploadContentTag(contentTagDTO);

        // 验证结果
        assertTrue(result);

        // 验证调用
        verify(tagMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(contentTagMapper, times(3)).insert(any(ContentTag.class));
    }

    /**
     * 测试上传内容标签 - 替换原有标签
     */
    @Test
    void testUploadContentTag_ReplaceExisting() {
        // Mock 标签验证
        when(tagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        // Mock 查询原有标签（有旧标签）
        List<ContentTag> oldTags = new ArrayList<>();
        ContentTag oldTag1 = ContentTag.builder()
                .contentTagId(1L)
                .contentId(100L)
                .tagId(4L)
                .build();
        ContentTag oldTag2 = ContentTag.builder()
                .contentTagId(2L)
                .contentId(100L)
                .tagId(5L)
                .build();
        oldTags.add(oldTag1);
        oldTags.add(oldTag2);

        when(contentTagMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(oldTags);

        // Mock 更新旧标签（软删除）
        when(contentTagMapper.updateById(any(ContentTag.class))).thenReturn(1);

        // Mock 插入新标签
        when(contentTagMapper.insert(any(ContentTag.class))).thenReturn(1);

        // 执行上传标签
        boolean result = contentTagService.uploadContentTag(contentTagDTO);

        // 验证结果
        assertTrue(result);

        // 验证调用
        verify(contentTagMapper, times(2)).updateById(any(ContentTag.class)); // 软删除2个旧标签
        verify(contentTagMapper, times(3)).insert(any(ContentTag.class)); // 插入3个新标签
    }

    /**
     * 测试上传内容标签 - 内容ID为空
     */
    @Test
    void testUploadContentTag_ContentIdNull() {
        contentTagDTO.setContentId(null);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            contentTagService.uploadContentTag(contentTagDTO);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试上传内容标签 - 标签列表为空
     */
    @Test
    void testUploadContentTag_TagIdsNull() {
        contentTagDTO.setTagIds(null);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            contentTagService.uploadContentTag(contentTagDTO);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试上传内容标签 - 标签列表为空集合
     */
    @Test
    void testUploadContentTag_TagIdsEmpty() {
        contentTagDTO.setTagIds(new ArrayList<>());

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            contentTagService.uploadContentTag(contentTagDTO);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试上传内容标签 - 标签无效
     */
    @Test
    void testUploadContentTag_InvalidTags() {
        // Mock 标签验证（只有2个有效标签）
        when(tagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            contentTagService.uploadContentTag(contentTagDTO);
        });

        assertEquals(HttpCodeEnum.INVALID_TAG.getCode(), exception.getCode());
    }

    /**
     * 测试上传内容标签 - 部分标签无效
     */
    @Test
    void testUploadContentTag_PartiallyInvalidTags() {
        contentTagDTO.setTagIds(Arrays.asList(1L, 2L, 999L)); // 999不存在

        // Mock 标签验证（只有2个有效标签）
        when(tagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            contentTagService.uploadContentTag(contentTagDTO);
        });

        assertEquals(HttpCodeEnum.INVALID_TAG.getCode(), exception.getCode());
    }

    /**
     * 测试更新内容标签 - 正常流程
     */
    @Test
    void testUpdateContentTag_Success() {
        // Mock 标签验证
        when(tagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L);

        // Mock 查询原有标签
        when(contentTagMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // Mock 插入新标签
        when(contentTagMapper.insert(any(ContentTag.class))).thenReturn(1);

        // 执行更新标签
        boolean result = contentTagService.updateContentTag(contentTagDTO);

        // 验证结果
        assertTrue(result);

        // 验证调用（updateContentTag 实际调用 uploadContentTag）
        verify(tagMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(contentTagMapper, times(3)).insert(any(ContentTag.class));
    }

    /**
     * 测试更新内容标签 - 内容ID为空
     */
    @Test
    void testUpdateContentTag_ContentIdNull() {
        contentTagDTO.setContentId(null);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            contentTagService.updateContentTag(contentTagDTO);
        });

        assertEquals(HttpCodeEnum.PARAM_ERROR.getCode(), exception.getCode());
    }

    /**
     * 测试上传内容标签 - 单个标签
     */
    @Test
    void testUploadContentTag_SingleTag() {
        contentTagDTO.setTagIds(Arrays.asList(1L));

        // Mock 标签验证
        when(tagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // Mock 查询原有标签（无）
        when(contentTagMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // Mock 插入新标签
        when(contentTagMapper.insert(any(ContentTag.class))).thenReturn(1);

        // 执行上传标签
        boolean result = contentTagService.uploadContentTag(contentTagDTO);

        // 验证结果
        assertTrue(result);

        // 验证调用
        verify(contentTagMapper, times(1)).insert(any(ContentTag.class));
    }

    /**
     * 测试上传内容标签 - 大量标签
     */
    @Test
    void testUploadContentTag_MultipleTags() {
        List<Long> tagIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        contentTagDTO.setTagIds(tagIds);

        // Mock 标签验证
        when(tagMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

        // Mock 查询原有标签（无）
        when(contentTagMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        // Mock 插入新标签
        when(contentTagMapper.insert(any(ContentTag.class))).thenReturn(1);

        // 执行上传标签
        boolean result = contentTagService.uploadContentTag(contentTagDTO);

        // 验证结果
        assertTrue(result);

        // 验证调用
        verify(contentTagMapper, times(10)).insert(any(ContentTag.class));
    }
}
