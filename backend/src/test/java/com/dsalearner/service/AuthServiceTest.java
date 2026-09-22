package com.dsalearner.service;

import com.dsalearner.dto.request.*;
import com.dsalearner.model.entity.*;
import com.dsalearner.repository.*;
import com.dsalearner.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
 @Mock UserRepository users; @Mock RefreshTokenRepository tokens; @Mock JwtService jwt; @Mock PasswordEncoder encoder;
 @Test void registerSuccessAndDuplicate(){
   AuthService s=new AuthService(users,tokens,jwt,encoder); UUID id=UUID.randomUUID(); User u=mock(User.class);
   when(users.existsByEmail("a@b.com")).thenReturn(false); when(encoder.encode("Pass1")).thenReturn("hash");
   when(users.save(any(User.class))).thenReturn(u); when(u.getId()).thenReturn(id); when(u.getEmail()).thenReturn("a@b.com"); when(u.getName()).thenReturn("A");
   when(jwt.generateAccessToken(isNull(),eq("a@b.com"))).thenReturn("access"); when(jwt.generateRefreshToken(isNull())).thenReturn("refresh"); when(jwt.getRefreshTokenExpiryMs()).thenReturn(1000L);
   assertEquals("access",s.register(new RegisterRequest("A","a@b.com","Pass1")).accessToken());
   verify(tokens).save(any(RefreshToken.class));
   when(users.existsByEmail("a@b.com")).thenReturn(true);
   assertThrows(com.dsalearner.exception.ConflictException.class,()->s.register(new RegisterRequest("A","a@b.com","Pass1")));
 }
 @Test void loginRejectsUnknownAndWrongPassword(){
   AuthService s=new AuthService(users,tokens,jwt,encoder);
   when(users.findByEmail("x")).thenReturn(Optional.empty());
   assertThrows(BadCredentialsException.class,()->s.login(new LoginRequest("x","p")));
   User u=mock(User.class); when(users.findByEmail("x")).thenReturn(Optional.of(u)); when(u.getPasswordHash()).thenReturn("hash"); when(encoder.matches("bad","hash")).thenReturn(false);
   assertThrows(BadCredentialsException.class,()->s.login(new LoginRequest("x","bad")));
 }
 @Test void refreshRejectsInvalidAndExpired(){
   AuthService s=new AuthService(users,tokens,jwt,encoder);
   when(tokens.findByToken("bad")).thenReturn(Optional.empty());
   assertThrows(com.dsalearner.exception.UnauthorizedException.class,()->s.refresh(new RefreshTokenRequest("bad")));
   RefreshToken rt=mock(RefreshToken.class); when(tokens.findByToken("old")).thenReturn(Optional.of(rt)); when(rt.isExpired()).thenReturn(true);
   assertThrows(com.dsalearner.exception.UnauthorizedException.class,()->s.refresh(new RefreshTokenRequest("old"))); verify(tokens).delete(rt);
 }
 @Test void refreshRotatesToken(){
   AuthService s=new AuthService(users,tokens,jwt,encoder); UUID id=UUID.randomUUID(); User u=mock(User.class); when(u.getId()).thenReturn(id);
   RefreshToken rt=mock(RefreshToken.class); when(tokens.findByToken("old")).thenReturn(Optional.of(rt)); when(rt.isExpired()).thenReturn(false); when(rt.getUser()).thenReturn(u);
   when(jwt.generateAccessToken(id,null)).thenReturn("a"); when(jwt.generateRefreshToken(id)).thenReturn("r"); when(jwt.getRefreshTokenExpiryMs()).thenReturn(1000L);
   assertEquals("a",s.refresh(new RefreshTokenRequest("old")).accessToken()); verify(tokens).delete(rt); verify(tokens).save(any(RefreshToken.class));
 }
 @Test void logoutAndMe(){
   AuthService s=new AuthService(users,tokens,jwt,encoder); UUID id=UUID.randomUUID(); User u=mock(User.class);
   when(users.findById(id)).thenReturn(Optional.of(u)); when(u.getId()).thenReturn(id); when(u.getEmail()).thenReturn("e"); when(u.getName()).thenReturn("N");
   s.logout(id.toString()); verify(tokens).deleteAllByUserId(id);
   assertEquals("e",s.me(id.toString()).email());
   when(users.findById(id)).thenReturn(Optional.empty());
   assertThrows(com.dsalearner.exception.UnauthorizedException.class,()->s.me(id.toString()));
 }
}
