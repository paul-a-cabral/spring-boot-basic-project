package com.example;

import com.example.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

public class EnrollmentServiceTest {

    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        enrollmentService = new EnrollmentService();
    }

    @Test
    void enrollShouldAddStudentToCourse() {
        enrollmentService.enroll(101L, 1L);

        assertThat(enrollmentService.getStudentIdsInCourse(101L)).containsExactly(1L);
        assertThat(enrollmentService.getCourseIdsForStudent(1L)).containsExactly(101L);
    }

    @Test
    void enrollShouldBeIdempotent() {
        enrollmentService.enroll(101L, 1L);
        enrollmentService.enroll(101L, 1L); // Second attempt

        assertThat(enrollmentService.getStudentIdsInCourse(101L)).hasSize(1);
        assertThat(enrollmentService.getCourseIdsForStudent(1L)).hasSize(1);
    }

    @Test
    void shouldHandleMultipleEnrollments() {
        enrollmentService.enroll(101L, 1L);
        enrollmentService.enroll(101L, 2L);
        enrollmentService.enroll(102L, 1L);

        assertThat(enrollmentService.getStudentIdsInCourse(101L)).containsExactlyInAnyOrder(1L, 2L);
        assertThat(enrollmentService.getCourseIdsForStudent(1L)).containsExactlyInAnyOrder(101L, 102L);
    }
}