package com.tvscs.bgv.domain.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AppealResponse {
    private Long id;
    private String appealId;
    private Long verificationId;
    private String employeeId;
    private String appealReason;
    private String status;
    private String hrResponse;
    private String hrComments;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private List<MismatchedFieldInfo> mismatchedFields;
    private VerifierInfo verifier;
    private EmployeeInfo employee;
    private VerificationInfo verification;

    @Data
    @Builder
    public static class MismatchedFieldInfo {
        private String fieldName;
        private String verifierValue;
        private String companyValue;
    }

    @Data
    @Builder
    public static class VerifierInfo {
        private Long id;
        private String companyName;
        private String email;
    }

    @Data
    @Builder
    public static class EmployeeInfo {
        private String employeeId;
        private String name;
        private String department;
        private String designation;
    }

    @Data
    @Builder
    public static class VerificationInfo {
        private String verificationId;
        private String overallStatus;
        private Integer matchScore;
    }
}
