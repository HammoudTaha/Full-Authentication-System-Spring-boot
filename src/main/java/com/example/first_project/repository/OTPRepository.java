package com.example.first_project.repository;

import com.example.first_project.entity.OTPCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OTPRepository extends JpaRepository<OTPCode, UUID> {
    @Query("""
           select o from OTPCode o
           where o.phone = :phone
             and o.type = :type
             and o.expiresAt > :now
             and o.consumedAt is null
             and o.invalidateAt is null
           order by o.createdAt desc
           """)
   List<OTPCode> findActive(
            String phone,
            String type,
            LocalDateTime now,
            Pageable pageable
    );

   @Lock(LockModeType.PESSIMISTIC_WRITE)
   @Query("select o from OTPCode o where o.id = :id")
   Optional<OTPCode> findByIdForUpdate(UUID id);
}
