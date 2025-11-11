package org.software.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.software.content.mapper.ContentMapper;
import org.software.content.service.ContentService;
import org.software.model.constants.ContentContants;
import org.software.model.content.Content;
import org.software.model.content.ContentD;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 内容主表(Content)表服务实现类
 *
 * @author Ra1nbot
 * @since 2025-11-11 09:40:54
 */
@Service
public class ContentServiceImpl extends ServiceImpl<ContentMapper, Content> implements ContentService {
    
    @Override
    public Long create(ContentD contentD) {
        // 创建 Content 实体
        Content content = BeanUtil.toBean(contentD, Content.class);
        
        // TODO: 从 token 中获取当前登录用户的 userId
        // content.setUserId(userId);
        
        // 如果状态为空，设置默认状态为草稿
        if (content.getStatus() == null || content.getStatus().isEmpty()) {
            content.setStatus(ContentContants.STATUS_DRAFT); // 草稿状态
        }
        
        // 保存到数据库
        boolean success = this.save(content);

        if (success) {
            // TODO: 送入任务队列进行后续处理（如审核、推送等）
            return content.getContentId();
        } else {
            throw new RuntimeException("帖子创建失败");
        }
    }
}

