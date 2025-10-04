package org.studyeasy.SpringRestDemo.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

// here we are adding information on our swagger UI.
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Demo API",
        version = "Versions 1.0",
        contact = @Contact(
            name = "StudyEasy", email = "rohit@gmail.com", url = "https://studyeasy.org"
        ),
        license = @License(
            name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0"
        ),
        termsOfService = "https://studyeasy.org",
        description = "SpringBoot RESTFul API demo by Rohit"
    )
)
public class SwaggerConfig {
    
}
