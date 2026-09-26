package com.dsalearner.security;

import com.dsalearner.model.entity.User;
import com.dsalearner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final JdbcTemplate jdbc;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        User user;
        try {
            user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        } catch (IllegalArgumentException e) {
            user = userRepository.findByEmail(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        jdbc.queryForList(
                "SELECT domain_code FROM user_learning_domains WHERE user_id = ?::uuid AND is_active = true",
                String.class,
                user.getId().toString()
        ).forEach(code -> authorities.add(new SimpleGrantedAuthority("ROLE_DOMAIN_" + code.toUpperCase())));

        return new org.springframework.security.core.userdetails.User(
                user.getId().toString(),
                user.getPasswordHash(),
                authorities
        );
    }
}
