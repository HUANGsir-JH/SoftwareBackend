package org.software.media.controller;

import lombok.extern.slf4j.Slf4j;
import org.software.media.service.MediaService;
import org.software.model.Response;
import org.software.model.content.media.UploadD;
import org.software.model.content.media.UploadV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private MediaService mediaService;

    @PostMapping("/signature")
    public Response upload(@RequestBody UploadD uploadD) {
        List<UploadV> list = mediaService.upload(uploadD);
        return Response.success(list);
    }

}
