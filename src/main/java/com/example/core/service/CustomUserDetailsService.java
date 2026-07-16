package com.example.core.service;

import com.example.core.model.Role;
import com.example.core.repository.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository
        .findByUsername(username)
        .map(this::mapToUserDetails)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
  }

  private UserDetails mapToUserDetails(com.example.core.entity.User user) {
    return User.builder()
        .username(user.getUsername())
        .password(user.getPassword())
        .authorities(
            getAuthorities(
                user.getRole() != null
                    ? Role.valueOf(user.getRole().replace("ROLE_", ""))
                    : Role.USER))
        .build();
  }

  private Collection<? extends GrantedAuthority> getAuthorities(Role role) {

    logger.info("Mapping role {} to authorities", role.name());

    List<GrantedAuthority> authorities = new ArrayList<>();

    // Add the role
    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

    // Add the permissions
    authorities.addAll(
        role.getPermissions().stream()
            .map(permission -> new SimpleGrantedAuthority(permission.name()))
            .toList());

    return authorities;
  }
}
