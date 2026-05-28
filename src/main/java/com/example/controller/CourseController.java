package com.example.controller;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import com.example.dto.CourseDto;
import com.example.dto.StudentDto;
import com.example.entity.Course;
import com.example.service.CourseService;
import com.example.service.StudentService;
import com.example.service.EnrollmentService;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;
    private final StudentService studentService;
    private final EnrollmentService enrollmentService;

    public CourseController(CourseService courseService, StudentService studentService, EnrollmentService enrollmentService) {
        this.courseService = courseService;
        this.studentService = studentService;
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public ResponseEntity<CourseDto> create(@Valid @RequestBody CourseDto courseDto) {
        Course saved = courseService.create(Objects.requireNonNull(courseDto.toEntity()));
        return ResponseEntity.ok(CourseDto.fromEntity(saved));
    }

    @GetMapping
    public List<CourseDto> list() {
        return courseService.list().stream()
                .map(CourseDto::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> get(@PathVariable @NonNull Long id) {
        return courseService.get(id)
                .map(CourseDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    record UpdateCourseRequest(@NotBlank(message = "Name is required") String name) {}

    @PutMapping("/{id}")
    public ResponseEntity<CourseDto> update(@PathVariable @NonNull Long id, @Valid @RequestBody UpdateCourseRequest request) {
        return courseService.updateName(id, request.name())
                .map(CourseDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable @NonNull Long id) {
        return courseService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/enroll/{studentId}")
    public ResponseEntity<Void> enrollStudent(@PathVariable @NonNull Long id, @PathVariable @NonNull Long studentId) {
        if (courseService.get(id).isEmpty() || studentService.get(studentId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        enrollmentService.enroll(id, studentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/students")
    public List<StudentDto> listStudents(@PathVariable @NonNull Long id) {
        return enrollmentService.getStudentIdsInCourse(id).stream()
                .map(studentService::get)
                .flatMap(Optional::stream)
                .map(StudentDto::fromEntity)
                .toList();
    }
}