package com.dsalearner.academy.security;

import com.dsalearner.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class CurrentUserProviderImplTest {

    private final CurrentUserProviderImpl provider = new CurrentUserProviderImpl();

    @Test
    void getUserId_extractsUuidFromUserDetails() {
        UUID userId = UUID.randomUUID();
        UserDetails userDetails = User.withUsername(userId.toString())
                .password("irrelevant").authorities(List.of()).build();
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        UUID result = provider.getUserId(auth);

        assertThat(result).isEqualTo(userId);
    }

    @Test
    void getUserId_throwsWhenAuthenticationIsNull() {
        assertThatThrownBy(() -> provider.getUserId(null))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Authentication required");
    }

    @Test
    void getUserId_throwsWhenNotAuthenticated() {
        Authentication auth = new UsernamePasswordAuthenticationToken(null, null);

        assertThatThrownBy(() -> provider.getUserId(auth))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getUserId_throwsWhenPrincipalIsNotUserDetails() {
        Authentication auth = new UsernamePasswordAuthenticationToken("not-user-details", null, List.of());

        assertThatThrownBy(() -> provider.getUserId(auth))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("principal");
    }
}
