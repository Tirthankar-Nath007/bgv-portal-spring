package com.tvscs.bgv.domain.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateVerifierRequest {

    @Size(min = 2, max = 100, message = "Company name must be 2-100 characters")
    private String companyName;
}
