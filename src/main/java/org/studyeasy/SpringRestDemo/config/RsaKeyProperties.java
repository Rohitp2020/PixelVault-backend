package org.studyeasy.SpringRestDemo.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;


//Java record — a special type of class introduced in Java 14 (finalized in Java 16)
// that is mainly used to store immutable data without writing a lot of boilerplate code.
@ConfigurationProperties(prefix = "rsa")
public record RsaKeyProperties(Resource publicKeyPath, Resource privateKeyPath) {
    
}
