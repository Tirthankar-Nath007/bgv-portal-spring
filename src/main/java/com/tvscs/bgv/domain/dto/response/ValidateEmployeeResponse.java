package com.tvscs.bgv.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidateEmployeeResponse {
    private boolean found;
    private String employeeId;
    private String message;
}
