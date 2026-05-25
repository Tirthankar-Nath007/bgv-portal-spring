package com.tvscs.bgv.service;

import com.tvscs.bgv.domain.dto.ComparisonResultDto;
import com.tvscs.bgv.domain.dto.request.VerificationRequest;
import com.tvscs.bgv.domain.entity.Employee;

import java.util.List;

public interface ComparisonService {
    List<ComparisonResultDto> compare(VerificationRequest submitted, Employee official);
    int calculateScore(List<ComparisonResultDto> results);
    String determineStatus(int score);
}
