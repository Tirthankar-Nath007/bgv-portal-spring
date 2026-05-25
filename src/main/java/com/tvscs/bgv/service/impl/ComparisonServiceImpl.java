package com.tvscs.bgv.service.impl;

import com.tvscs.bgv.domain.dto.ComparisonResultDto;
import com.tvscs.bgv.domain.dto.request.VerificationRequest;
import com.tvscs.bgv.domain.entity.Employee;
import com.tvscs.bgv.service.ComparisonService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ComparisonServiceImpl implements ComparisonService {

    private static final int DATE_TOLERANCE_DAYS = 1;

    @Override
    public List<ComparisonResultDto> compare(VerificationRequest submitted, Employee official) {
        List<ComparisonResultDto> results = new ArrayList<>();

        results.add(compareExact("employeeId", submitted.getEmployeeId(), official.getEmployeeId(), true));
        results.add(compareIgnoreCase("name", submitted.getName(), official.getFullName()));
        results.add(compareExact("entityName", submitted.getEntityName(), official.getBusiness(), false));
        results.add(compareDate("dateOfJoining", submitted.getDateOfJoining(),
                official.getDateOfJoining() != null ? official.getDateOfJoining().toString() : null));
        results.add(compareDate("dateOfLeaving", submitted.getDateOfLeaving(),
                official.getDateOfLeaving() != null ? official.getDateOfLeaving().toString() : null));
        results.add(compareExact("designation", submitted.getDesignation(), official.getDesignation(), false));
        results.add(compareExact("exitReason", submitted.getExitReason(), official.getExitReason(), false));

        return results;
    }

    @Override
    public int calculateScore(List<ComparisonResultDto> results) {
        if (results.isEmpty()) return 0;
        long matched = results.stream().filter(ComparisonResultDto::isMatch).count();
        return (int) Math.round((double) matched / results.size() * 100);
    }

    @Override
    public String determineStatus(int score) {
        if (score == 100) return "matched";
        if (score >= 70) return "partial_match";
        return "mismatch";
    }

    private ComparisonResultDto compareExact(String field, String verifierValue, String companyValue, boolean caseSensitive) {
        if (isBlank(verifierValue)) {
            return notProvided(field, companyValue);
        }
        String v = verifierValue.trim();
        String c = companyValue != null ? companyValue.trim() : "";
        boolean match = caseSensitive ? v.equals(c) : v.equalsIgnoreCase(c);
        return ComparisonResultDto.builder()
                .field(field)
                .verifierValue(v)
                .companyValue(c)
                .isMatch(match)
                .matchType(match ? "exact" : "mismatch")
                .build();
    }

    private ComparisonResultDto compareIgnoreCase(String field, String verifierValue, String companyValue) {
        return compareExact(field, verifierValue, companyValue, false);
    }

    private ComparisonResultDto compareDate(String field, String verifierValue, String companyValue) {
        if (isBlank(verifierValue)) {
            return notProvided(field, companyValue);
        }
        if (isBlank(companyValue)) {
            return ComparisonResultDto.builder()
                    .field(field).verifierValue(verifierValue).companyValue(null)
                    .isMatch(false).matchType("mismatch").build();
        }
        try {
            LocalDate vDate = parseDate(verifierValue.trim());
            LocalDate cDate = parseDate(companyValue.trim());
            boolean match = Math.abs(ChronoUnit.DAYS.between(vDate, cDate)) <= DATE_TOLERANCE_DAYS;
            return ComparisonResultDto.builder()
                    .field(field)
                    .verifierValue(verifierValue.trim())
                    .companyValue(companyValue.trim())
                    .isMatch(match)
                    .matchType(match ? "exact" : "mismatch")
                    .build();
        } catch (DateTimeParseException e) {
            return ComparisonResultDto.builder()
                    .field(field).verifierValue(verifierValue).companyValue(companyValue)
                    .isMatch(false).matchType("mismatch").build();
        }
    }

    private ComparisonResultDto notProvided(String field, String companyValue) {
        return ComparisonResultDto.builder()
                .field(field)
                .verifierValue(null)
                .companyValue(companyValue)
                .isMatch(false)
                .matchType("not_provided")
                .build();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private LocalDate parseDate(String s) {
        // Try ISO first, then common formats
        for (String pattern : new String[]{"yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy", "dd-MM-yyyy"}) {
            try {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern(pattern));
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new DateTimeParseException("Cannot parse date: " + s, s, 0);
    }
}
