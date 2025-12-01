package org.software.content.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.software.model.content.post.PostD;
import org.software.model.content.post.PostE;

@Mapper
public interface PostMapper extends BaseMapper {
    //这里要传是草稿状态还是非草稿状态
    Page<PostE> selectSimplePage(Page<PostE> page, @Param("ew") Wrapper<PostE> wrapper);
    Page<PostE> selectFriendPage(Page<PostE> page, @Param("ew") Wrapper<PostE> wrapper);
    //不知道帖子在数据库中表结构，后面可以改xml文件，我让ai生成了一个，maybe有问题
    //指路PostMapper.xml
    int updateById(PostD post);
}
