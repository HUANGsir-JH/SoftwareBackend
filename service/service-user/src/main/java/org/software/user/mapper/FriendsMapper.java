package org.software.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.software.model.social.Friends;


/**
 * 好友关系表(Friends)表数据库访问层
 *
 * @author Ra1nbot
 * @since 2025-11-26 22:14:16
 */
@Mapper
public interface FriendsMapper extends BaseMapper<Friends> {

}

