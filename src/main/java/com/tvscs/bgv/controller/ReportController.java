package com.tvscs.bgv.controller;

import com.tvscs.bgv.security.UserPrincipal;
import com.tvscs.bgv.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports")
@RequiredArgsConstructor
public class ReportController {

    private final PdfService pdfService;

    public ReportController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/generate")
    @Operation(summary = "Generate a PDF verification report")
    public ResponseEntity<byte[]> generateReport(
            @RequestParam String verificationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long verifierId = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().startsWith("ROLE_ADMIN") || a.getAuthority().contains("HR"))
                ? null : principal.getId();

        byte[] pdf = pdfService.generateVerificationReport(verificationId, verifierId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + verificationId + "-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
