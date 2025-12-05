package org.software.media.service;

import org.software.model.media.UploadD;
import org.software.model.media.UploadTotalD;
import org.software.model.media.UploadV;

import java.util.List;

public interface MediaService {

    List<UploadV> upload(UploadTotalD uploadD);
}
