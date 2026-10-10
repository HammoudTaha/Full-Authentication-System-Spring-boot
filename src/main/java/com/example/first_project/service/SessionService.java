package com.example.first_project.service;

import com.example.first_project.dto.AuthResponseDTO;
import com.example.first_project.dto.RefreshTokenResponseDTO;
import com.example.first_project.dto.RequestMetadata;
import com.example.first_project.entity.Device;
import com.example.first_project.entity.RefreshToken;
import com.example.first_project.entity.User;
import com.example.first_project.exception.BaseApiException;
import com.example.first_project.repository.RefreshTokenRepository;
import com.example.first_project.utils.HashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
@Service
public class SessionService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    @Value("${app.refresh-token-expiration-minutes:10080}")
    private int refreshTokenExpirationMinutes;
    private final Clock clock;
    private final TokenBlacklistService tokenBlacklistService;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionService(RefreshTokenRepository refreshTokenRepository, JwtService jwtService, TokenBlacklistService tokenBlacklistService, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.clock = clock;
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    @Transactional
    public RefreshTokenResponseDTO createSession(Device device, RequestMetadata metadata) {
        LocalDateTime now = now();
        List<UUID> oldSids = refreshTokenRepository.findActiveFamilyIdsByDevice(device.getId());
        refreshTokenRepository.revokeActiveByDeviceId(device.getId(),now);
        oldSids.forEach(sid -> tokenBlacklistService.addToBlacklist("SESSION:"+sid, jwtService.jwtExpirationMilliseconds()));
        UUID familyId = UUID.randomUUID();
        device.setLastUsedAt(now);
        return createTokenForFamily(device,familyId,metadata);
    }


    private AuthResponseDTO generateAccessToken(User user,Device device,UUID familyId) {
        String token = jwtService.generateToken(user,device,familyId);
        AuthResponseDTO authResponseDTO = new AuthResponseDTO();
        authResponseDTO.setToken(token);
        authResponseDTO.setExpirationMinutes(jwtService.jwtExpirationMinutes);
        return authResponseDTO;
    }

    @Transactional(noRollbackFor = BaseApiException.class)
    public RefreshTokenResponseDTO verifyAndRotate(String token, RequestMetadata metadata) {
        LocalDateTime now = now();
        String tokenHash = HashUtil.hash(token);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BaseApiException("Invalid refresh token"));
        if (refreshToken.isExpired(now)) {
            throw new BaseApiException("Refresh token has expired");
        }
        if (refreshToken.isRevoked()) {
            refreshTokenRepository.revokeActiveByFamilyId(refreshToken.getFamilyId(),now);
            throw new BaseApiException("Refresh token has been revoked");
        }
        refreshToken.revoke(now);
        return createTokenForFamily(refreshToken.getDevice(),refreshToken.getFamilyId(),metadata);
    }


    private RefreshTokenResponseDTO createTokenForFamily(Device device, UUID familyId, RequestMetadata metadata) {
        LocalDateTime now = now();
        String token = generateRefreshToken();
        String tokenHash = HashUtil.hash(token);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setDevice(device);
        refreshToken.setFamilyId(familyId);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(now.plusMinutes(refreshTokenExpirationMinutes));
        refreshToken.setIpAddress(metadata.ipAddress());
        refreshToken.setUserAgent(metadata.userAgent());
        refreshTokenRepository.save(refreshToken);
        AuthResponseDTO access = generateAccessToken(device.getUser(),device,familyId);
        AuthResponseDTO refresh = new AuthResponseDTO();
        refresh.setToken(token);
        refresh.setTokenType("Refresh");
        refresh.setExpirationMinutes(refreshTokenExpirationMinutes);
        return new RefreshTokenResponseDTO(access, refresh);
    }

    @Transactional
    public void revokeAllUserSessions(User user){
        UUID userId = user.getId();
        refreshTokenRepository.revokeActiveByUser(userId,now());
        tokenBlacklistService.addToBlacklist("USER:"+userId.toString(), jwtService.jwtExpirationMilliseconds());
    }

    @Transactional
    public void revokeDeviceSessions(String accessToken) {
        UUID deviceId = jwtService.extractDeviceId(accessToken);
        UUID sessionId = jwtService.extractSessionId(accessToken);
        refreshTokenRepository.revokeActiveByDeviceId(deviceId, now());
        tokenBlacklistService.addToBlacklist("SESSION:"+sessionId.toString(), jwtService.jwtExpirationMilliseconds());
    }

    public LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
