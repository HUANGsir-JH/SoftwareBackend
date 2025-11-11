package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.content.Content;
import org.software.model.content.ContentD;


/**
 * 内容主表(Content)表服务接口
 *
 * @author Ra1nbot
 * @since 2025-11-11 09:40:54
 */
public interface ContentService extends IService<Content> {

    Long create(ContentD contentD);
}

