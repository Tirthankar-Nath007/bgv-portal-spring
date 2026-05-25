package com.tvscs.bgv.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RespondAppealRequest {

    @NotBlank(message = "HR response is required")
    @Size(min = 2, max = 2000, message = "HR response must be 2-2000 characters")
    private String hrComments;
}
