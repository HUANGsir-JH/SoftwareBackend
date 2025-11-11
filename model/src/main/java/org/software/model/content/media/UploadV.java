package org.software.model.content.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadV {

    private String presignedUrl;
    private String objectKey;
    private String expiresIn;
}
