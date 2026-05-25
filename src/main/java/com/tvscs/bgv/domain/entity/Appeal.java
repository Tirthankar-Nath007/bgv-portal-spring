package com.tvscs.bgv.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "BGV_APPEALS")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Appeal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appeal_id", unique = true, nullable = false, length = 20)
    private String appealId;

    @Column(name = "verification_id", nullable = false)
    private Long verificationId;

    @Column(name = "verifier_id", nullable = false)
    private Long verifierId;

    @Column(name = "employee_id", length = 100)
    private String employeeId;

    @Lob
    @Column(name = "appeal_reason", columnDefinition = "CLOB")
    private String appealReason;

    @Lob
    @Column(name = "documents", columnDefinition = "CLOB")
    private String documents;

    @Lob
    @Column(name = "mismatched_fields", columnDefinition = "CLOB")
    private String mismatchedFields;

    @Column(name = "status", length = 20)
    private String status;

    @Lob
    @Column(name = "hr_response", columnDefinition = "CLOB")
    private String hrResponse;

    @Lob
    @Column(name = "hr_comments", columnDefinition = "CLOB")
    private String hrComments;

    @Column(name = "reviewed_by", length = 255)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
