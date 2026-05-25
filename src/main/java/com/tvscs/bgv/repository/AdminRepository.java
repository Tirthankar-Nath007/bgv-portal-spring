package com.tvscs.bgv.repository;

import com.tvscs.bgv.domain.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    @Query("SELECT a FROM Admin a WHERE LOWER(a.username) = LOWER(:username)")
    Optional<Admin> findByUsernameIgnoreCase(String username);

    Optional<Admin> findByEmail(String email);

    @Query("SELECT a.email FROM Admin a WHERE a.isActive = true")
    List<String> findAllActiveAdminEmails();
}
