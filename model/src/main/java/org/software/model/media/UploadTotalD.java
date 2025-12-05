package org.software.model.media;

import lombok.Data;

@Data
public class UploadTotalD {
    private int count;
    private String type;
    private Media[] medias;
}
