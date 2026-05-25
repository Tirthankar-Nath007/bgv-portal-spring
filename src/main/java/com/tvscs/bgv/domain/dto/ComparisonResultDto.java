package com.tvscs.bgv.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonResultDto {
    private String field;
    private String verifierValue;
    private String companyValue;
    @JsonProperty("isMatch")
    private boolean isMatch;
    private String matchType; // exact, partial, mismatch, not_provided
}
