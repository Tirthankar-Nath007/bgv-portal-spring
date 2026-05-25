package com.tvscs.bgv.security;

import com.tvscs.bgv.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final AppProperties appProperties;

    // In-memory revoked token store (jti values)
    private final Set<String> revokedTokens = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long id, String identifier, String userType, String role, String displayName) {
        String jti = UUID.randomUUID().toString();
        String subject = userType + ":" + identifier;
        return Jwts.builder()
                .id(jti)
                .subject(subject)
                .claim("userId", id)
                .claim("userType", userType)
                .claim("role", role)
                .claim("displayName", displayName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + appProperties.getJwt().getExpirationMs()))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractSubject(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractJti(String token) {
        return extractAllClaims(token).getId();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (revokedTokens.contains(claims.getId())) {
                return false;
            }
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public void revokeToken(String token) {
        try {
            String jti = extractJti(token);
            revokedTokens.add(jti);
        } catch (Exception ignored) {
        }
    }
}
