package com.example.core.service;

import com.example.core.entity.Permission;
import com.example.core.entity.Role;
import com.example.core.repository.UserRepository;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
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
    Role role = user.getRole();
    if (role == null) {
      throw new UsernameNotFoundException(
          "User " + user.getUsername() + " does not have a role assigned");
    }

    return User.builder()
        .username(user.getUsername())
        .password(user.getPassword())
        .authorities(getAuthorities(role))
        .build();
  }

  private Collection<? extends GrantedAuthority> getAuthorities(Role role) {
    logger.info("Mapping role {} to authorities", role.getCode());

    Set<GrantedAuthority> authorities = new LinkedHashSet<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
    authorities.addAll(
        role.getPermissions().stream()
            .map(Permission::getCode)
            .map(SimpleGrantedAuthority::new)
            .toList());
    return authorities;
  }
}
