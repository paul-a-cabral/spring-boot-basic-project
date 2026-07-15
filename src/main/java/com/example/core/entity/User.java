package com.example.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Collection;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String username;

  @Column(nullable = false)
  private String password; // This must store the ENCRYPTED hash (e.g. BCrypt)

  private String role; // e.g., "ROLE_USER" or "ROLE_ADMIN"

  private String authority; // e.g., "AUDIT" or "WRITE_PRIVILEGE"

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // Expose both role and optional privilege authorities to method security checks.
    return Stream.of(role, authority)
        .filter(value -> value != null && !value.isBlank())
        .map(SimpleGrantedAuthority::new)
        .toList();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
