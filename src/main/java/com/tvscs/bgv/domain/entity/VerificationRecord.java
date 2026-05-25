package com.tvscs.bgv.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "BGV_VERIFICATION_RECORDS")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_id", unique = true, nullable = false, length = 20)
    private String verificationId;

    @Column(name = "employee_id", nullable = false, length = 100)
    private String employeeId;

    @Column(name = "verifier_id", nullable = false)
    private Long verifierId;

    @Lob
    @Column(name = "submitted_data", columnDefinition = "CLOB")
    private String submittedData;

    @Lob
    @Column(name = "comparison_results", columnDefinition = "CLOB")
    private String comparisonResults;

    @Column(name = "overall_status", length = 20)
    private String overallStatus;

    @Column(name = "match_score")
    private Integer matchScore;

    @Column(name = "consent_given")
    private boolean consentGiven;

    @Column(name = "pdf_report_url", length = 500)
    private String pdfReportUrl;

    @Column(name = "verification_completed_at")
    private LocalDateTime verificationCompletedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
