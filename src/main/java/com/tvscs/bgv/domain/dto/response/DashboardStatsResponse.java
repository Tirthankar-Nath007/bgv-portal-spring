package com.tvscs.bgv.domain.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardStatsResponse {
    private long totalVerifications;
    private long pendingAppeals;
    private long activeVerifiers;
    private long totalEmployees;
    private long matchedVerifications;
    private long partialMatches;
    private long mismatches;
    private long completedAppeals;
    private List<RecentActivity> recentVerifications;
    private List<RecentActivity> recentAppeals;

    @Data
    @Builder
    public static class RecentActivity {
        private String id;
        private String type; // "verification" or "appeal"
        private String employeeId;
        private String status;
        private String timestamp;
    }
}
