package com.example.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import com.example.dto.StudentDto;
import com.example.dto.CourseDto;
import com.example.entity.Student;
import com.example.service.StudentService;
import com.example.service.CourseService;
import com.example.service.EnrollmentService;
import com.example.util.StudentGenerator;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final String emailDomain;

    public StudentController(StudentService studentService,
                             CourseService courseService,
                             EnrollmentService enrollmentService,
                             @Value("${app.students.email.domain:example.com}") String emailDomain) {
        this.studentService = studentService;
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.emailDomain = emailDomain;
    }

    @PostMapping("/generate")
    public ResponseEntity<StudentDto> generate() {
        String name = StudentGenerator.generateName();
        String email = StudentGenerator.generateEmailFromName(name, emailDomain);
        Student saved = studentService.create(new Student(null, name, email));
        return ResponseEntity.ok(StudentDto.fromEntity(saved));
    }

    @PostMapping
    public ResponseEntity<StudentDto> create(@Valid @RequestBody StudentDto studentDto) {
        Student saved = studentService.create(Objects.requireNonNull(studentDto.toEntity()));
        return ResponseEntity.ok(StudentDto.fromEntity(saved));
    }

    @GetMapping
    public List<StudentDto> list() {
        return studentService.list().stream()
                .map(StudentDto::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> get(@PathVariable @NonNull Long id) {
        return studentService.get(id)
                .map(StudentDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<StudentDto> findByEmail(@RequestParam String email) {
        return studentService.findByEmail(email)
                .map(StudentDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    record UpdateNameRequest(@NotBlank(message = "Name is required") String name) {}

    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> update(@PathVariable @NonNull Long id, @Valid @RequestBody UpdateNameRequest request) {
        return studentService.updateName(id, request.name())
                .map(StudentDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable @NonNull Long id) {
        return studentService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/courses")
    public List<CourseDto> listCourses(@PathVariable @NonNull Long id) {
        return enrollmentService.getCourseIdsForStudent(id).stream()
                .map(courseService::get)
                .flatMap(Optional::stream)
                .map(CourseDto::fromEntity)
                .toList();
    }
}
