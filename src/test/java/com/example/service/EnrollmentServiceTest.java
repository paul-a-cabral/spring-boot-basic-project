package com.example.service;

import com.example.entity.Course;
import com.example.entity.Enrollment;
import com.example.entity.Student;
import com.example.repository.CourseRepository;
import com.example.repository.EnrollmentRepository;
import com.example.repository.StudentRepository;
import com.example.service.EnrollmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Student student;
    private Course course;

    @BeforeEach
    void setUp() {
        student = new Student(1L, "Jane Doe", "jane@example.com");
        course = new Course(101L, "Spring Boot");
    }

    @Test
    void enrollShouldSaveEnrollmentWhenNew() {
        // Given
        given(enrollmentRepository.existsByCourseIdAndStudentId(101L, 1L)).willReturn(false);
        given(studentRepository.findById(1L)).willReturn(Optional.of(student));
        given(courseRepository.findById(101L)).willReturn(Optional.of(course));

        // When
        enrollmentService.enroll(101L, 1L);

        // Then
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void getStudentIdsInCourseShouldReturnIds() {
        // Given
        Enrollment enrollment = new Enrollment(student, course);
        given(enrollmentRepository.findByCourseId(101L)).willReturn(List.of(enrollment));

        // When
        Set<Long> result = enrollmentService.getStudentIdsInCourse(101L);

        // Then
        assertThat(result).containsExactly(1L);
    }
}