package com.tvscs.bgv.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginInitResponse {
    private boolean requireOtp;
    private String email;
    private String message;
}
