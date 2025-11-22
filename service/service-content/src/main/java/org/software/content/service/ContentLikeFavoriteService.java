package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.content.dto.ContentLikeFavoriteDTO;
import org.software.model.content.vo.ContentLikeFavoriteVO;
import org.software.model.exception.BusinessException;
import org.software.model.interaction.ContentLikeFavorite;

import java.util.List;

public interface ContentLikeFavoriteService extends IService<ContentLikeFavorite> {
    boolean addOrCancelLike(ContentLikeFavoriteDTO dto) throws BusinessException;
    List<ContentLikeFavoriteVO> getLikeFavoriteRecords(Integer userId, String type) throws BusinessException;
    boolean readAll(Integer userId, String type) throws BusinessException;
    List<ContentLikeFavoriteVO> getUnreadLikeFavorite(Integer userId, String type) throws BusinessException;
}