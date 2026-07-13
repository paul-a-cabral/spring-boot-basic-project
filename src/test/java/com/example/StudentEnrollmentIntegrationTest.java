package com.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.entity.Course;
import com.example.entity.Student;
import com.example.repository.CourseRepository;
import com.example.repository.EnrollmentRepository;
import com.example.repository.StudentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rolls back changes after each test to keep DB clean
public class StudentEnrollmentIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private StudentRepository studentRepository;

  @Autowired private CourseRepository courseRepository;

  @Autowired private EnrollmentRepository enrollmentRepository;

  @Test
  @DisplayName("End-to-End: Create Student, Course, and Enroll via API")
  void fullEnrollmentFlow() throws Exception {
    // 1. Setup: Manually save entities to the H2 DB
    Student student =
        studentRepository.save(new Student(null, "Alice Smith", "alice@my-domain.com"));
    Course course = courseRepository.save(new Course(null, "Cloud Architecture"));

    // 2. Action: Call the enrollment endpoint
    mockMvc
        .perform(post("/api/courses/" + course.getId() + "/enroll/" + student.getId()))
        .andExpect(status().isOk());

    // 3. Verification: Check the database relationship
    boolean exists =
        enrollmentRepository.existsByCourseIdAndStudentId(course.getId(), student.getId());
    assertThat(exists).isTrue();

    // Verify JPA Auditing worked on the enrollment
    var enrollment = enrollmentRepository.findByCourseId(course.getId()).get(0);
    assertThat(enrollment.getEnrolledAt()).isNotNull();
  }
}
