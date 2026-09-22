package com.dsalearner.controller;

import com.dsalearner.dto.request.LoginRequest;
import com.dsalearner.dto.request.RefreshTokenRequest;
import com.dsalearner.dto.request.RegisterRequest;
import com.dsalearner.dto.response.AuthResponse;
import com.dsalearner.dto.response.TokenResponse;
import com.dsalearner.dto.response.UserResponse;
import com.dsalearner.exception.TooManyRequestsException;
import com.dsalearner.service.AuthService;
import com.dsalearner.service.LoginRateLimiter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService      authService;
    private final LoginRateLimiter loginRateLimiter;

    @Value("${app.jwt.access-token-expiry-ms}")
    private long accessTokenExpiryMs;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Value("${app.cookie.secure:true}")
    private boolean secureCookies;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody RegisterRequest req,
            HttpServletResponse response) {
        AuthResponse auth = authService.register(req);
        setAuthCookies(response, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.user());
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest request,
            HttpServletResponse response) {
        String ip = getClientIp(request);
        if (!loginRateLimiter.isAllowed(ip)) {
            throw new TooManyRequestsException("Too many login attempts. Try again in 15 minutes.");
        }
        AuthResponse auth = authService.login(req);
        loginRateLimiter.resetAttempts(ip);
        setAuthCookies(response, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.ok(auth.user());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refreshToken", required = false) String cookieRefresh,
            @RequestBody(required = false) RefreshTokenRequest bodyReq,
            HttpServletResponse response) {
        // Accept refresh token from HttpOnly cookie (preferred) or request body (legacy)
        String token = (cookieRefresh != null) ? cookieRefresh
                : (bodyReq != null ? bodyReq.refreshToken() : null);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TokenResponse tokens = authService.refresh(new RefreshTokenRequest(token));
        setAuthCookies(response, tokens.accessToken(), tokens.refreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) {
        if (userDetails != null) {
            authService.logout(userDetails.getUsername());
        }
        clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.me(userDetails.getUsername()));
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader("Set-Cookie", buildCookie(
                "accessToken", accessToken,
                (int) (accessTokenExpiryMs / 1000)));
        response.addHeader("Set-Cookie", buildCookie(
                "refreshToken", refreshToken,
                (int) (refreshTokenExpiryMs / 1000)));
    }

    private void clearAuthCookies(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildCookie("accessToken",  "", 0));
        response.addHeader("Set-Cookie", buildCookie("refreshToken", "", 0));
    }

    private String buildCookie(String name, String value, int maxAgeSeconds) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value).append(";");
        sb.append(" Max-Age=").append(maxAgeSeconds).append(";");
        sb.append(" Path=/;");
        sb.append(" HttpOnly;");
        sb.append(" SameSite=Lax;");
        if (secureCookies) sb.append(" Secure;");
        return sb.toString();
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
