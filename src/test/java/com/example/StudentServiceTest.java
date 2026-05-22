package com.example;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository repository;

    @InjectMocks
    private StudentService studentService;

    private Student student;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        student = new Student(1L, "Jane Doe", "jane.doe@example.com");
    }

    @Test
    void createShouldSaveAndReturnStudent() {
        given(repository.save(any(Student.class))).willReturn(student);

        Student result = studentService.create(student);

        assertThat(result).isSameAs(student);
        then(repository).should().save(student);
    }

    @Test
    void listShouldReturnAllStudents() {
        given(repository.findAll()).willReturn(List.of(student));

        List<Student> result = studentService.list();

        assertThat(result).containsExactly(student);
        then(repository).should().findAll();
    }

    @Test
    void getShouldReturnStudentWhenFound() {
        given(repository.findById(1L)).willReturn(Optional.of(student));

        Optional<Student> result = studentService.get(1L);

        assertThat(result).contains(student);
        then(repository).should().findById(1L);
    }

    @Test
    void updateShouldReturnUpdatedStudentWhenFound() {
        Student updatedRequest = new Student(null, "John Doe", "john.doe@example.com");
        Student updatedStudent = new Student(1L, "John Doe", "john.doe@example.com");

        given(repository.findById(1L)).willReturn(Optional.of(student));
        given(repository.save(any(Student.class))).willReturn(updatedStudent);

        Optional<Student> result = studentService.update(1L, updatedRequest);

        assertThat(result).contains(updatedStudent);
        then(repository).should().findById(1L);
        then(repository).should().save(student);
        assertThat(student.getName()).isEqualTo("John Doe");
        assertThat(student.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void updateShouldReturnEmptyWhenNotFound() {
        given(repository.findById(1L)).willReturn(Optional.empty());

        Optional<Student> result = studentService.update(1L, student);

        assertThat(result).isEmpty();
        then(repository).should().findById(1L);
        then(repository).should(never()).save(any());
    }

    @Test
    void deleteShouldRemoveStudentWhenFound() {
        given(repository.findById(1L)).willReturn(Optional.of(student));

        boolean result = studentService.delete(1L);

        assertThat(result).isTrue();
        then(repository).should().findById(1L);
        then(repository).should().delete(student);
    }

    @Test
    void deleteShouldReturnFalseWhenNotFound() {
        given(repository.findById(1L)).willReturn(Optional.empty());

        boolean result = studentService.delete(1L);

        assertThat(result).isFalse();
        then(repository).should().findById(1L);
        then(repository).should(never()).delete(any());
    }

    @Test
    void findByEmailShouldReturnStudentWhenFound() {
        given(repository.findByEmail("jane.doe@example.com")).willReturn(Optional.of(student));

        Optional<Student> result = studentService.findByEmail("jane.doe@example.com");

        assertThat(result).contains(student);
        then(repository).should().findByEmail("jane.doe@example.com");
    }
}
