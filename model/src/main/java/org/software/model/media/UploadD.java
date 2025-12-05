package org.software.model.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadD {
    private Integer count = 1;
    private String type;
    private String contentType;
    private Long contentLength;
    private String filename;
}
