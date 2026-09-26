package com.dsalearner.academy.security;

import org.springframework.security.core.Authentication;

import java.util.UUID;

public interface CurrentUserProvider {
    UUID getUserId(Authentication authentication);
}
