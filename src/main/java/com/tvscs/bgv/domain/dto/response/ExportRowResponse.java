package com.tvscs.bgv.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExportRowResponse {
    private int sNo;
    private String employeeId;
    private String employeeName;
    private String product;
    private String department;
    private String designation;
    private String dateOfJoining;
    private String lastWorkingDay;
    private String verifiedOn;
    private String verifiedBy;
    private String verifiedFor;
}
