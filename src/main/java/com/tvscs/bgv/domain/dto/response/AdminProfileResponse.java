package com.tvscs.bgv.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private String department;
    private List<String> permissions;
    @JsonProperty("isActive")
    private boolean isActive;
}
