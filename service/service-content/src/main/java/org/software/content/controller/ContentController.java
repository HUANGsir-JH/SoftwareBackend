package org.software.content.controller;

import cn.dev33.satoken.stp.StpUtil;
import org.software.content.dto.ContentDetailDto;
import org.software.content.dto.UserContentDataVO;
import org.software.content.service.ContentService;
import org.software.model.Response;
import org.software.model.content.Content;
import org.software.model.content.ContentD;
import org.software.model.content.post.PostD;
import org.software.model.content.post.PostPage;
import org.software.model.page.PageQuery;
import org.software.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;


/**
 * 内容主表(Content)表控制层
 *
 * @author Ra1nbot
 * @since 2025-11-11 09:40:54
 */
@RestController
@RequestMapping("/content")
public class ContentController{

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
    public Response create(@RequestBody ContentD contentD){
        Long userId = StpUtil.getLoginIdAsLong();
        // 调用 service 层创建帖子
        Long result = contentService.create(contentD);
        return Response.success(result);
    }

    /**
     * 获取我的帖子/草稿
     * GET /content
     */
    @GetMapping
    public Response getMyContent(PageQuery pageQuery){
        // TODO: 从token中获取userId
        Long userId = StpUtil.getLoginIdAsLong();
        // TODO: 实现分页查询我的帖子
        PostPage result = contentService.getMyContent(pageQuery, userId);
        return Response.success(result);
    }

    /**
     * 更新帖子
     * PUT /content
     * 这里需要传递contentid吧
     */
    @PutMapping("/{contentId}")
    public Response updateContent(@RequestBody Content content){
        // TODO: 验证当前用户是否有权限更新该帖子
        // TODO: 实现帖子更新逻辑
        if (content.getContentId() == null) {
            return Response.error();
        }
        contentService.updatePost(content);
        return Response.success();

    }

    /**
     * 删除帖子
     * DELETE /content/{contentId}
     */
    @DeleteMapping("/{contentId}")
    public Response deleteContent(@PathVariable Integer contentId){
        // TODO: 验证当前用户是否有权限删除该帖子
        // TODO: 实现软删除逻辑
        if (contentId == null) {
            return Response.error();
        }
        contentService.deleteContent(contentId);
        return Response.success();
    }

    /**
     * 获取帖子详情
     * GET /content/view
     */
    @GetMapping("/view")
    public Response viewContent(@RequestParam Integer contentId){
        // TODO: 实现帖子详情查询
        // TODO: 包含帖子的媒体文件、标签、点赞收藏数等信息
        if (contentId == null) {
            return Response.error();
        }
       ContentDetailDto contentd= contentService.viewContent(contentId);
        return Response.success(contentd);
    }

    /**
     * 上传帖子图像
     * POST /content/{contentId}/image
     */
    @PostMapping("/{contentId}/image")
    public Response uploadImage(@PathVariable Integer contentId, @RequestParam("file") MultipartFile file){
        // TODO: 验证帖子所有权
        // TODO: 调用媒体服务上传图片
        // TODO: 保存媒体文件关联信息
        return Response.success();
    }

    /**
     * 上传帖子视频
     * POST /content/{contentId}/video
     */
    @PostMapping("/{contentId}/video")
    public Response uploadVideo(@PathVariable Integer contentId, @RequestParam("file") MultipartFile file){
        // TODO: 验证帖子所有权
        // TODO: 调用媒体服务上传视频
        // TODO: 保存媒体文件关联信息
        return Response.success();
    }

    /**
     * 创建文字图像
     * POST /content/gen-image
     */
    //暂未完成
    @PostMapping("/gen-image")
    public Response generateImage(@RequestBody String text){
        // TODO: 调用AI服务生成文字图像
        return Response.success();
    }

    /**
     * 获取所有帖子
     * GET /content/all
     */
    @GetMapping("/all")
    public Response getAllContent(PageQuery pageQuery){
        // TODO: 从token中获取userId
        Long userId = StpUtil.getLoginIdAsLong();
        PostPage result = contentService.getAllContent(pageQuery, userId);
        return Response.success(result);

    }

    /**
     * 获取指定好友帖子
     * GET /content/single/friend
     */
    @GetMapping("/single/friend")
    public Response getSingleFriendContent(@RequestParam Integer friendId, PageQuery pageQuery){
        // TODO: 验证好友关系
        // TODO: 查询指定好友的帖子（分页）
        return Response.success();
    }

    // ========================= B端管理接口 ==============================

    /**
     * 获取所有帖子-B端
     * GET /content/b
     */
    @GetMapping("/b")
    public Response getAllContentForAdmin(PageQuery pageQuery, 
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String contentType,
                                          @RequestParam(required = false) String startTime,
                                          @RequestParam(required = false) String title){
        // TODO: 实现管理员查询所有帖子
        // TODO: 支持按状态、类型筛选
        PostPage page = contentService.getAllContentForAdmin(pageQuery,  status,contentType,startTime,title);
        return Response.success(page);
    }

    /**
     * 通过审核-B端
     * PUT /content/b/{contentId}
     */
    @PutMapping("/b/{contentId}")
    public Response approveContent(@PathVariable Integer contentId){
        // TODO: 验证管理员权限how？
        // TODO: 更新帖子状态为已审核
        /*User currentUser = userService.getCurrentUser(); // 你项目中已有
        if (currentUser == null || !"admin".equals(currentUser.getRole())) {
            throw new RuntimeException("无权限：只有管理员可以审核帖子");
        }
         */
        contentService.approveContent(contentId);
        return Response.success();
    }

    /**
     * 封禁帖子-B端
     * DELETE /content/b/{contentId}
     */
    @DeleteMapping("/b/{contentId}")
    public Response banContent(@PathVariable Integer contentId){
        // TODO: 验证管理员权限 how？
        // TODO: 更新帖子状态为已封禁

        boolean result = contentService.banContent(contentId);
        return Response.success();
    }

    @GetMapping("/content/data")
    public Response getContentData(@RequestParam Integer contentId){
        UserContentDataVO vo = contentService.getContentData(contentId);
        return Response.success(vo);
    }
}

