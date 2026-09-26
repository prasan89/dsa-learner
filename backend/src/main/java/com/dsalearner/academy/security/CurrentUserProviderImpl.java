package com.dsalearner.academy.security;

import com.dsalearner.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserProviderImpl implements CurrentUserProvider {

    @Override
    public UUID getUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return UUID.fromString(userDetails.getUsername());
        }
        throw new UnauthorizedException("Cannot resolve user identity from authentication principal");
    }
}
