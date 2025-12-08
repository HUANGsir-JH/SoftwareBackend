package org.software.model.content.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.software.model.content.FirstMedia;
import org.software.model.user.UserV;

import java.util.Date;

@Data
public class ContentVO {
    private Long contentId;
    private String contentType;
    private FirstMedia firstMedia;
    private String title;
    private Long likeCount;

    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;

    private UserV user;

}
