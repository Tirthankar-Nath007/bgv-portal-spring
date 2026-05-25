package com.tvscs.bgv.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SubmitAppealRequest {

    @NotBlank(message = "Verification ID is required")
    private String verificationId;

    @NotBlank(message = "Appeal reason is required")
    @Size(min = 10, max = 1000, message = "Appeal reason must be 10-1000 characters")
    private String comments;

    private List<MismatchedFieldDto> mismatchedFields;

    @Data
    public static class MismatchedFieldDto {
        private String fieldName;
        private String verifierValue;
        private String companyValue;
    }
}
