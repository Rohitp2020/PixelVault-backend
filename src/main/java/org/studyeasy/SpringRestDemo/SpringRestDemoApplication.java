package org.studyeasy.SpringRestDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.studyeasy.SpringRestDemo.config.RsaKeyProperties;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyProperties.class)
@SecurityScheme(name = "studyeasy-demo-api",scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)  // this will bring the authorize bar for JWT authentication
public class SpringRestDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringRestDemoApplication.class, args);
	}

}
