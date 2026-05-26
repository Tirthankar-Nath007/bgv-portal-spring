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
            log.warn("Sequence {} not found, using max-based fallback: {}", sequenceName, e.getMessage());
            // Use MAX of numeric suffix to avoid collisions when records have been deleted
            String idColumn = prefix.equals("VER") ? "verification_id" : "appeal_id";
            String table = prefix.equals("VER") ? "BGV_VERIFICATION_RECORDS" : "BGV_APPEALS";
            String sql = "SELECT NVL(MAX(TO_NUMBER(SUBSTR(" + idColumn + ", " + (prefix.length() + 1) + "))), 0) + 1 FROM " + table;
            Long next = jdbcTemplate.queryForObject(sql, Long.class);
            return String.format("%s%06d", prefix, next == null ? 1 : next);
        }
    }
}
