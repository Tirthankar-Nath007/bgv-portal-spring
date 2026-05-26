package com.tvscs.bgv.service.impl;

import com.tvscs.bgv.domain.entity.AccessLog;
import com.tvscs.bgv.repository.AccessLogRepository;
import com.tvscs.bgv.service.AccessLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessLogServiceImpl implements AccessLogService {

    private final AccessLogRepository accessLogRepository;

    @Override
    public void log(String email, String role, String action, String status,
                    String failureReason, HttpServletRequest request) {
        try {
            AccessLog entry = AccessLog.builder()
                    .email(email)
                    .role(role)
                    .action(action)
                    .status(status)
                    .failureReason(failureReason)
                    .ipAddress(getClientIp(request))
                    .userAgent(request != null ? request.getHeader("User-Agent") : null)
                    .build();
            accessLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to write access log: {}", e.getMessage());
        }
    }

    @Override
    public Page<AccessLog> getLogs(String status, String role, Pageable pageable) {
        String s = (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) ? status : null;
        String r = (role != null && !role.isBlank() && !"ALL".equalsIgnoreCase(role)) ? role : null;
        return accessLogRepository.findByFilters(s, r, pageable);
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
