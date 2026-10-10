package com.example.first_project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "devices",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","device_id"}),
        indexes = {
                @Index(name = "idx_device_user_id", columnList = "user_id"),
                @Index(name = "idx_device_device_id", columnList = "device_id")
        }
)
@Setter
@Getter
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;
    @Column(name = "device_id",nullable = false,updatable = false)
    private String deviceId;
    @Column(nullable = false)
    private String deviceName;
    @Column(nullable = false)
    private boolean trusted = false;
    @Column(nullable = false)
    private LocalDateTime lastUsedAt;
    @Column(nullable = false,updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
