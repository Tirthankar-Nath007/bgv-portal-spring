package com.tvscs.bgv.repository;

import com.tvscs.bgv.domain.entity.Appeal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppealRepository extends JpaRepository<Appeal, Long> {

    Optional<Appeal> findByAppealId(String appealId);

    Page<Appeal> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<Appeal> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(String status);

    List<Appeal> findByVerifierIdOrderByCreatedAtDesc(Long verifierId);

    boolean existsByVerificationIdAndVerifierId(Long verificationId, Long verifierId);
}
