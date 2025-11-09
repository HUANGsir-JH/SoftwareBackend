package org.software.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordView {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
