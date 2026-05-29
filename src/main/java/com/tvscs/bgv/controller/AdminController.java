package com.tvscs.bgv.controller;

import com.tvscs.bgv.domain.dto.response.AccessLogResponse;
import com.tvscs.bgv.domain.dto.response.DashboardStatsResponse;
import com.tvscs.bgv.domain.dto.response.VerifierProfileResponse;
import com.tvscs.bgv.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<DashboardStatsResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @GetMapping("/verifiers")
    @Operation(summary = "List all verifiers")
    public ResponseEntity<List<VerifierProfileResponse>> getAllVerifiers() {
        return ResponseEntity.ok(adminService.getAllVerifiers());
    }

    @PostMapping("/verifiers/{id}/toggle")
    @Operation(summary = "Activate or deactivate a verifier")
    public ResponseEntity<VerifierProfileResponse> toggleVerifier(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleVerifier(id));
    }

    @GetMapping("/blocked-verifiers")
    @Operation(summary = "List blocked verifiers")
    public ResponseEntity<List<Map<String, Object>>> getBlockedVerifiers() {
        return ResponseEntity.ok(adminService.getBlockedVerifiers());
    }

    @DeleteMapping("/blocked/{attemptId}")
    @Operation(summary = "Unblock a verifier's blocked attempt record")
    public ResponseEntity<Void> unblockAttempt(@PathVariable Long attemptId) {
        adminService.unblockAttempt(attemptId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/logs")
    @Operation(summary = "Get access logs with optional filters")
    public ResponseEntity<Page<AccessLogResponse>> getLogs(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "") String role,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.getAccessLogs(status, role, email, pageable));
    }

    @GetMapping("/export")
    @Operation(summary = "Export verification data as Excel")
    public ResponseEntity<byte[]> export() {
        byte[] data = adminService.exportVerificationsAsExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bgv-export.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(data);
    }
}
