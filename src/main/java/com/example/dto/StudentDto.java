package com.example.dto;

import com.example.entity.Student;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

@Data // ✅ Generates Getters, Setters, toString, equals, and hashCode
@NoArgsConstructor // ✅ Generates the empty constructor (needed by Jackson for deserialization)
@AllArgsConstructor // ✅ Generates the full constructor (used in fromEntity)
@Builder // ✅ Optional, but highly recommended for DTOs to make object creation cleaner
public class StudentDto {

  private Long id;

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  private String email;

  @JsonProperty(access = Access.READ_ONLY)
  private Instant createdAt;

  @JsonProperty(access = Access.READ_ONLY)
  private Instant updatedAt;

  @Nullable
  public static StudentDto fromEntity(@Nullable Student student) {
    if (student == null) {
      return null;
    }
    // You can still use the AllArgsConstructor:
    return new StudentDto(
        student.getId(),
        student.getName(),
        student.getEmail(),
        student.getCreatedAt(),
        student.getUpdatedAt());
  }

  public Student toEntity() {
    return new Student(getId(), getName(), getEmail());
  }
}
