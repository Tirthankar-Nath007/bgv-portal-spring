package com.tvscs.bgv.controller;

import com.tvscs.bgv.domain.dto.request.LoginVerifierRequest;
import com.tvscs.bgv.domain.dto.request.RegisterVerifierRequest;
import com.tvscs.bgv.domain.dto.request.UpdateVerifierRequest;
import com.tvscs.bgv.domain.dto.request.VerifyOtpRequest;
import com.tvscs.bgv.domain.dto.response.AuthResponse;
import com.tvscs.bgv.domain.dto.response.LoginInitResponse;
import com.tvscs.bgv.domain.dto.response.VerifierProfileResponse;
import com.tvscs.bgv.security.UserPrincipal;
import com.tvscs.bgv.service.VerifierAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Verifier Auth")
@RequiredArgsConstructor
public class VerifierAuthController {

    private final VerifierAuthService verifierAuthService;

    public VerifierAuthController(VerifierAuthService verifierAuthService) {
        this.verifierAuthService = verifierAuthService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new verifier company")
    public ResponseEntity<VerifierProfileResponse> register(@Valid @RequestBody RegisterVerifierRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(verifierAuthService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Verifier login — returns requireOtp flag")
    public ResponseEntity<LoginInitResponse> login(@Valid @RequestBody LoginVerifierRequest req,
                                                   HttpServletRequest httpReq) {
        return ResponseEntity.ok(verifierAuthService.login(req, httpReq));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and receive JWT token")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest req,
                                                  HttpServletRequest httpReq) {
        return ResponseEntity.ok(verifierAuthService.verifyOtp(req, httpReq));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current verifier profile")
    public ResponseEntity<VerifierProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(verifierAuthService.getProfile(principal.getId()));
    }

    @PutMapping("/me")
    @Operation(summary = "Update verifier profile")
    public ResponseEntity<VerifierProfileResponse> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @Valid @RequestBody UpdateVerifierRequest req) {
        return ResponseEntity.ok(verifierAuthService.updateProfile(principal.getId(), req));
    }

    @PostMapping("/revoke")
    @Operation(summary = "Revoke current JWT token")
    public ResponseEntity<Map<String, String>> revoke(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            verifierAuthService.revoke(authHeader.substring(7));
        }
        return ResponseEntity.ok(Map.of("message", "Token revoked"));
    }
}
