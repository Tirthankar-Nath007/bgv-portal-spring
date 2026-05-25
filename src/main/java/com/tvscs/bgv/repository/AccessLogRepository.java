package com.tvscs.bgv.repository;

import com.tvscs.bgv.domain.entity.AccessLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    @Query("SELECT a FROM AccessLog a WHERE (:status IS NULL OR a.status = :status) AND (:role IS NULL OR a.role = :role) ORDER BY a.timestamp DESC")
    Page<AccessLog> findByFilters(String status, String role, Pageable pageable);
}
