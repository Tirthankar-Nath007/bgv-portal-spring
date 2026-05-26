package com.tvscs.bgv.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private String adminEmail;
    private String baseUrl;
    private int verificationMaxAttempts = 3;
    private String corsAllowedOrigins;
    private Mail mail = new Mail();

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public int getVerificationMaxAttempts() { return verificationMaxAttempts; }
    public void setVerificationMaxAttempts(int verificationMaxAttempts) { this.verificationMaxAttempts = verificationMaxAttempts; }

    public String getCorsAllowedOrigins() { return corsAllowedOrigins; }
    public void setCorsAllowedOrigins(String corsAllowedOrigins) { this.corsAllowedOrigins = corsAllowedOrigins; }

    public Mail getMail() { return mail; }
    public void setMail(Mail mail) { this.mail = mail; }

    public static class Jwt {
        private String secret;
        private long expirationMs;

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }

        public long getExpirationMs() { return expirationMs; }
        public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
    }

    public static class Mail {
        private String from;

        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
    }
}
