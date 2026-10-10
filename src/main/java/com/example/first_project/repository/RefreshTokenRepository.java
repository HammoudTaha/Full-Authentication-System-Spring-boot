package com.example.first_project.repository;

import com.example.first_project.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Query("SELECT DISTINCT r.familyId FROM RefreshToken r WHERE r.device.id = :deviceId AND r.revokedAt IS NULL")
    List<UUID> findActiveFamilyIdsByDevice(UUID deviceId);
    @Modifying
    @Query("""
      UPDATE RefreshToken r SET r.revokedAt = :now WHERE r.device.user.id = :userId AND r.revokedAt IS NULL""")
    void revokeActiveByUser(UUID userId,LocalDateTime now);
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revokedAt = :now WHERE r.familyId = :familyId AND r.revokedAt IS NULL")
    void revokeActiveByFamilyId(UUID familyId, LocalDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revokedAt = :now WHERE r.device.id = :deviceId AND r.revokedAt IS NULL")
    void revokeActiveByDeviceId(UUID deviceId, LocalDateTime now);
}
