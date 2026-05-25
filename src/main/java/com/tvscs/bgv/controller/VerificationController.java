package com.tvscs.bgv.controller;

import com.tvscs.bgv.domain.dto.request.ValidateEmployeeRequest;
import com.tvscs.bgv.domain.dto.request.VerificationRequest;
import com.tvscs.bgv.domain.dto.response.ValidateEmployeeResponse;
import com.tvscs.bgv.domain.dto.response.VerificationResponse;
import com.tvscs.bgv.security.UserPrincipal;
import com.tvscs.bgv.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/verify")
@Tag(name = "Verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/validate-employee")
    @Operation(summary = "Validate employee existence before full verification")
    public ResponseEntity<ValidateEmployeeResponse> validateEmployee(
            @Valid @RequestBody ValidateEmployeeRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(verificationService.validateEmployee(principal.getId(), req));
    }

    @PostMapping("/request")
    @Operation(summary = "Submit a full employment verification")
    public ResponseEntity<VerificationResponse> verify(
            @Valid @RequestBody VerificationRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(verificationService.verify(principal.getId(), req));
    }

    @GetMapping("/request")
    @Operation(summary = "Get verification history or a specific verification")
    public ResponseEntity<?> getVerification(
            @RequestParam(required = false) String verificationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (verificationId != null && !verificationId.isBlank()) {
            return ResponseEntity.ok(verificationService.getByVerificationId(verificationId, principal.getId()));
        }
        List<VerificationResponse> history = verificationService.getHistoryByVerifier(principal.getId());
        return ResponseEntity.ok(history);
    }
}
