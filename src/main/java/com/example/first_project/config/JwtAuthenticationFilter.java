package com.example.first_project.config;

import com.example.first_project.service.CustomUserDetailsService;
import com.example.first_project.utils.ExtractAuthToken;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.example.first_project.service.JwtService;

import io.jsonwebtoken.JwtException;
import com.example.first_project.service.TokenBlacklistService;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final HandlerExceptionResolver resolver;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
            TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.resolver = resolver;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {
        Optional<String> authHeader = ExtractAuthToken.extract(request);
        if (authHeader.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwtToken = authHeader.get();

        try {
            if (!jwtService.isTokenValid(jwtToken)) {
                throw new JwtException("Invalid or expired JWT token");
            }
            validateTokenNotBlacklisted(jwtToken);
            UUID userId = jwtService.extractUserId(jwtToken);
            validateUserNotBlacklisted(userId,jwtToken);
            UUID sessionId = jwtService.extractSessionId(jwtToken);
            validateSessionNotBlacklisted(sessionId);

            if (shouldAuthenticate(userId)) {
                authenticateUser(userId, jwtToken, request);
            }

            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            resolver.resolveException(request, response, null, ex);
        }
    }


    private void validateTokenNotBlacklisted(String jwtToken) throws JwtException {
        if (tokenBlacklistService.isBlacklisted("TOKEN:"+jwtToken)) {
            throw new JwtException("Token is blacklisted");
        }
    }

    private void validateUserNotBlacklisted(UUID userId, String jwtToken) throws JwtException {
        Long expirationTime = tokenBlacklistService.getExpirationTime("USER:"+userId.toString());
        if (expirationTime !=null){
            Long tokenIssuedAt = jwtService.extractIssuedAt(jwtToken);
            if (tokenIssuedAt < expirationTime) {
                throw new JwtException("Token is blacklisted");
            }
        }
    }

    private void validateSessionNotBlacklisted(UUID sessionId) throws JwtException {
        if (tokenBlacklistService.isBlacklisted("SESSION:"+sessionId.toString())) {
            throw new JwtException("Session is blacklisted");
        }
    }

    private boolean shouldAuthenticate(UUID userId) {
        return userId != null && SecurityContextHolder.getContext().getAuthentication() == null;
    }

    private void authenticateUser(UUID userId, String jwtToken, HttpServletRequest request) {
        UserDetails userDetails = this.userDetailsService.loadUserById(userId);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                jwtToken,
                userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

}
