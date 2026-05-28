package com.tvscs.bgv.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tvscs.bgv.config.AppProperties;
import com.tvscs.bgv.domain.dto.ComparisonResultDto;
import com.tvscs.bgv.domain.dto.request.ValidateEmployeeRequest;
import com.tvscs.bgv.domain.dto.request.VerificationRequest;
import com.tvscs.bgv.domain.dto.response.ValidateEmployeeResponse;
import com.tvscs.bgv.domain.dto.response.VerificationResponse;
import com.tvscs.bgv.domain.entity.Employee;
import com.tvscs.bgv.domain.entity.Verifier;
import com.tvscs.bgv.domain.entity.VerificationAttempt;
import com.tvscs.bgv.domain.entity.VerificationRecord;
import com.tvscs.bgv.exception.BlockedException;
import com.tvscs.bgv.exception.ResourceNotFoundException;
import com.tvscs.bgv.repository.EmployeeRepository;
import com.tvscs.bgv.repository.VerificationAttemptRepository;
import com.tvscs.bgv.repository.VerificationRecordRepository;
import com.tvscs.bgv.repository.VerifierRepository;
import com.tvscs.bgv.service.ComparisonService;
import com.tvscs.bgv.service.EmailService;
import com.tvscs.bgv.service.SequenceService;
import com.tvscs.bgv.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private final EmployeeRepository employeeRepository;
    private final VerificationRecordRepository verificationRecordRepository;
    private final VerificationAttemptRepository verificationAttemptRepository;
    private final VerifierRepository verifierRepository;
    private final ComparisonService comparisonService;
    private final SequenceService sequenceService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    @Override
    @Transactional(noRollbackFor = BlockedException.class)
    public ValidateEmployeeResponse validateEmployee(Long verifierId, ValidateEmployeeRequest req) {
        String normalizedEmpId = req.getEmployeeId().trim().toUpperCase();

        VerificationAttempt attempt = verificationAttemptRepository
                .findByVerifierIdAndEmployeeIdIgnoreCase(verifierId, normalizedEmpId)
                .orElse(null);

        if (attempt != null && attempt.isBlocked()) {
            throw new BlockedException(getBlockedMessage());
        }

        Employee employee = employeeRepository.findByEmployeeIdIgnoreCase(normalizedEmpId).orElse(null);

        boolean nameMatch = employee != null &&
                employee.getFullName().trim().equalsIgnoreCase(req.getName().trim());

        if (employee == null || !nameMatch) {
            attempt = incrementAttempt(verifierId, normalizedEmpId, attempt);
            if (attempt.isBlocked()) {
                Verifier verifier = verifierRepository.findById(verifierId).orElse(null);
                try {
                    emailService.sendBlockNotification(verifier, normalizedEmpId, attempt.getAttemptCount());
                } catch (Exception e) {
                    log.error("Block notification email failed: {}", e.getMessage());
                }
                throw new BlockedException(getBlockedMessage());
            }
            int remaining = appProperties.getVerificationMaxAttempts() - attempt.getAttemptCount();
            return ValidateEmployeeResponse.builder()
                    .found(false)
                    .employeeId(normalizedEmpId)
                    .message("Employee not found or name mismatch. " + remaining + " attempt(s) remaining.")
                    .build();
        }

        // Entity name check — if the caller supplied one, it must match the employee's business field
        String requestedEntity = req.getEntityName();
        if (requestedEntity != null && !requestedEntity.isBlank()) {
            String employeeBusiness = employee.getBusiness();
            boolean entityMatch = employeeBusiness != null &&
                    requestedEntity.trim().equalsIgnoreCase(employeeBusiness.trim());
            if (!entityMatch) {
                attempt = incrementAttempt(verifierId, normalizedEmpId, attempt);
                if (attempt.isBlocked()) {
                    Verifier verifier = verifierRepository.findById(verifierId).orElse(null);
                    try {
                        emailService.sendBlockNotification(verifier, normalizedEmpId, attempt.getAttemptCount());
                    } catch (Exception e) {
                        log.error("Block notification email failed: {}", e.getMessage());
                    }
                    throw new BlockedException(getBlockedMessage());
                }
                int remaining = appProperties.getVerificationMaxAttempts() - attempt.getAttemptCount();
                return ValidateEmployeeResponse.builder()
                        .found(false)
                        .employeeId(normalizedEmpId)
                        .message("Employee verification failed. Invalid Credentials. " + remaining + " attempt(s) remaining.")
                        .build();
            }
        }

        // Success — reset attempts
        resetAttempt(verifierId, normalizedEmpId, attempt);
        return ValidateEmployeeResponse.builder()
                .found(true)
                .employeeId(employee.getEmployeeId())
                .message("Employee validated successfully")
                .build();
    }

    @Override
    @Transactional
    public VerificationResponse verify(Long verifierId, VerificationRequest req) {
        String normalizedEmpId = req.getEmployeeId().trim().toUpperCase();
        Employee employee = employeeRepository.findByEmployeeIdIgnoreCase(normalizedEmpId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + normalizedEmpId));

        List<ComparisonResultDto> results = comparisonService.compare(req, employee);
        int score = comparisonService.calculateScore(results);
        String status = comparisonService.determineStatus(score);

        String verificationId = sequenceService.nextVerificationId();
        String submittedDataJson = writeJson(buildSubmittedDataMap(req));
        String comparisonResultsJson = writeJson(results);

        VerificationRecord record = VerificationRecord.builder()
                .verificationId(verificationId)
                .employeeId(employee.getEmployeeId())
                .verifierId(verifierId)
                .submittedData(submittedDataJson)
                .comparisonResults(comparisonResultsJson)
                .overallStatus(status)
                .matchScore(score)
                .consentGiven(Boolean.TRUE.equals(req.getConsentGiven()))
                .verificationCompletedAt(LocalDateTime.now())
                .build();

        record = verificationRecordRepository.save(record);

        // Reset attempt count on successful verification
        String normalizedId = req.getEmployeeId().trim().toUpperCase();
        verificationAttemptRepository.findByVerifierIdAndEmployeeIdIgnoreCase(verifierId, normalizedId)
                .ifPresent(a -> {
                    a.setAttemptCount(0);
                    a.setBlocked(false);
                    a.setBlockedAt(null);
                    verificationAttemptRepository.save(a);
                });

        return toResponse(record, employee, results);
    }

    @Override
    public VerificationResponse getByVerificationId(String verificationId, Long verifierId) {
        VerificationRecord record = verificationRecordRepository
                .findByVerificationIdIgnoreCase(verificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification not found: " + verificationId));
        if (!record.getVerifierId().equals(verifierId)) {
            throw new ResourceNotFoundException("Verification not found");
        }
        Employee employee = employeeRepository.findByEmployeeIdIgnoreCase(record.getEmployeeId()).orElse(null);
        List<ComparisonResultDto> results = parseComparisonResults(record.getComparisonResults());
        return toResponse(record, employee, results);
    }

    @Override
    public List<VerificationResponse> getHistoryByVerifier(Long verifierId) {
        return verificationRecordRepository.findByVerifierIdOrderByCreatedAtDesc(verifierId).stream()
                .map(r -> {
                    Employee emp = employeeRepository.findByEmployeeIdIgnoreCase(r.getEmployeeId()).orElse(null);
                    List<ComparisonResultDto> res = parseComparisonResults(r.getComparisonResults());
                    return toResponse(r, emp, res);
                })
                .collect(Collectors.toList());
    }

    private VerificationAttempt incrementAttempt(Long verifierId, String employeeId, VerificationAttempt existing) {
        int maxAttempts = appProperties.getVerificationMaxAttempts();
        if (existing == null) {
            VerificationAttempt a = VerificationAttempt.builder()
                    .verifierId(verifierId)
                    .employeeId(employeeId)
                    .attemptCount(1)
                    .isBlocked(1 >= maxAttempts)
                    .lastAttemptAt(LocalDateTime.now())
                    .build();
            if (a.isBlocked()) a.setBlockedAt(LocalDateTime.now());
            return verificationAttemptRepository.save(a);
        } else {
            int newCount = existing.getAttemptCount() + 1;
            existing.setAttemptCount(newCount);
            existing.setLastAttemptAt(LocalDateTime.now());
            if (newCount >= maxAttempts && !existing.isBlocked()) {
                existing.setBlocked(true);
                existing.setBlockedAt(LocalDateTime.now());
            }
            return verificationAttemptRepository.save(existing);
        }
    }

    private void resetAttempt(Long verifierId, String employeeId, VerificationAttempt existing) {
        if (existing != null) {
            existing.setAttemptCount(0);
            existing.setBlocked(false);
            existing.setBlockedAt(null);
            verificationAttemptRepository.save(existing);
        }
    }

    private VerificationResponse toResponse(VerificationRecord r, Employee emp, List<ComparisonResultDto> results) {
        VerificationResponse.EmployeeSummary empSummary = null;
        if (emp != null) {
            empSummary = VerificationResponse.EmployeeSummary.builder()
                    .employeeId(emp.getEmployeeId())
                    .name(emp.getFullName())
                    .department(emp.getDepartment())
                    .designation(emp.getDesignation())
                    .entityName(emp.getBusiness())
                    .dateOfJoining(emp.getDateOfJoining() != null ? emp.getDateOfJoining().toString() : null)
                    .dateOfLeaving(emp.getDateOfLeaving() != null ? emp.getDateOfLeaving().toString() : null)
                    .fnfStatus(deriveFnfStatus(emp))
                    .build();
        }
        return VerificationResponse.builder()
                .id(r.getId())
                .verificationId(r.getVerificationId())
                .employeeId(r.getEmployeeId())
                .overallStatus(r.getOverallStatus())
                .matchScore(r.getMatchScore())
                .comparisonResults(results)
                .verificationCompletedAt(r.getVerificationCompletedAt())
                .createdAt(r.getCreatedAt())
                .employee(empSummary)
                .build();
    }

    private String deriveFnfStatus(Employee emp) {
        if (emp.getDateOfLeaving() == null && emp.getExitReason() == null) return "Pending";
        return "Completed";
    }

    private Map<String, Object> buildSubmittedDataMap(VerificationRequest req) {
        return Map.of(
                "employeeId", nullSafe(req.getEmployeeId()),
                "name", nullSafe(req.getName()),
                "entityName", nullSafe(req.getEntityName()),
                "dateOfJoining", nullSafe(req.getDateOfJoining()),
                "dateOfLeaving", nullSafe(req.getDateOfLeaving()),
                "designation", nullSafe(req.getDesignation()),
                "exitReason", nullSafe(req.getExitReason()),
                "consentGiven", Boolean.TRUE.equals(req.getConsentGiven())
        );
    }

    private String nullSafe(String s) {
        return s != null ? s : "";
    }

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private List<ComparisonResultDto> parseComparisonResults(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String getBlockedMessage() {
        return "Maximum attempts reached. Please contact the exit team at " + appProperties.getAdminEmail();
    }
}
