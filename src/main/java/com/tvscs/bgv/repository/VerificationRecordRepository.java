package com.tvscs.bgv.repository;

import com.tvscs.bgv.domain.entity.VerificationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VerificationRecordRepository extends JpaRepository<VerificationRecord, Long> {

    @Query("SELECT v FROM VerificationRecord v WHERE UPPER(v.verificationId) = UPPER(:verificationId)")
    Optional<VerificationRecord> findByVerificationIdIgnoreCase(String verificationId);

    List<VerificationRecord> findByVerifierIdOrderByCreatedAtDesc(Long verifierId);

    Page<VerificationRecord> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByOverallStatus(String status);

    @Query("SELECT v FROM VerificationRecord v WHERE v.createdAt >= :from ORDER BY v.createdAt DESC")
    List<VerificationRecord> findRecentSince(LocalDateTime from);
}
