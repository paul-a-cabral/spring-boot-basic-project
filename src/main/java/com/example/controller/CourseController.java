package com.example.controller;

import com.example.dto.CourseRequestDto;
import com.example.dto.CourseResponseDto;
import com.example.dto.StudentDto;
import com.example.entity.Course;
import com.example.service.CourseService;
import com.example.service.EnrollmentService;
import com.example.service.StudentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

  private final CourseService courseService;
  private final StudentService studentService;
  private final EnrollmentService enrollmentService;

  public CourseController(
      CourseService courseService,
      StudentService studentService,
      EnrollmentService enrollmentService) {
    this.courseService = courseService;
    this.studentService = studentService;
    this.enrollmentService = enrollmentService;
  }

  @PostMapping
  public ResponseEntity<CourseResponseDto> create(
      @Valid @RequestBody CourseRequestDto courseRequestDto) {
    Course saved = courseService.create(courseRequestDto.toEntity());
    return ResponseEntity.ok(CourseResponseDto.fromEntity(saved));
  }

  @GetMapping
  public List<CourseResponseDto> list() {
    return courseService.list().stream().map(CourseResponseDto::fromEntity).toList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<CourseResponseDto> get(@PathVariable @NonNull Long id) {
    return courseService
        .get(id)
        .map(CourseResponseDto::fromEntity)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<CourseResponseDto> update(
      @PathVariable @NonNull Long id, @Valid @RequestBody CourseRequestDto request) {
    return courseService
        .updateName(id, request.name())
        .map(CourseResponseDto::fromEntity)
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
  public ResponseEntity<Void> enrollStudent(
      @PathVariable @NonNull Long id, @PathVariable @NonNull Long studentId) {
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
        .flatMap(opt -> opt.stream())
        .map(StudentDto::fromEntity)
        .toList();
  }
}
