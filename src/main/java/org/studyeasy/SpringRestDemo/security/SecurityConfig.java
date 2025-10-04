package org.studyeasy.SpringRestDemo.security;

import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.studyeasy.SpringRestDemo.config.RsaKeyProperties;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.var;

@SuppressWarnings("unused")
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RsaKeyProperties rsaKeys;

    public SecurityConfig(RsaKeyProperties rsaKeys){
        this.rsaKeys = rsaKeys;
    }

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // When you want to have only one username and password for login without database
    // @Bean
    // public InMemoryUserDetailsManager users(){
    //     return new InMemoryUserDetailsManager(
    //         User.withUsername("rohit")
    //             .password("{noop}rohit")
    //             .authorities("read")
    //             .build()
    //     );
    // }

    @SuppressWarnings("deprecation")
    @Bean
    public AuthenticationManager authManager(UserDetailsService userDetailsService){
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return new ProviderManager(authProvider);
    }

    @Bean
    public RSAPublicKey publicKey() throws IOException, Exception {
        return loadPublicKey(rsaKeys.publicKeyPath());
    }

    @Bean
    public RSAPrivateKey privateKey() throws IOException, Exception {
        return loadPrivateKey(rsaKeys.privateKeyPath());
    }

    @Bean
    JwtDecoder jwtDecoder(RSAPublicKey publicKey){
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    public JwtEncoder jwtEncoder(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        JWK jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }

    // ----- Utility Methods -----
    private RSAPublicKey loadPublicKey(org.springframework.core.io.Resource resource) throws Exception {
        String key = Files.readString(resource.getFile().toPath())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }

    private RSAPrivateKey loadPrivateKey(org.springframework.core.io.Resource resource) throws Exception {
        String key = Files.readString(resource.getFile().toPath())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(spec);
    }

    @SuppressWarnings("removal")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http            
                        
                        .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/**").permitAll()
                        .requestMatchers("/token").permitAll()

                        .requestMatchers("/api/v1/albums/add").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers("/api/v1/albums/fetch").permitAll()
                        .requestMatchers("/api/v1/albums/photos").permitAll()
                        .requestMatchers("/api/v1/albums/{album_id}/photos").hasAuthority("SCOPE_ADMIN")

                        .requestMatchers("/api/v1/auth/token").permitAll()
                        .requestMatchers("/api/v1/auth/users").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers("/api/v1/auth/users/{user_id}/update_authority").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers("/api/v1/auth/users/add").permitAll()
                        .requestMatchers("/api/v1/auth/profile").permitAll()
                        .requestMatchers("/api/v1/auth/profile/update_password").permitAll()
                        .requestMatchers("/api/v1/auth/profile/delete").permitAll()
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/db-console/**").permitAll()
                        .requestMatchers("/test")
                        .authenticated())
                        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))  //oauth2 is used here for JWT(JSON Web tokens) tokens
                        .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); //JWT tokens made stateless here

                        http.csrf(csrf -> csrf.disable());
                        http.headers(headers -> headers.frameOptions().disable()); 
                        
                        return http.build();
            
    }
    
}
