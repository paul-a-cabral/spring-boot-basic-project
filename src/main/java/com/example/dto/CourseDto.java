package com.example.dto;

import java.time.Instant;
import com.example.entity.Course;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.Nullable;

public class CourseDto {

    private Long id;

    @NotBlank(message = "Course name is required")
    private String name;

    @JsonProperty(access = Access.READ_ONLY)
    private Instant createdAt;

    @JsonProperty(access = Access.READ_ONLY)
    private Instant updatedAt;

    public CourseDto() {}

    public CourseDto(Long id, String name, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Nullable
    public static CourseDto fromEntity(@Nullable Course course) {
        if (course == null) return null;
        return new CourseDto(course.getId(), course.getName(), course.getCreatedAt(), course.getUpdatedAt());
    }

    public Course toEntity() {
        return new Course(getId(), getName());
    }
}