package com.tvscs.bgv.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private String adminEmail;
    private String baseUrl;
    private int verificationMaxAttempts = 3;
    private String corsAllowedOrigins;

    @Data
    public static class Jwt {
        private String secret;
        private long expirationMs;
    }

    private Mail mail = new Mail();

    @Data
    public static class Mail {
        private String from;
    }
}
