package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.content.Content;
import org.software.model.content.dto.ContentDTO;
import org.software.model.content.vo.ContentDetailVO;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;


/**
 * 内容主表(Content)表服务接口
 *
 * @author Ra1nbot
 * @since 2025-12-08 14:03:09
 */
public interface ContentService extends IService<Content> {

    Long create(ContentDTO contentDTO);

    PageResult pageContent(PageQuery pageQuery, Long userId, String status);

    PageResult getAllContent(PageQuery pageQuery, Long tag, String query);

    void updatePost(ContentDTO content);

    ContentDetailVO viewContent(Long contentId);
    
    PageResult getContentForAdmin(Integer pageNum, Integer pageSize, String title,
                                  String contentType,
                                  String startTime, String endTime, String status);
    
    void remove(Long contentId);

}

