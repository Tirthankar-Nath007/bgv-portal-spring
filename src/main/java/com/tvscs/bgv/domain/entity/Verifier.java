package com.tvscs.bgv.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "BGV_VERIFIERS")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Verifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "is_email_verified")
    private boolean isEmailVerified;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "is_bgv_agency")
    private boolean isBgvAgency;

    @Column(name = "test_mode")
    private boolean testMode;

    @Lob
    @Column(name = "notifications", columnDefinition = "CLOB")
    private String notifications;

    @Lob
    @Column(name = "verification_requests", columnDefinition = "CLOB")
    private String verificationRequests;

    @Column(name = "bypass_token", length = 255)
    private String bypassToken;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
