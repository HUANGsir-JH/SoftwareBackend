package org.software.media.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import org.software.media.service.MediaService;
import org.software.media.util.R2OSSUtil;
import org.software.model.content.media.UploadD;
import org.software.model.content.media.UploadV;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MediaServiceImpl implements MediaService {

    @Autowired
    private R2OSSUtil r2OSSUtil;

    @Override
    public List<UploadV> upload(UploadD uploadD) {
        Long userId = StpUtil.getLoginIdAsLong();

        List<UploadV> list = new ArrayList<>();
        for (int i=0; i<uploadD.getCount(); i++) {
            UploadV uploadV = r2OSSUtil.generatePresignedUploadInfo(userId, uploadD, 10);
            list.add(uploadV);
        }
        return list;
    }
}
