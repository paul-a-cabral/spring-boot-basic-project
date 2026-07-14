package com.example.service;

import com.example.entity.Course;
import com.example.entity.Enrollment;
import com.example.entity.Student;
import com.example.repository.CourseRepository;
import com.example.repository.EnrollmentRepository;
import com.example.repository.StudentRepository;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentService {

  private final EnrollmentRepository enrollmentRepository;
  private final StudentRepository studentRepository;
  private final CourseRepository courseRepository;

  public EnrollmentService(
      EnrollmentRepository enrollmentRepository,
      StudentRepository studentRepository,
      CourseRepository courseRepository) {
    this.enrollmentRepository = enrollmentRepository;
    this.studentRepository = studentRepository;
    this.courseRepository = courseRepository;
  }

  @Transactional
  public void enroll(Long courseId, Long studentId) {
    if (enrollmentRepository.existsByCourseIdAndStudentId(courseId, studentId)) {
      return; // Maintain idempotency
    }

    Student student =
        studentRepository
            .findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found"));
    Course course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found"));

    enrollmentRepository.save(new Enrollment(student, course));
  }

  public Set<Long> getStudentIdsInCourse(Long courseId) {
    return enrollmentRepository.findByCourseId(courseId).stream()
        .map(enrollment -> enrollment.getStudent().getId())
        .collect(Collectors.toSet());
  }

  public Set<Long> getCourseIdsForStudent(Long studentId) {
    return enrollmentRepository.findByStudentId(studentId).stream()
        .map(enrollment -> enrollment.getCourse().getId())
        .collect(Collectors.toSet());
  }
}
