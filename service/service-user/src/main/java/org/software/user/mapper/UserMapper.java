package org.software.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.software.model.user.EmailLoginRequest;
import org.software.model.user.PageUserD;
import org.software.model.user.User;
import org.software.model.user.UsernameLoginRequest;


/**
 * 用户表(User)表数据库访问层
 *
 * @author Ra1nbot
 * @since 2025-11-09 00:12:35
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}

