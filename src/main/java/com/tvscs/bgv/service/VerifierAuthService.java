package com.tvscs.bgv.service;

import com.tvscs.bgv.domain.dto.request.LoginVerifierRequest;
import com.tvscs.bgv.domain.dto.request.RegisterVerifierRequest;
import com.tvscs.bgv.domain.dto.request.UpdateVerifierRequest;
import com.tvscs.bgv.domain.dto.request.VerifyOtpRequest;
import com.tvscs.bgv.domain.dto.response.AuthResponse;
import com.tvscs.bgv.domain.dto.response.LoginInitResponse;
import com.tvscs.bgv.domain.dto.response.VerifierProfileResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface VerifierAuthService {
    VerifierProfileResponse register(RegisterVerifierRequest req);
    LoginInitResponse login(LoginVerifierRequest req, HttpServletRequest httpReq);
    AuthResponse verifyOtp(VerifyOtpRequest req, HttpServletRequest httpReq);
    VerifierProfileResponse getProfile(Long verifierId);
    VerifierProfileResponse updateProfile(Long verifierId, UpdateVerifierRequest req);
    void revoke(String token);
}
