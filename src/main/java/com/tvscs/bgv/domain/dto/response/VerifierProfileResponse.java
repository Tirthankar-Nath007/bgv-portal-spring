package com.tvscs.bgv.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VerifierProfileResponse {
    private Long id;
    private String email;
    private String companyName;
    @JsonProperty("isEmailVerified")
    private boolean isEmailVerified;
    @JsonProperty("isActive")
    private boolean isActive;
    @JsonProperty("isBgvAgency")
    private boolean isBgvAgency;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
