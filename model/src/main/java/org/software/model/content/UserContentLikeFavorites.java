package org.software.model.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.software.model.user.UserV;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContentLikeFavorites {
    private Long contentId;
    private Long userId;
    private UserV userV;
    private String contentType;
    private FirstMedia firstMedia;
    private String title;
    private Integer likeCount;
    public Date createdAt;
    public Date updatedAt;
    public Date deletedAt;
}
