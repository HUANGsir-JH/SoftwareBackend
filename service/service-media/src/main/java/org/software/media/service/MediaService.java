package org.software.media.service;

import org.software.model.content.media.UploadD;
import org.software.model.content.media.UploadV;

import java.util.List;

public interface MediaService {

    List<UploadV> upload(UploadD uploadD);
}
