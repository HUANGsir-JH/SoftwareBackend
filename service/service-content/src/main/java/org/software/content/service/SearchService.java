package org.software.content.service;


import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;

import java.util.Map;

public interface SearchService {

    Map<String, PageResult> searchHome(PageQuery pquery, String type, String query);
}
