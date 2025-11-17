package org.software.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.software.model.content.tag.Tag;
import org.springframework.stereotype.Repository;

/*


*/
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
