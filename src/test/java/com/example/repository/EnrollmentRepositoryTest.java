package com.example.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.entity.Course;
import com.example.entity.Enrollment;
import com.example.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class EnrollmentRepositoryTest {

  @Autowired private TestEntityManager entityManager;

  @Autowired private EnrollmentRepository enrollmentRepository;

  @Test
  void shouldPersistEnrollmentWithAuditTimestamp() {
    Student student = entityManager.persist(new Student(null, "Test Student", "test@example.com"));
    Course course = entityManager.persist(new Course(null, "Test Course"));

    Enrollment enrollment = new Enrollment(student, course);
    Enrollment saved = enrollmentRepository.save(enrollment);

    entityManager.flush();
    entityManager.clear();

    Enrollment found = enrollmentRepository.findById(saved.getId()).orElseThrow();
    assertThat(found.getStudent().getName()).isEqualTo("Test Student");
    assertThat(found.getEnrolledAt()).isNotNull(); // Verifies JPA Auditing
  }
}
