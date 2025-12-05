package org.software.model.media;

import lombok.Data;

@Data
public class Media {
    private String contentType;
    private String contentLength;
    private String filename;
}
