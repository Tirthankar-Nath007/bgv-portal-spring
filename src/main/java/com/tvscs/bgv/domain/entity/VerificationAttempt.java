package com.tvscs.bgv.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "BGV_VERIFICATION_ATTEMPTS",
        uniqueConstraints = @UniqueConstraint(columnNames = {"verifier_id", "employee_id"}))
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verifier_id", nullable = false)
    private Long verifierId;

    @Column(name = "employee_id", nullable = false, length = 100)
    private String employeeId;

    @Column(name = "attempt_count")
    private int attemptCount;

    @Column(name = "is_blocked")
    private boolean isBlocked;

    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
