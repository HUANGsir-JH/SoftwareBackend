package org.software.media.controller;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.software.media.service.MediaService;
import org.software.media.util.R2OSSUtil;
import org.software.model.Response;
import org.software.model.constants.MediaContants;
import org.software.model.content.media.UploadD;
import org.software.model.content.media.UploadV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private MediaService mediaService;

    @GetMapping("/signature")
    public Response upload(UploadD uploadD) {
        // TODO:
        List<UploadV> list = mediaService.upload(uploadD);
        return Response.success(list);
    }
}
