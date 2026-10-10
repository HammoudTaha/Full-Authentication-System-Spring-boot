package com.example.first_project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Setter
@Getter
@Table(name = "password_reset_tokens",
        indexes = {
                @Index(name = "idx_password_reset_token_user_id", columnList = "user_id"),
                @Index(name = "idx_password_reset_token_token", columnList = "reset_token_hash")
        })
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "reset_token_hash", nullable = false, unique = true)
    private String resetTokenHash;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }
    public boolean isUsed() {
        return usedAt != null;
    }
    public void markAsUsed(LocalDateTime now) {
        this.usedAt = now;
    }
}
