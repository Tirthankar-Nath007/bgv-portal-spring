package com.tvscs.bgv.domain.dto.response;

import com.tvscs.bgv.domain.dto.ComparisonResultDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VerificationResponse {
    private Long id;
    private String verificationId;
    private String employeeId;
    private String overallStatus;
    private Integer matchScore;
    private List<ComparisonResultDto> comparisonResults;
    private LocalDateTime verificationCompletedAt;
    private LocalDateTime createdAt;
    private EmployeeSummary employee;

    @Data
    @Builder
    public static class EmployeeSummary {
        private String employeeId;
        private String name;
        private String department;
        private String designation;
        private String entityName;
        private String dateOfJoining;
        private String dateOfLeaving;
        private String fnfStatus;
    }
}
