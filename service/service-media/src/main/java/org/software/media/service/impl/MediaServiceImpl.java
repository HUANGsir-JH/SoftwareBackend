package org.software.media.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.software.media.service.MediaService;
import org.software.media.util.R2OSSUtil;
import org.software.model.constants.HttpCodeEnum;
import org.software.model.media.Media;
import org.software.model.media.UploadD;
import org.software.model.media.UploadTotalD;
import org.software.model.media.UploadV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MediaServiceImpl implements MediaService {

    @Autowired
    private R2OSSUtil r2OSSUtil;

    @Override
    public List<UploadV> upload(UploadTotalD uploadTotalD) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("生成预签名URL | userId: {} | count: {}", userId, uploadTotalD.getCount());

        List<UploadV> list = new ArrayList<>();
        for (int i=0; i<uploadTotalD.getCount(); i++) {
            Media media = uploadTotalD.getMedias()[i];
            UploadD uploadD = UploadD.builder()
                    .count(1)
                    .type(uploadTotalD.getType())
                    .contentType(media.getContentType())
                    .contentLength(Long.valueOf(media.getContentLength()))
                    .filename(media.getFilename())
                    .build();
            UploadV uploadV = r2OSSUtil.generatePresignedUploadInfo(userId, uploadD, 10);
            list.add(uploadV);
        }
        
        log.info("{} | userId: {} | generated: {}", HttpCodeEnum.SUCCESS.getMsg(), userId, list.size());
        return list;
    }
}
