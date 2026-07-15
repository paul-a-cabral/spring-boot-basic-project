package com.example.core.model;

import java.util.Set;

public enum Role {
  USER(Set.of(Permission.CAN_READ)),

  ADMIN(Set.of(Permission.CAN_READ, Permission.CAN_EDIT, Permission.CAN_DELETE)),

  AUDITOR(Set.of(Permission.CAN_READ, Permission.CAN_EDIT, Permission.CAN_AUDIT));

  private final Set<Permission> permissions;

  Role(Set<Permission> permissions) {
    this.permissions = permissions;
  }

  public Set<Permission> getPermissions() {
    return permissions;
  }
}
