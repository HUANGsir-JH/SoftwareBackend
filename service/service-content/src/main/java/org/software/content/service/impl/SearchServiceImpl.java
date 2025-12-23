package org.software.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import org.software.content.service.ContentService;
import org.software.content.service.SearchService;
import org.software.feign.UserFeignClient;
import org.software.model.Response;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ContentService contentService;
    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public Map<String, PageResult> searchHome(PageQuery pquery, String type, String query) {
        Map<String, PageResult> map = new HashMap<>();

        if (Objects.equals(type, "all") || Objects.equals(type, "content")) {
            PageResult allContent = contentService.getAllContent(pquery, null, query);
            map.put("contents", allContent);
        }
        if (Objects.equals(type, "all") || Objects.equals(type, "user")) {
            Response allUser = userFeignClient.searchFriend(pquery.getPageNum(), pquery.getPageSize(), query);
            PageResult pr = BeanUtil.copyProperties(allUser.getData(), PageResult.class);
            map.put("users", pr);
        }
        return map;

    }
}
