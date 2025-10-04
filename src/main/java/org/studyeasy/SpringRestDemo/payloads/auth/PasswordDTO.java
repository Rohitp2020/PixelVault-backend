package org.studyeasy.SpringRestDemo.payloads.auth;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordDTO {
    @Size(min = 5,max = 20)
    @Schema(description = "Password", example = "password",
    requiredMode = RequiredMode.REQUIRED, maxLength = 20, minLength = 5)
    private String password;
}
