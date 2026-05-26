package com.tvscs.bgv.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tvscs.bgv.domain.dto.request.AdminLoginRequest;
import com.tvscs.bgv.domain.dto.request.CreateAdminRequest;
import com.tvscs.bgv.domain.dto.response.AdminAuthResponse;
import com.tvscs.bgv.domain.dto.response.AdminProfileResponse;
import com.tvscs.bgv.domain.entity.Admin;
import com.tvscs.bgv.exception.DuplicateResourceException;
import com.tvscs.bgv.repository.AdminRepository;
import com.tvscs.bgv.security.JwtService;
import com.tvscs.bgv.service.AccessLogService;
import com.tvscs.bgv.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthServiceImpl implements AdminAuthService {

    private final AdminRepository adminRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AccessLogService accessLogService;
    private final ObjectMapper objectMapper;

    @Override
    public AdminAuthResponse login(AdminLoginRequest req, HttpServletRequest httpReq) {
        Admin admin = adminRepository.findByUsernameIgnoreCase(req.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!admin.isActive()) {
            accessLogService.log(req.getUsername(), "admin", "LOGIN", "FAILURE", "Account inactive", httpReq);
            throw new BadCredentialsException("Account is inactive");
        }
        if (!passwordEncoder.matches(req.getPassword(), admin.getPassword())) {
            accessLogService.log(req.getUsername(), "admin", "LOGIN", "FAILURE", "Invalid password", httpReq);
            throw new BadCredentialsException("Invalid credentials");
        }

        admin.setLastLoginAt(LocalDateTime.now());
        adminRepository.save(admin);
        accessLogService.log(req.getUsername(), "admin", "LOGIN", "SUCCESS", null, httpReq);

        String springRole = admin.getRole().toUpperCase().replace(" ", "_");
        String token = jwtService.generateToken(
                admin.getId(), admin.getUsername(), "ADMIN", springRole, admin.getFullName());

        return AdminAuthResponse.builder()
                .token(token)
                .admin(toProfile(admin))
                .build();
    }

    @Override
    public AdminProfileResponse createAdmin(CreateAdminRequest req) {
        if (adminRepository.findByUsernameIgnoreCase(req.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists");
        }
        List<String> perms = req.getPermissions() != null ? req.getPermissions() : Collections.emptyList();
        Admin admin = Admin.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .role(req.getRole())
                .department(req.getDepartment())
                .permissions(writeJson(perms))
                .isActive(true)
                .build();
        admin = adminRepository.save(admin);
        return toProfile(admin);
    }

    private AdminProfileResponse toProfile(Admin a) {
        return AdminProfileResponse.builder()
                .id(a.getId())
                .username(a.getUsername())
                .email(a.getEmail())
                .fullName(a.getFullName())
                .role(a.getRole())
                .department(a.getDepartment())
                .permissions(readPermissions(a.getPermissions()))
                .isActive(a.isActive())
                .build();
    }

    private List<String> readPermissions(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "[]";
        }
    }
}
