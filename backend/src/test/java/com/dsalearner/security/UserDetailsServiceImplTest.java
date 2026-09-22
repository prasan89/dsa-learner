package com.dsalearner.security;

import com.dsalearner.model.entity.User;
import com.dsalearner.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {
  @Mock UserRepository repo;

  @Test void loadsByUuid() {
    UUID id=UUID.randomUUID(); User u=mock(User.class);
    when(u.getId()).thenReturn(id); when(u.getPasswordHash()).thenReturn("hash");
    when(repo.findById(id)).thenReturn(Optional.of(u));
    var result=new UserDetailsServiceImpl(repo).loadUserByUsername(id.toString());
    assertEquals(id.toString(),result.getUsername()); assertEquals("hash",result.getPassword());
    assertTrue(result.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_USER")));
  }

  @Test void fallsBackToEmail() {
    UUID id=UUID.randomUUID(); User u=mock(User.class);
    when(u.getId()).thenReturn(id); when(u.getPasswordHash()).thenReturn("hash");
    when(repo.findByEmail("u@test.com")).thenReturn(Optional.of(u));
    assertEquals(id.toString(),new UserDetailsServiceImpl(repo).loadUserByUsername("u@test.com").getUsername());
  }

  @Test void unknownUserThrows() {
    when(repo.findByEmail(any())).thenReturn(Optional.empty());
    assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
      ()->new UserDetailsServiceImpl(repo).loadUserByUsername("missing"));
  }
}
