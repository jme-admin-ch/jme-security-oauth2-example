package ch.admin.bit.jeap.jme.security.oauth.resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * When including the jeap-spring-boot-security-starter dependency and providing the matching configuration properties
 * all web endpoints of the application will be automatically protected by OAuth2 as a default. Please refer to module
 * `jme-security-oauth2-resource-service` on how to overwrite the defaults provided by jeap.
 */
@Configuration
public class WebSecurityConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${jme.security.cors.allowed-origins:http://localhost:4200}") List<String> allowedOrigins) {
        //Define you own CORS configuration. This e.g. lets a UI deployed under localhost:4200 access
        //POST and GET Endpoint. You might need to set this environment dependant (e.g. not on PROD)
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
