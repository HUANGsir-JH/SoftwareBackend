package org.software.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.software.content.service.ContentService;
import org.software.feign.UserFeignClient;
import org.software.model.Response;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SearchServiceImpl 白盒测试
 * 测试重点：搜索服务核心业务逻辑分支覆盖
 */
@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    private ContentService contentService;
    private UserFeignClient userFeignClient;
    private SearchServiceImpl searchService;

    @BeforeEach
    void setUp() {
        // 创建 Mock 对象
        contentService = mock(ContentService.class);
        userFeignClient = mock(UserFeignClient.class);

        // 手动创建 SearchServiceImpl 并注入依赖
        searchService = new SearchServiceImpl();

        // 使用反射注入 Mock 依赖
        try {
            java.lang.reflect.Field contentServiceField = SearchServiceImpl.class.getDeclaredField("contentService");
            contentServiceField.setAccessible(true);
            contentServiceField.set(searchService, contentService);

            java.lang.reflect.Field userFeignClientField = SearchServiceImpl.class.getDeclaredField("userFeignClient");
            userFeignClientField.setAccessible(true);
            userFeignClientField.set(searchService, userFeignClient);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }

    /**
     * 测试搜索首页 - 类型为all（搜索全部）
     */
    @Test
    void testSearchHome_TypeAll() {
        PageQuery pageQuery = new PageQuery(1, 10);
        String type = "all";
        String query = "测试";

        // Mock 内容搜索
        PageResult contentResult = PageResult.builder()
                .pageNum(1)
                .pageSize(10)
                .total(5L)
                .records(new ArrayList<>())
                .build();
        when(contentService.getAllContent(any(PageQuery.class), isNull(), eq(query)))
                .thenReturn(contentResult);

        // Mock 用户搜索
        PageResult userResult = PageResult.builder()
                .pageNum(1)
                .pageSize(10)
                .total(3L)
                .records(new ArrayList<>())
                .build();
        when(userFeignClient.searchFriend(anyInt(), anyInt(), eq(query)))
                .thenReturn(Response.success(userResult));

        // 执行搜索
        Map<String, PageResult> result = searchService.searchHome(pageQuery, type, query);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("contents"));
        assertTrue(result.containsKey("users"));
        assertEquals(5L, result.get("contents").getTotal());
        assertEquals(3L, result.get("users").getTotal());

        // 验证调用
        verify(contentService, times(1)).getAllContent(any(PageQuery.class), isNull(), eq(query));
        verify(userFeignClient, times(1)).searchFriend(anyInt(), anyInt(), eq(query));
    }

    /**
     * 测试搜索首页 - 类型为content（仅搜索内容）
     */
    @Test
    void testSearchHome_TypeContent() {
        PageQuery pageQuery = new PageQuery(1, 10);
        String type = "content";
        String query = "测试";

        // Mock 内容搜索
        PageResult contentResult = PageResult.builder()
                .pageNum(1)
                .pageSize(10)
                .total(5L)
                .records(new ArrayList<>())
                .build();
        when(contentService.getAllContent(any(PageQuery.class), isNull(), eq(query)))
                .thenReturn(contentResult);

        // 执行搜索
        Map<String, PageResult> result = searchService.searchHome(pageQuery, type, query);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("contents"));
        assertFalse(result.containsKey("users"));
        assertEquals(5L, result.get("contents").getTotal());

        // 验证调用
        verify(contentService, times(1)).getAllContent(any(PageQuery.class), isNull(), eq(query));
        verify(userFeignClient, never()).searchFriend(anyInt(), anyInt(), anyString());
    }

    /**
     * 测试搜索首页 - 类型为user（仅搜索用户）
     */
    @Test
    void testSearchHome_TypeUser() {
        PageQuery pageQuery = new PageQuery(1, 10);
        String type = "user";
        String query = "测试";

        // Mock 用户搜索
        PageResult userResult = PageResult.builder()
                .pageNum(1)
                .pageSize(10)
                .total(3L)
                .records(new ArrayList<>())
                .build();
        when(userFeignClient.searchFriend(anyInt(), anyInt(), eq(query)))
                .thenReturn(Response.success(userResult));

        // 执行搜索
        Map<String, PageResult> result = searchService.searchHome(pageQuery, type, query);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.containsKey("contents"));
        assertTrue(result.containsKey("users"));
        assertEquals(3L, result.get("users").getTotal());

        // 验证调用
        verify(contentService, never()).getAllContent(any(PageQuery.class), any(), anyString());
        verify(userFeignClient, times(1)).searchFriend(anyInt(), anyInt(), eq(query));
    }

    /**
     * 测试搜索首页 - 空搜索关键词
     */
    @Test
    void testSearchHome_EmptyQuery() {
        PageQuery pageQuery = new PageQuery(1, 10);
        String type = "all";
        String query = "";

        // Mock 内容搜索
        PageResult contentResult = PageResult.builder()
                .pageNum(1)
                .pageSize(10)
                .total(0L)
                .records(new ArrayList<>())
                .build();
        when(contentService.getAllContent(any(PageQuery.class), isNull(), eq(query)))
                .thenReturn(contentResult);

        // Mock 用户搜索
        PageResult userResult = PageResult.builder()
                .pageNum(1)
                .pageSize(10)
                .total(0L)
                .records(new ArrayList<>())
                .build();
        when(userFeignClient.searchFriend(anyInt(), anyInt(), eq(query)))
                .thenReturn(Response.success(userResult));

        // 执行搜索
        Map<String, PageResult> result = searchService.searchHome(pageQuery, type, query);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("contents"));
        assertTrue(result.containsKey("users"));
    }

    /**
     * 测试搜索首页 - 无结果
     */
    @Test
    void testSearchHome_NoResults() {
        PageQuery pageQuery = new PageQuery(1, 10);
        String type = "all";
        String query = "不存在的内容";

        // Mock 内容搜索返回空
        PageResult contentResult = PageResult.builder()
                .pageNum(1)
                .pageSize(10)
                .total(0L)
                .records(new ArrayList<>())
                .build();
        when(contentService.getAllContent(any(PageQuery.class), isNull(), eq(query)))
                .thenReturn(contentResult);

        // Mock 用户搜索返回空
        PageResult userResult = PageResult.builder()
                .pageNum(1)
                .pageSize(10)
                .total(0L)
                .records(new ArrayList<>())
                .build();
        when(userFeignClient.searchFriend(anyInt(), anyInt(), eq(query)))
                .thenReturn(Response.success(userResult));

        // 执行搜索
        Map<String, PageResult> result = searchService.searchHome(pageQuery, type, query);

        // 验证结果
        assertNotNull(result);
        assertEquals(0L, result.get("contents").getTotal());
        assertEquals(0L, result.get("users").getTotal());
    }

    /**
     * 测试搜索首页 - 其他类型（不匹配any分支）
     */
    @Test
    void testSearchHome_OtherType() {
        PageQuery pageQuery = new PageQuery(1, 10);
        String type = "other";
        String query = "测试";

        // 执行搜索
        Map<String, PageResult> result = searchService.searchHome(pageQuery, type, query);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证未调用任何搜索服务
        verify(contentService, never()).getAllContent(any(PageQuery.class), any(), anyString());
        verify(userFeignClient, never()).searchFriend(anyInt(), anyInt(), anyString());
    }

    /**
     * 测试搜索首页 - 大分页参数
     */
    @Test
    void testSearchHome_LargePagination() {
        PageQuery pageQuery = new PageQuery(10, 100);
        String type = "all";
        String query = "测试";

        // Mock 内容搜索
        PageResult contentResult = PageResult.builder()
                .pageNum(10)
                .pageSize(100)
                .total(500L)
                .records(new ArrayList<>())
                .build();
        when(contentService.getAllContent(any(PageQuery.class), isNull(), eq(query)))
                .thenReturn(contentResult);

        // Mock 用户搜索
        PageResult userResult = PageResult.builder()
                .pageNum(10)
                .pageSize(100)
                .total(200L)
                .records(new ArrayList<>())
                .build();
        when(userFeignClient.searchFriend(anyInt(), anyInt(), eq(query)))
                .thenReturn(Response.success(userResult));

        // 执行搜索
        Map<String, PageResult> result = searchService.searchHome(pageQuery, type, query);

        // 验证结果
        assertNotNull(result);
        assertEquals(500L, result.get("contents").getTotal());
        assertEquals(200L, result.get("users").getTotal());
    }

    /**
     * 测试搜索首页 - null查询参数
     */
    @Test
    void testSearchHome_NullQuery() {
        PageQuery pageQuery = new PageQuery(1, 10);
        String type = "all";
        String query = null;

        // Mock 内容搜索
        PageResult contentResult = PageResult.builder()
                .pageNum(1)
                .pageSize(10)
                .total(10L)
                .records(new ArrayList<>())
                .build();
        when(contentService.getAllContent(any(PageQuery.class), isNull(), isNull()))
                .thenReturn(contentResult);

        // Mock 用户搜索
        PageResult userResult = PageResult.builder()
                .pageNum(1)
                .pageSize(10)
                .total(5L)
                .records(new ArrayList<>())
                .build();
        when(userFeignClient.searchFriend(anyInt(), anyInt(), isNull()))
                .thenReturn(Response.success(userResult));

        // 执行搜索
        Map<String, PageResult> result = searchService.searchHome(pageQuery, type, query);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.containsKey("contents"));
        assertTrue(result.containsKey("users"));
    }
}
