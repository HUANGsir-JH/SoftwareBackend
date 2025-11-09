package org.software.user.controller;

import org.software.model.Response;
import org.software.model.page.PageQuery;
import org.software.model.user.PageUserDTO;
import org.software.model.user.PasswordView;
import org.software.model.user.User;
import org.software.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public Response getUser(String userId) {
        return Response.success(userService.getById(userId));
    }

    @PutMapping
    public Response updateUser(@RequestBody User user) {
        // TODO:
        return null;
    }

    @PutMapping("/password")
    public Response updatePassword(@RequestBody PasswordView passV) {

        return null;
    }

    @PutMapping("/avatar")
    public Response uploadAvatar(MultipartFile file) {
        // TODO:
        return null;
    }

    @PutMapping("/backImage")
    public Response uploadBackImage(MultipartFile file) {
        // TODO
        return null;
    }

// ========================= B端 ==============================

    @GetMapping("/b")
    public Response getUserBatch(PageQuery pageQuery, PageUserDTO pageUserDTO) {
        return null;
    }

    @DeleteMapping("/b/{userId}")
    public Response banUser(@PathVariable String userId) {
        return null;
    }

    @PutMapping("/b/{userId}")
    public Response updateBannedUserStatus(@PathVariable String userId) {
        return null;
    }



}
