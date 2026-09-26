package com.dsalearner.security;

import com.dsalearner.repository.UserLearningDomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DomainAuthorizationService {

    private final JdbcTemplate jdbc;

    public void requireDomain(Authentication authentication, String requiredDomain) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }

        String requiredAuthority = "ROLE_DOMAIN_" + requiredDomain.toUpperCase();
        boolean hasDomain = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(requiredAuthority::equals);

        if (!hasDomain) {
            throw new AccessDeniedException(
                    "Your account is not enrolled in the '" + requiredDomain + "' learning domain");
        }
    }

    public String getActiveDomain(UUID userId) {
        return jdbc.queryForList(
                "SELECT domain_code FROM user_learning_domains WHERE user_id = ?::uuid AND is_active = true LIMIT 1",
                String.class,
                userId.toString()
        ).stream().findFirst().orElse(null);
    }
}
