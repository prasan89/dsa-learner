package com.dsalearner.security;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
  private final JwtService service = new JwtService(
      "this-is-a-test-secret-key-with-at-least-32-bytes!!", 60_000, 120_000);

  @Test void accessTokenRoundTrip() {
    UUID id=UUID.randomUUID(); String token=service.generateAccessToken(id,"u@test.com");
    assertEquals(id,service.extractUserId(token));
    assertEquals("u@test.com",service.extractClaims(token).get("email"));
    assertEquals("access",service.extractClaims(token).get("type"));
    assertTrue(service.isTokenValid(token));
  }

  @Test void refreshTokenRoundTrip() {
    UUID id=UUID.randomUUID(); String token=service.generateRefreshToken(id);
    assertEquals(id,service.extractUserId(token));
    assertEquals("refresh",service.extractClaims(token).get("type"));
    assertEquals(120_000,service.getRefreshTokenExpiryMs());
  }

  @Test void malformedTokenIsInvalid() { assertFalse(service.isTokenValid("not-a-jwt")); }

  @Test void wrongSignatureIsInvalid() {
    JwtService other=new JwtService("another-test-secret-key-with-at-least-32-bytes!!",60_000,120_000);
    assertFalse(service.isTokenValid(other.generateAccessToken(UUID.randomUUID(),"x")));
  }
}
