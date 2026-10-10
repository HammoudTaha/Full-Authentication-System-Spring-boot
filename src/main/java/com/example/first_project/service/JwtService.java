package com.example.first_project.service;
import com.example.first_project.entity.Device;
import com.example.first_project.entity.User;
import com.example.first_project.model.JwtToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import io.jsonwebtoken.Jwts;

import java.util.UUID;

@Service
public class JwtService {
    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMinutes}")
    public int jwtExpirationMinutes;

    public long jwtExpirationMilliseconds() {
        return System.currentTimeMillis() + (long) jwtExpirationMinutes * 60 * 1000;
    }



    private SecretKey generateKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user, Device device, UUID familyId) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("sid", familyId)
                .claim("did",device.getId().toString())
                .issuedAt(new java.util.Date())
                .expiration(new java.util.Date(System.currentTimeMillis() + (long) jwtExpirationMinutes * 60 * 1000))
                .signWith(generateKey())
                .compact();
    }

    public JwtToken parseToken(String token) {
        return new JwtToken(token, jwtSecret);
    }

    public UUID extractSessionId(String token){
        return parseToken(token).extractSessionId();
    }

    public UUID extractDeviceId(String token){
        return parseToken(token).extractDeviceId();
    }

    public UUID extractUserId(String token){
        return parseToken(token).extractUserId();
    }

    public Long extractIssuedAt(String token){
        return parseToken(token).extractIssuedAt();
    }

    public long getExpirationTime(String token) {
        return parseToken(token).getExpirationTime();
    }

    public long getRemainingLifetime(String token) {
        return parseToken(token).getRemainingLifetime();
    }

    public boolean isTokenValid(String token) {
        return parseToken(token).isValid();
    }
}
