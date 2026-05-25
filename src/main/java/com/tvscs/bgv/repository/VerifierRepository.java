package com.tvscs.bgv.repository;

import com.tvscs.bgv.domain.entity.Verifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VerifierRepository extends JpaRepository<Verifier, Long> {

    @Query("SELECT v FROM Verifier v WHERE LOWER(v.email) = LOWER(:email)")
    Optional<Verifier> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT v.email FROM Verifier v WHERE v.isActive = true")
    List<String> findAllActiveEmails();
}
