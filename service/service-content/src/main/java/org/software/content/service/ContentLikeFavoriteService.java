package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.content.dto.ContentLikeFavoriteDTO;
import org.software.content.dto.ContentLikeFavoriteVO;
import org.software.model.interaction.ContentLikeFavorite;

import java.util.List;

public interface ContentLikeFavoriteService extends IService<ContentLikeFavorite> {
    boolean addOrCancelLike(ContentLikeFavoriteDTO dto);
    List<ContentLikeFavoriteVO> getLikeFavoriteRecords(Integer userId, String type);
    boolean readAll(Integer userId, String type);
    List<ContentLikeFavoriteVO> getUnreadLikeFavorite(Integer userId, String type);
}