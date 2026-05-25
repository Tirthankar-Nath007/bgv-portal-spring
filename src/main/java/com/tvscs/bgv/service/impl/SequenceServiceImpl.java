package com.tvscs.bgv.service.impl;

import com.tvscs.bgv.service.SequenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SequenceServiceImpl implements SequenceService {

    private final JdbcTemplate jdbcTemplate;

    public SequenceServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String nextVerificationId() {
        return generateId("BGV_VER_SEQ", "VER");
    }

    @Override
    public String nextAppealId() {
        return generateId("BGV_APP_SEQ", "APP");
    }

    private String generateId(String sequenceName, String prefix) {
        try {
            Long next = jdbcTemplate.queryForObject(
                    "SELECT " + sequenceName + ".NEXTVAL FROM DUAL", Long.class);
            return String.format("%s%06d", prefix, next);
        } catch (Exception e) {
            // Fallback: use COUNT+1 if sequence doesn't exist
            log.warn("Sequence {} not found, using count-based fallback: {}", sequenceName, e.getMessage());
            String table = prefix.equals("VER") ? "BGV_VERIFICATION_RECORDS" : "BGV_APPEALS";
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
            return String.format("%s%06d", prefix, (count == null ? 0 : count) + 1);
        }
    }
}
