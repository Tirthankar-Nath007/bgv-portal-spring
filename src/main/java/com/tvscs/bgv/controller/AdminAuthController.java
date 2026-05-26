package com.tvscs.bgv.controller;

import com.tvscs.bgv.domain.dto.request.AdminLoginRequest;
import com.tvscs.bgv.domain.dto.request.CreateAdminRequest;
import com.tvscs.bgv.domain.dto.response.AdminAuthResponse;
import com.tvscs.bgv.domain.dto.response.AdminProfileResponse;
import com.tvscs.bgv.service.AdminAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@Tag(name = "Admin Auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    @Operation(summary = "Admin login")
    public ResponseEntity<AdminAuthResponse> login(@Valid @RequestBody AdminLoginRequest req,
                                                   HttpServletRequest httpReq) {
        return ResponseEntity.ok(adminAuthService.login(req, httpReq));
    }

    @PostMapping("/register")
    @Operation(summary = "Create a new admin user")
    public ResponseEntity<AdminProfileResponse> createAdmin(@Valid @RequestBody CreateAdminRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminAuthService.createAdmin(req));
    }
}
