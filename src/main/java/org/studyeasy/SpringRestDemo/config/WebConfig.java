package org.studyeasy.SpringRestDemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // This class helps me to show images in my front end.
    // With WebConfig, Spring understands you’re requesting a static resource and serves it correctly.
    // Without WebConfig, Spring thought you were asking for a missing REST endpoint → 404.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Maps all requests starting with /uploads/ to the actual folder in your project
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("classpath:/static/uploads/"); // if uploads folder is at project path
    
    }
}

