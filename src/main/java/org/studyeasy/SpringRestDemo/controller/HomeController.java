package org.studyeasy.SpringRestDemo.controller;

import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@CrossOrigin(origins = "http://localhost:3000/", maxAge = 3600)
public class HomeController {

    //Just look at the front-end of our swagger UI, you will find the / GET controller there
    @GetMapping("/")
    public String demo(){
        return "Hello world.";
    }

    //Just look at the front-end of our swagger UI, you will find the /test GET controller there
    @GetMapping("/test")
    @Tag(name = "Test", description = "The Test API.")
    @SecurityRequirement(name = "studyeasy-demo-api")  // securing our endpoint with JWT
    public String test(){
        return "Test API.";
    }

    
}
