package com.tvscs.bgv.service;

import com.tvscs.bgv.domain.entity.AccessLog;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccessLogService {
    void log(String email, String role, String action, String status,
             String failureReason, HttpServletRequest request);

    Page<AccessLog> getLogs(String status, String role, String email, Pageable pageable);
}
