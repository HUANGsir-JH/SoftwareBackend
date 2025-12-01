package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.content.dto.UserContentDataVO;
import org.software.model.content.Content;
import org.software.model.content.ContentD;
import org.software.model.content.post.PostD;
import org.software.model.content.post.PostPage;
import org.software.model.page.PageQuery;


/**
 * 内容主表(Content)表服务接口
 *
 * @author Ra1nbot
 * @since 2025-11-11 09:40:54
 */
public interface ContentService extends IService<Content> {

    Long create(ContentD contentD);
    PostPage getMyContent(PageQuery query, Long userId);
    PostPage getAllFriendContent(PageQuery query, Long userId);

    void updatePost(Content content);

    void deleteContent(Integer contentId);

    PostD viewContent(Integer contentId);

    PostPage getAllContentForAdmin(PageQuery pageQuery, String status, String contentType,String startTime,String title);

    void approveContent(Integer contentId);

    boolean banContent(Integer contentId);

    UserContentDataVO getContentData(Integer contentId);
}

