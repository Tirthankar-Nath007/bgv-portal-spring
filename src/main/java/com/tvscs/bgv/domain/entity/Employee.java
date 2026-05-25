package com.tvscs.bgv.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "BGV_EMPLOYEES")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", unique = true, nullable = false, length = 100)
    private String employeeId;

    @Column(name = "first_name", length = 150)
    private String firstName;

    @Column(name = "middle_name", length = 150)
    private String middleName;

    @Column(name = "last_name", length = 150)
    private String lastName;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "business", length = 255)
    private String business;

    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;

    @Column(name = "date_of_leaving")
    private LocalDate dateOfLeaving;

    @Column(name = "designation", length = 255)
    private String designation;

    @Column(name = "exit_reason", length = 255)
    private String exitReason;

    @Column(name = "department", length = 255)
    private String department;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getFullName() {
        return new StringBuilder()
                .append(firstName != null ? firstName : "")
                .append(middleName != null && !middleName.isBlank() ? " " + middleName : "")
                .append(lastName != null && !lastName.isBlank() ? " " + lastName : "")
                .toString().trim();
    }
}
