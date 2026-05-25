package com.tvscs.bgv.service;

import com.tvscs.bgv.domain.dto.response.AccessLogResponse;
import com.tvscs.bgv.domain.dto.response.DashboardStatsResponse;
import com.tvscs.bgv.domain.dto.response.VerifierProfileResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface AdminService {
    DashboardStatsResponse getDashboard();
    List<VerifierProfileResponse> getAllVerifiers();
    VerifierProfileResponse toggleVerifier(Long verifierId);
    List<Map<String, Object>> getBlockedVerifiers();
    Page<AccessLogResponse> getAccessLogs(String status, String role, Pageable pageable);
    byte[] exportVerificationsAsExcel();
}
