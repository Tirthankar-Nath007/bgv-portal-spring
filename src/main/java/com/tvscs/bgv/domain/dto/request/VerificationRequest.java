package com.tvscs.bgv.domain.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerificationRequest {

    @NotBlank(message = "Employee ID is required")
    private String employeeId;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String name;

    private String entityName;
    private String dateOfJoining;
    private String dateOfLeaving;
    private String designation;
    private String exitReason;

    @NotNull(message = "Consent must be provided")
    @AssertTrue(message = "Consent must be given to proceed with verification")
    private Boolean consentGiven;
}
