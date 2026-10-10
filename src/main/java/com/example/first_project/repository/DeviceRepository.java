package com.example.first_project.repository;

import com.example.first_project.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    @Query("SELECT d FROM Device d WHERE d.deviceId = :deviceId AND d.user.id = :userId")
    Optional<Device> findByDeviceIdAndUserId(String deviceId, UUID userId);
}
