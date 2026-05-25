package com.tvscs.bgv.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAuthResponse {
    private String token;
    private AdminProfileResponse admin;
}
