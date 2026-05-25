package com.tvscs.bgv.security;

import com.tvscs.bgv.domain.entity.Admin;
import com.tvscs.bgv.domain.entity.Verifier;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username; // composite: USERTYPE:identifier
    private final String password;
    private final String role;
    private final boolean active;

    private UserPrincipal(Long id, String username, String password, String role, boolean active) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = active;
    }

    public static UserPrincipal fromVerifier(Verifier v) {
        return new UserPrincipal(
                v.getId(),
                "VERIFIER:" + v.getEmail(),
                v.getPassword(),
                "VERIFIER",
                v.isActive()
        );
    }

    public static UserPrincipal fromAdmin(Admin a) {
        String springRole = a.getRole().toUpperCase().replace(" ", "_");
        return new UserPrincipal(
                a.getId(),
                "ADMIN:" + a.getUsername(),
                a.getPassword(),
                springRole,
                a.isActive()
        );
    }

    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
