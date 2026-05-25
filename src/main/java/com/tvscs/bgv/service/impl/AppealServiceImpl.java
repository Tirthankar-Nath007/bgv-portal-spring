package com.tvscs.bgv.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tvscs.bgv.domain.dto.request.RespondAppealRequest;
import com.tvscs.bgv.domain.dto.request.SubmitAppealRequest;
import com.tvscs.bgv.domain.dto.response.AppealResponse;
import com.tvscs.bgv.domain.entity.Appeal;
import com.tvscs.bgv.domain.entity.Employee;
import com.tvscs.bgv.domain.entity.VerificationRecord;
import com.tvscs.bgv.domain.entity.Verifier;
import com.tvscs.bgv.exception.ResourceNotFoundException;
import com.tvscs.bgv.repository.AppealRepository;
import com.tvscs.bgv.repository.EmployeeRepository;
import com.tvscs.bgv.repository.VerificationRecordRepository;
import com.tvscs.bgv.repository.VerifierRepository;
import com.tvscs.bgv.security.UserPrincipal;
import com.tvscs.bgv.service.AppealService;
import com.tvscs.bgv.service.EmailService;
import com.tvscs.bgv.service.SequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppealServiceImpl implements AppealService {

    private final AppealRepository appealRepository;
    private final VerificationRecordRepository verificationRecordRepository;
    private final VerifierRepository verifierRepository;
    private final EmployeeRepository employeeRepository;
    private final SequenceService sequenceService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    public AppealServiceImpl(AppealRepository appealRepository, VerificationRecordRepository verificationRecordRepository, VerifierRepository verifierRepository, EmployeeRepository employeeRepository, SequenceService sequenceService, EmailService emailService, ObjectMapper objectMapper) {
        this.appealRepository = appealRepository;
        this.verificationRecordRepository = verificationRecordRepository;
        this.verifierRepository = verifierRepository;
        this.employeeRepository = employeeRepository;
        this.sequenceService = sequenceService;
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public AppealResponse submit(Long verifierId, SubmitAppealRequest req) {
        VerificationRecord record = verificationRecordRepository
                .findByVerificationIdIgnoreCase(req.getVerificationId())
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found: " + req.getVerificationId()));

        if (!record.getVerifierId().equals(verifierId)) {
            throw new ResourceNotFoundException("Verification not found");
        }

        String mismatchedFieldsJson = writeJson(req.getMismatchedFields());
        String appealId = sequenceService.nextAppealId();

        Appeal appeal = Appeal.builder()
                .appealId(appealId)
                .verificationId(record.getId())
                .verifierId(verifierId)
                .employeeId(record.getEmployeeId())
                .appealReason(req.getComments())
                .mismatchedFields(mismatchedFieldsJson)
                .status("pending")
                .build();

        appeal = appealRepository.save(appeal);

        Verifier verifier = verifierRepository.findById(verifierId).orElse(null);
        final Appeal savedAppeal = appeal;
        try {
            emailService.sendAppealNotification(savedAppeal, verifier);
        } catch (Exception e) {
            log.error("Appeal notification email failed: {}", e.getMessage());
        }

        return toResponse(savedAppeal, verifier, null, record);
    }

    @Override
    public Page<AppealResponse> listAll(String status, Pageable pageable) {
        Page<Appeal> appeals = (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status))
                ? appealRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                : appealRepository.findAllByOrderByCreatedAtDesc(pageable);

        return appeals.map(a -> {
            Verifier verifier = verifierRepository.findById(a.getVerifierId()).orElse(null);
            Employee employee = employeeRepository.findByEmployeeIdIgnoreCase(a.getEmployeeId()).orElse(null);
            VerificationRecord record = verificationRecordRepository.findById(a.getVerificationId()).orElse(null);
            return toResponse(a, verifier, employee, record);
        });
    }

    @Override
    public AppealResponse getById(String appealId) {
        Appeal appeal = appealRepository.findByAppealId(appealId)
                .orElseThrow(() -> new ResourceNotFoundException("Appeal not found: " + appealId));
        Verifier verifier = verifierRepository.findById(appeal.getVerifierId()).orElse(null);
        Employee employee = employeeRepository.findByEmployeeIdIgnoreCase(appeal.getEmployeeId()).orElse(null);
        VerificationRecord record = verificationRecordRepository.findById(appeal.getVerificationId()).orElse(null);
        return toResponse(appeal, verifier, employee, record);
    }

    @Override
    @Transactional
    public AppealResponse respond(String appealId, RespondAppealRequest req, UserPrincipal admin) {
        Appeal appeal = appealRepository.findByAppealId(appealId)
                .orElseThrow(() -> new ResourceNotFoundException("Appeal not found: " + appealId));

        appeal.setStatus("completed");
        appeal.setHrResponse(req.getHrComments());
        appeal.setHrComments(req.getHrComments());
        appeal.setReviewedBy(String.valueOf(admin.getId()));
        appeal.setReviewedAt(LocalDateTime.now());
        appeal = appealRepository.save(appeal);

        Verifier verifier = verifierRepository.findById(appeal.getVerifierId()).orElse(null);
        if (verifier != null) {
            final Appeal savedAppeal = appeal;
            try {
                emailService.sendAppealResponse(savedAppeal, verifier.getEmail());
            } catch (Exception e) {
                log.error("Appeal response email failed: {}", e.getMessage());
            }
        }

        Employee employee = employeeRepository.findByEmployeeIdIgnoreCase(appeal.getEmployeeId()).orElse(null);
        VerificationRecord record = verificationRecordRepository.findById(appeal.getVerificationId()).orElse(null);
        return toResponse(appeal, verifier, employee, record);
    }

    private AppealResponse toResponse(Appeal a, Verifier verifier, Employee employee, VerificationRecord record) {
        List<AppealResponse.MismatchedFieldInfo> fields = parseMismatchedFields(a.getMismatchedFields());

        AppealResponse.VerifierInfo verifierInfo = verifier == null ? null :
                AppealResponse.VerifierInfo.builder()
                        .id(verifier.getId())
                        .companyName(verifier.getCompanyName())
                        .email(verifier.getEmail())
                        .build();

        AppealResponse.EmployeeInfo employeeInfo = employee == null ? null :
                AppealResponse.EmployeeInfo.builder()
                        .employeeId(employee.getEmployeeId())
                        .name(employee.getFullName())
                        .department(employee.getDepartment())
                        .designation(employee.getDesignation())
                        .build();

        AppealResponse.VerificationInfo verificationInfo = record == null ? null :
                AppealResponse.VerificationInfo.builder()
                        .verificationId(record.getVerificationId())
                        .overallStatus(record.getOverallStatus())
                        .matchScore(record.getMatchScore())
                        .build();

        return AppealResponse.builder()
                .id(a.getId())
                .appealId(a.getAppealId())
                .verificationId(a.getVerificationId())
                .employeeId(a.getEmployeeId())
                .appealReason(a.getAppealReason())
                .status(a.getStatus())
                .hrResponse(a.getHrResponse())
                .hrComments(a.getHrComments())
                .reviewedBy(a.getReviewedBy())
                .reviewedAt(a.getReviewedAt())
                .createdAt(a.getCreatedAt())
                .mismatchedFields(fields)
                .verifier(verifierInfo)
                .employee(employeeInfo)
                .verification(verificationInfo)
                .build();
    }

    private List<AppealResponse.MismatchedFieldInfo> parseMismatchedFields(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            List<SubmitAppealRequest.MismatchedFieldDto> raw = objectMapper.readValue(json, new TypeReference<>() {});
            return raw.stream()
                    .map(f -> AppealResponse.MismatchedFieldInfo.builder()
                            .fieldName(f.getFieldName())
                            .verifierValue(f.getVerifierValue())
                            .companyValue(f.getCompanyValue())
                            .build())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String writeJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }
}
