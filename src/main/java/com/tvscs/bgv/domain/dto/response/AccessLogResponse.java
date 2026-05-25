package com.tvscs.bgv.domain.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccessLogResponse {
    private Long id;
    private String email;
    private String role;
    private String action;
    private String status;
    private String ipAddress;
    private String userAgent;
    private String failureReason;
    private LocalDateTime timestamp;
}
