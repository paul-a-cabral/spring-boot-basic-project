package com.example.dto;

import com.example.entity.Course;
import java.time.Instant;
import org.springframework.lang.Nullable;

public record CourseResponseDto(Long id, String name, Instant createdAt, Instant updatedAt) {

  @Nullable
  public static CourseResponseDto fromEntity(@Nullable Course course) {
    if (course == null) {
      return null;
    }

    return new CourseResponseDto(
        course.getId(), course.getName(), course.getCreatedAt(), course.getUpdatedAt());
  }
}
