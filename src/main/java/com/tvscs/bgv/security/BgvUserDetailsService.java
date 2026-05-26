package com.tvscs.bgv.security;

import com.tvscs.bgv.repository.AdminRepository;
import com.tvscs.bgv.repository.VerifierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BgvUserDetailsService implements UserDetailsService {

    private final VerifierRepository verifierRepository;
    private final AdminRepository adminRepository;

    // subject format: "VERIFIER:email" or "ADMIN:username"
    @Override
    public UserDetails loadUserByUsername(String subject) throws UsernameNotFoundException {
        int colonIdx = subject.indexOf(':');
        if (colonIdx < 0) {
            throw new UsernameNotFoundException("Invalid subject format: " + subject);
        }
        String userType = subject.substring(0, colonIdx);
        String identifier = subject.substring(colonIdx + 1);

        return switch (userType) {
            case "VERIFIER" -> verifierRepository.findByEmailIgnoreCase(identifier)
                    .map(UserPrincipal::fromVerifier)
                    .orElseThrow(() -> new UsernameNotFoundException("Verifier not found: " + identifier));
            case "ADMIN" -> adminRepository.findByUsernameIgnoreCase(identifier)
                    .map(UserPrincipal::fromAdmin)
                    .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + identifier));
            default -> throw new UsernameNotFoundException("Unknown user type: " + userType);
        };
    }
}
