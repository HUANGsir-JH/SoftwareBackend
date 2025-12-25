package org.software.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.software.model.content.UserContentLikeFavorites;
import org.software.model.content.dto.ContentLikeFavoriteDTO;
import org.software.model.content.vo.ContentLikeFavoriteVO;
import org.software.model.exception.BusinessException;
import org.software.model.interaction.ContentLikeFavorite;
import org.software.model.page.PageQuery;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ContentLikeFavoriteService extends IService<ContentLikeFavorite> {
    boolean addOrCancelLike(Integer contentId, String type) throws BusinessException;
    List<ContentLikeFavoriteVO> getLikeFavoriteRecords(Integer pageNum,
                                                       Integer pageSize,
                                                       String type) throws BusinessException;
    boolean readAll() throws BusinessException;
    List<ContentLikeFavoriteVO> getUnreadLikeFavorite(Integer pageNum,
                                                      Integer pageSize,
                                                      String type) throws BusinessException;
    
    
    List<UserContentLikeFavorites> getUserLikedContents(PageQuery pageQuery, String type);
}
