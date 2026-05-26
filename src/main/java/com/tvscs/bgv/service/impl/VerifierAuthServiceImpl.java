package com.tvscs.bgv.service.impl;

import com.tvscs.bgv.domain.dto.request.LoginVerifierRequest;
import com.tvscs.bgv.domain.dto.request.RegisterVerifierRequest;
import com.tvscs.bgv.domain.dto.request.UpdateVerifierRequest;
import com.tvscs.bgv.domain.dto.request.VerifyOtpRequest;
import com.tvscs.bgv.domain.dto.response.AuthResponse;
import com.tvscs.bgv.domain.dto.response.LoginInitResponse;
import com.tvscs.bgv.domain.dto.response.VerifierProfileResponse;
import com.tvscs.bgv.domain.entity.Verifier;
import com.tvscs.bgv.exception.DuplicateResourceException;
import com.tvscs.bgv.exception.ResourceNotFoundException;
import com.tvscs.bgv.repository.VerifierRepository;
import com.tvscs.bgv.security.JwtService;
import com.tvscs.bgv.service.AccessLogService;
import com.tvscs.bgv.service.VerifierAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerifierAuthServiceImpl implements VerifierAuthService {

    private static final String DUMMY_OTP = "12345";

    private final VerifierRepository verifierRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AccessLogService accessLogService;

    @Override
    public VerifierProfileResponse register(RegisterVerifierRequest req) {
        if (verifierRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new DuplicateResourceException("Email is already registered");
        }
        Verifier verifier = Verifier.builder()
                .email(req.getEmail().toLowerCase())
                .password(passwordEncoder.encode(req.getPassword()))
                .companyName(req.getCompanyName())
                .isEmailVerified(true)
                .isActive(true)
                .isBgvAgency(req.isBgvAgency())
                .testMode(false)
                .build();
        verifier = verifierRepository.save(verifier);
        return toProfile(verifier);
    }

    @Override
    public LoginInitResponse login(LoginVerifierRequest req, HttpServletRequest httpReq) {
        Verifier verifier = verifierRepository.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!verifier.isActive()) {
            accessLogService.log(req.getEmail(), "verifier", "LOGIN", "FAILURE", "Account inactive", httpReq);
            throw new BadCredentialsException("Account is inactive");
        }

        if (!passwordEncoder.matches(req.getPassword(), verifier.getPassword())) {
            accessLogService.log(req.getEmail(), "verifier", "LOGIN", "FAILURE", "Invalid password", httpReq);
            throw new BadCredentialsException("Invalid credentials");
        }

        accessLogService.log(req.getEmail(), "verifier", "LOGIN", "SUCCESS", null, httpReq);

        return LoginInitResponse.builder()
                .requireOtp(true)
                .email(req.getEmail())
                .message("OTP sent to your email")
                .build();
    }

    @Override
    public AuthResponse verifyOtp(VerifyOtpRequest req, HttpServletRequest httpReq) {
        if (!DUMMY_OTP.equals(req.getOtp())) {
            accessLogService.log(req.getEmail(), "verifier", "LOGIN_OTP", "FAILURE", "Invalid OTP", httpReq);
            throw new BadCredentialsException("Invalid OTP");
        }

        Verifier verifier = verifierRepository.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Verifier not found"));

        verifier.setLastLoginAt(LocalDateTime.now());
        verifierRepository.save(verifier);

        accessLogService.log(req.getEmail(), "verifier", "LOGIN_OTP", "SUCCESS", null, httpReq);

        String token = jwtService.generateToken(
                verifier.getId(), verifier.getEmail(), "VERIFIER", "VERIFIER", verifier.getCompanyName());

        return AuthResponse.builder()
                .token(token)
                .verifier(toProfile(verifier))
                .build();
    }

    @Override
    public VerifierProfileResponse getProfile(Long verifierId) {
        Verifier verifier = verifierRepository.findById(verifierId)
                .orElseThrow(() -> new ResourceNotFoundException("Verifier not found"));
        return toProfile(verifier);
    }

    @Override
    public VerifierProfileResponse updateProfile(Long verifierId, UpdateVerifierRequest req) {
        Verifier verifier = verifierRepository.findById(verifierId)
                .orElseThrow(() -> new ResourceNotFoundException("Verifier not found"));
        if (req.getCompanyName() != null && !req.getCompanyName().isBlank()) {
            verifier.setCompanyName(req.getCompanyName());
        }
        return toProfile(verifierRepository.save(verifier));
    }

    @Override
    public void revoke(String token) {
        jwtService.revokeToken(token);
    }

    private VerifierProfileResponse toProfile(Verifier v) {
        return VerifierProfileResponse.builder()
                .id(v.getId())
                .email(v.getEmail())
                .companyName(v.getCompanyName())
                .isEmailVerified(v.isEmailVerified())
                .isActive(v.isActive())
                .isBgvAgency(v.isBgvAgency())
                .lastLoginAt(v.getLastLoginAt())
                .createdAt(v.getCreatedAt())
                .build();
    }
}
