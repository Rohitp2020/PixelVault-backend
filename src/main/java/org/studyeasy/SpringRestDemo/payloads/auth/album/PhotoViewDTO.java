package org.studyeasy.SpringRestDemo.payloads.auth.album;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PhotoViewDTO {

    private long id;

    @NotBlank
    @Schema(description = "Photo name", example = "xyz", requiredMode = RequiredMode.REQUIRED)
    private String photo_name;

    @NotBlank
    @Schema(description = "Album description", example = "Description", requiredMode = RequiredMode.REQUIRED)
    private String description;

    //@NotBlank
    @Schema(description = "Album photo fileName", example = "FileName")
    private String fileName;

}
