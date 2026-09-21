package com.dsalearner.service;

import com.dsalearner.dto.request.LoginRequest;
import com.dsalearner.dto.request.RefreshTokenRequest;
import com.dsalearner.dto.request.RegisterRequest;
import com.dsalearner.dto.response.AuthResponse;
import com.dsalearner.dto.response.TokenResponse;
import com.dsalearner.dto.response.UserResponse;
import com.dsalearner.exception.ConflictException;
import com.dsalearner.exception.UnauthorizedException;
import com.dsalearner.model.entity.RefreshToken;
import com.dsalearner.model.entity.User;
import com.dsalearner.repository.RefreshTokenRepository;
import com.dsalearner.repository.UserRepository;
import com.dsalearner.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email already registered");
        }

        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .build();
        userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return issueTokens(user);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest req) {
        RefreshToken stored = refreshTokenRepository.findByToken(req.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new UnauthorizedException("Refresh token expired");
        }

        refreshTokenRepository.delete(stored);

        User user = stored.getUser();
        String newAccess = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String newRefresh = jwtService.generateRefreshToken(user.getId());
        saveRefreshToken(user, newRefresh);

        return new TokenResponse(newAccess, newRefresh);
    }

    @Transactional
    public void logout(String userId) {
        userRepository.findById(java.util.UUID.fromString(userId))
                .ifPresent(user -> refreshTokenRepository.deleteAllByUserId(user.getId()));
    }

    public UserResponse me(String userId) {
        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return toUserResponse(user);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        saveRefreshToken(user, refreshToken);
        return new AuthResponse(toUserResponse(user), accessToken, refreshToken);
    }

    private void saveRefreshToken(User user, String token) {
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpiryMs()))
                .build();
        refreshTokenRepository.save(rt);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(),
                user.getAvatarUrl(), user.getCreatedAt());
    }
}
