package org.studyeasy.SpringRestDemo.payloads.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginDTO {

    @Email
    @Schema(description = "Email address", example = "admin@gmail.com",
     requiredMode = RequiredMode.REQUIRED)
    private String email;

    @Size(min = 5,max = 20)
    @Schema(description = "Password", example = "password",
     requiredMode = RequiredMode.REQUIRED, maxLength = 20, minLength = 5)
    private String password;
    
}
