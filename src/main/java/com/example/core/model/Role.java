package com.example.core.model;

import static com.example.core.model.Permission.*;

import java.util.Set;

public enum Role {
  GUEST(Set.of()),

  USER(Set.of(CAN_READ)),

  ADMIN(Set.of(CAN_READ, CAN_WRITE, CAN_EDIT, CAN_DELETE)),

  AUDITOR(Set.of(CAN_READ, CAN_AUDIT));

  private final Set<Permission> permissions;

  Role(Set<Permission> permissions) {
    this.permissions = permissions;
  }

  public Set<Permission> getPermissions() {
    return permissions;
  }
}
