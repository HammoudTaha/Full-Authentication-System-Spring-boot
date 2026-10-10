package com.example.first_project.model;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

public class JwtToken {
    @Getter
    private final String token;
    private final Claims claims;
    private final SecretKey key;

    public JwtToken(String token, String jwtSecret) {
        this.token = token;
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.claims = parseClaims();
    }

    private Claims parseClaims() {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractSessionId() {
        String sessionId = claims.get("sid", String.class);
        return UUID.fromString(sessionId);
    }

    public UUID extractDeviceId() {
        String deviceId = claims.get("did", String.class);
        return UUID.fromString(deviceId);
    }

    public UUID extractUserId() {
        String userId = claims.getSubject();
        return UUID.fromString(userId);
    }

    public long extractIssuedAt() {
        return claims.getIssuedAt().getTime();
    }

    public long getExpirationTime() {
        return claims.getExpiration().getTime();
    }

    public long getRemainingLifetime() {
        long remainTime = getExpirationTime() - System.currentTimeMillis();
        return remainTime > 0 ? remainTime : 0;
    }

    public boolean isValid() {
        return !isExpired();
    }

    public boolean isExpired() {
        return claims.getExpiration().before(new Date());
    }

}
