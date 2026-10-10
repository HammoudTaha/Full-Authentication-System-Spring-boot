package com.example.first_project.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.first_project.utils.OTPType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
@Entity
@Table(name = "otp_codes",indexes = {
        @Index(columnList = "phone,type"),
})
@Getter
@Setter
public class OTPCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private String codeHash;
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    @Column(nullable = false)
    private int attempts=0;
    @Column(nullable = false)
    private int maxAttempts = 5;
    @Column(nullable = false)
    private String type = OTPType.VERIFICATION.toString();
    private LocalDateTime consumedAt;
    private LocalDateTime invalidateAt;
    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean isValid() {
        return consumedAt == null
                && LocalDateTime.now().isBefore(expiresAt)
                && attempts < maxAttempts;
    }
    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }
    public boolean isConsumed() {
        return consumedAt != null;
    }

    public boolean canAttempt(int maxAttempts) {
        return attempts < maxAttempts;
    }
    public void incrementAttempts() {
        attempts++;
    }
    public boolean isInvalidate() {
        return invalidateAt != null;
    }


    public void invalidate(LocalDateTime now){
        if (isExpired(now) || isConsumed()) return;
        if (isInvalidate()) return;
        invalidateAt = now;
    }

    public OTPType getType() {
        return OTPType.valueOf(type);
    }

    public void consume(LocalDateTime now) {
        consumedAt = now;
    }
}
