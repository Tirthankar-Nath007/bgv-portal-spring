package com.tvscs.bgv.service;

import com.tvscs.bgv.domain.dto.request.ValidateEmployeeRequest;
import com.tvscs.bgv.domain.dto.request.VerificationRequest;
import com.tvscs.bgv.domain.dto.response.ValidateEmployeeResponse;
import com.tvscs.bgv.domain.dto.response.VerificationResponse;

import java.util.List;

public interface VerificationService {
    ValidateEmployeeResponse validateEmployee(Long verifierId, ValidateEmployeeRequest req);
    VerificationResponse verify(Long verifierId, VerificationRequest req);
    VerificationResponse getByVerificationId(String verificationId, Long verifierId);
    List<VerificationResponse> getHistoryByVerifier(Long verifierId);
}
