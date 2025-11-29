package org.software.notification.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.software.feign.UserFeignClient;
import org.software.model.Response;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.software.model.social.priv.PrivateConversations;
import org.software.model.user.UserV;
import org.software.notification.mapper.PrivateConversationsMapper;
import org.software.notification.service.PrivateConversationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 私聊会话表(PrivateConversations)表服务实现类
 *
 * @author Ra1nbot
 * @since 2025-11-27 22:06:51
 */
@Service
public class PrivateConversationsServiceImpl extends ServiceImpl<PrivateConversationsMapper, PrivateConversations> implements PrivateConversationsService {

    @Autowired
    private PrivateConversationsMapper privateConversationsMapper;
    @Autowired
    private UserFeignClient userFeignClient;

    @Override
    public void addConv(Long friendId) {
        long userId = StpUtil.getLoginIdAsLong();

        PrivateConversations conv = PrivateConversations.builder()
                .user1Id(userId)
                .user2Id(friendId)
                .build();

        save(conv);
    }

    @Override
    public PageResult getPrivateChatList(PageQuery query) {
        Long userId = StpUtil.getLoginIdAsLong();

        Page<PrivateConversations> page = new Page<>(query.getPageNum(), query.getPageSize());
        page = privateConversationsMapper.pageC(page, userId);
        List<PrivateConversations> list = page.getRecords().stream()
                .peek(conv -> {
                    Response response = userFeignClient.getUser(userId);
                    UserV friend = BeanUtil.copyProperties(response.getData(), UserV.class);
                    conv.setFriend(friend);

                }).toList();

        return PageResult.builder()
                .total(page.getTotal())
                .pageNum(query.getPageNum())
                .pageSize(query.getPageSize())
                .records(list)
                .build();
    }

    @Override
    public void updateConv(PrivateConversations conv) {
        save(conv);
    }

}

