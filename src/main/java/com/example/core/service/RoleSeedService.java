package com.example.core.service;

import com.example.core.entity.Role;
import com.example.core.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleSeedService {

  private final RoleRepository roleRepository;

  public RoleSeedService(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  public Role getRequiredRoleByCode(String code) {
    return roleRepository
        .findByCode(code)
        .orElseThrow(() -> new IllegalStateException("Missing role seed data for code: " + code));
  }
}
