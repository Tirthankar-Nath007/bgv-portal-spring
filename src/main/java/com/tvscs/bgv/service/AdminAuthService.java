package com.tvscs.bgv.service;

import com.tvscs.bgv.domain.dto.request.AdminLoginRequest;
import com.tvscs.bgv.domain.dto.request.CreateAdminRequest;
import com.tvscs.bgv.domain.dto.response.AdminAuthResponse;
import com.tvscs.bgv.domain.dto.response.AdminProfileResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AdminAuthService {
    AdminAuthResponse login(AdminLoginRequest req, HttpServletRequest httpReq);
    AdminProfileResponse createAdmin(CreateAdminRequest req);
}
