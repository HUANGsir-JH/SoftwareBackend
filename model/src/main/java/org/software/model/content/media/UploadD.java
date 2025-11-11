package org.software.model.content.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadD {
    private Integer count;
    private String type;
    private String contentType;
    private Long contentLength;
    private String fileName;
}
