package com.tvscs.bgv.repository;

import com.tvscs.bgv.domain.entity.VerificationAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VerificationAttemptRepository extends JpaRepository<VerificationAttempt, Long> {

    @Query("SELECT va FROM VerificationAttempt va WHERE va.verifierId = :verifierId AND UPPER(va.employeeId) = UPPER(:employeeId)")
    Optional<VerificationAttempt> findByVerifierIdAndEmployeeIdIgnoreCase(Long verifierId, String employeeId);

    @Query("SELECT va FROM VerificationAttempt va WHERE va.isBlocked = true OR va.blockedAt IS NOT NULL ORDER BY va.blockedAt DESC NULLS LAST")
    List<VerificationAttempt> findAllBlocked();
}
