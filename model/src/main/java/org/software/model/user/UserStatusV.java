package org.software.model.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatusV {
    private User user;
    private String status;
}
