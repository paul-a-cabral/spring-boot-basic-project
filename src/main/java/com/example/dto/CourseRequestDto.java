package com.example.dto;

import com.example.entity.Course;
import jakarta.validation.constraints.NotBlank;

public record CourseRequestDto(@NotBlank(message = "Course name is required") String name) {

  public Course toEntity() {
    return new Course(null, name);
  }
}
