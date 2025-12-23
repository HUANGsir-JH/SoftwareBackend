package org.software.content.controller;

import org.software.content.service.SearchService;
import org.software.model.Response;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/home")
    public Response searchHome(PageQuery pquery, String type, String query) {
        Map<String, PageResult> pr = searchService.searchHome(pquery, type, query);
        return Response.success(pr);
    }

}
