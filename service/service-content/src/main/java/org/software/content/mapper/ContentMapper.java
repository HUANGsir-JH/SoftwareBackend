package org.software.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.checkerframework.checker.units.qual.C;
import org.software.model.content.Content;
import org.software.model.content.vo.ContentDetailVO;
import org.software.model.media.ContentMedia;

import java.util.List;

/**
 * 内容主表(Content)表数据库访问层
 *
 * @author Ra1nbot
 * @since 2025-11-11 09:40:54
 */
@Mapper
public interface ContentMapper extends BaseMapper<Content> {
    /**
     * 批量插入标签关联
     */
    void batchInsertTags(@Param("contentId") Long contentId, @Param("tags") List<Long> tags);

    /**
     * 批量插入媒体文件
     */
    void batchInsertMedias(@Param("contentId") Long contentId, @Param("medias") List<ContentMedia> medias);

    /**
     * 删除指定内容的所有标签关联（软删除）
     */
    void deleteTagsByContentId(@Param("contentId") Long contentId);

    /**
     * 删除指定内容的所有媒体文件（软删除）
     */
    void deleteMediasByContentId(@Param("contentId") Long contentId);
    
    List<ContentDetailVO> selectContentDetailPage(Page<ContentDetailVO> page, String title, String contentType,
                                                  String startTime, String endTime, String status);
}

