package org.software.content.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.software.content.service.ContentService;
import org.software.model.Response;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.content.Content;
import org.software.model.content.dto.ContentDTO;
import org.software.model.content.vo.ContentDetailVO;
import org.software.model.content.vo.ContentVO;
import org.software.model.content.vo.UserContentDataVO;
import org.software.model.exception.BusinessException;
import org.software.model.page.PageQuery;
import org.software.model.page.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 内容主表(Content)表控制层
 *
 * @author Ra1nbot
 * @since 2025-12-08 14:03:09
 */
@RestController
@RequestMapping("/content")
public class ContentController {

    @Autowired
    private ContentService contentService;

    /**
     * 创建帖子
     * POST /content
     * 
     * @param contentD 帖子创建数据传输对象
     * @return Response 包含创建结果信息
     */
    @PostMapping
    public Response create(@RequestBody ContentDTO contentD) {
        return Response.success(contentService.create(contentD));
    }

    /**
     * 获取用户帖子/草稿
     * GET /content
     * 
     * @param pageQuery 分页查询参数
     * @param userId 用户id
     * @return Response 包含帖子分页数据
     */
    @GetMapping
    public Response getMyContent(PageQuery pageQuery, @RequestParam Long userId, @RequestParam String status) {
        if (userId == null) {
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }
        PageResult pageResult = contentService.pageContent(pageQuery, userId, status);
        return Response.success(pageResult);
    }

    /**
     * 更新帖子
     * PUT /content
     * 
     * @param contentD 帖子更新数据
     * @return Response 更新结果
     */
    @PutMapping
    public Response updateContent(@RequestBody ContentDTO contentD) {
        if (contentD.getContentId() == null) {
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }
        contentService.updatePost(contentD);
        return Response.success();
    }

    /**
     * 删除帖子
     * DELETE /content/{contentId}
     * 
     * @param contentId 帖子id
     * @return Response 删除结果
     */
    @DeleteMapping("/{contentId}")
    public Response deleteContent(@PathVariable Long contentId) {
        if (contentId == null) {
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }
        contentService.remove(contentId);
        return Response.success();
    }

    /**
     * 创建文字图像
     * POST /content/gen-image
     * 
     * @param requestBody 包含description字段的请求体
     * @return Response 包含生成的图片url
     */
    @PostMapping("/gen-image")
    public Response generateImage(@RequestBody String requestBody) {
        // TODO: 调用AI服务生成文字图像
        // TODO: 返回生成的图片url
        return Response.success();
    }

    /**
     * 首页获取所有帖子
     * GET /content/all
     * 
     * @param pageQuery 分页查询参数
     * @param tag 要筛选的标签id（可选）
     * @return Response 包含帖子分页数据
     */
    @GetMapping("/all")
    public Response getAllContent(PageQuery pageQuery, @RequestParam(required = false) Long tag) {
        PageResult result = contentService.getAllContent(pageQuery, tag);
        return Response.success(result);
    }

    /**
     * 获取帖子详情
     * GET /content/view
     * 
     * @param contentId 帖子id
     * @return Response 包含帖子详情
     */
    @GetMapping("/view")
    public Response viewContent(@RequestParam Long contentId) {
        if (contentId == null) {
            throw new BusinessException(HttpCodeEnum.PARAM_ERROR);
        }
        ContentDetailVO contentDetail = contentService.viewContent(contentId);
        return Response.success(contentDetail);
    }

    // ========================= B端管理接口 ==============================
    /**
     * 获取所有帖子-B端
     * GET /content/b
     * 
     * @param pageNum 分页参数
     * @param pageSize 分页参数
     * @param status 帖子状态筛选
     * @param contentType 内容类型筛选
     * @param startTime 创建时间筛选（开始）
     * @param endTime 创建时间筛选（结束）
     * @param title 标题筛选
     * @return Response 包含帖子分页数据
     */
    @GetMapping("/b")
    public Response getAllContentForAdmin(
            @RequestParam(required = true) Integer pageNum,
            @RequestParam(required = true) Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String title) {
        PageResult page = contentService.getContentForAdmin(pageNum, pageSize, status, contentType, startTime, endTime, title);
        return Response.success(page);
    }
    
/*
    /**
     * 通过审核/解封-B端
     * PUT /content/b/{contentId}
     * 
     * @param contentId 帖子id
     * @return Response 操作结果
     *//*
    @PutMapping("/b/{contentId}")
    public Response approveContent(@PathVariable Integer contentId) {
        // TODO: 验证管理员权限
        contentService.approveContent(contentId);
        return Response.success();
    }

    *//**
     * 封禁帖子-B端
     * DELETE /content/b/{contentId}
     * 
     * @param contentId 帖子id
     * @return Response 操作结果
     *//*
    @DeleteMapping("/b/{contentId}")
    public Response banContent(@PathVariable Integer contentId) {
        // TODO: 验证管理员权限
        boolean result = contentService.banContent(contentId);
        return Response.success(result);
    }

    *//**
     * 获取用户总点赞/收藏数
     * GET /content/data
     * 该接口用于后端OpenFeign调用，前端无需调用
     * 
     * @param userId 用户id
     * @return Response 包含点赞数和收藏数
     *//*
    @GetMapping("/data")
    public Response getContentData(@RequestParam Integer userId) {
        UserContentDataVO vo = contentService.getContentData(userId);
        return Response.success(vo);
    }*/
}

