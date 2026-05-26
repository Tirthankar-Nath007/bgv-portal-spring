package com.tvscs.bgv.controller;

import com.tvscs.bgv.domain.dto.request.RespondAppealRequest;
import com.tvscs.bgv.domain.dto.request.SubmitAppealRequest;
import com.tvscs.bgv.domain.dto.response.AppealResponse;
import com.tvscs.bgv.security.UserPrincipal;
import com.tvscs.bgv.service.AppealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appeals")
@Tag(name = "Appeals")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;

    @PostMapping
    @Operation(summary = "Submit an appeal for a verification discrepancy")
    public ResponseEntity<AppealResponse> submit(
            @Valid @RequestBody SubmitAppealRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appealService.submit(principal.getId(), req));
    }

    @GetMapping
    @Operation(summary = "List all appeals (admin only)")
    public ResponseEntity<Page<AppealResponse>> listAll(
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(appealService.listAll(status, pageable));
    }

    @GetMapping("/{appealId}")
    @Operation(summary = "Get a specific appeal by ID")
    public ResponseEntity<AppealResponse> getById(@PathVariable String appealId) {
        return ResponseEntity.ok(appealService.getById(appealId));
    }

    @PostMapping("/{appealId}/respond")
    @Operation(summary = "HR responds to an appeal")
    public ResponseEntity<AppealResponse> respond(
            @PathVariable String appealId,
            @Valid @RequestBody RespondAppealRequest req,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(appealService.respond(appealId, req, principal));
    }
}
