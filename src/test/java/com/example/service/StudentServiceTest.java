package com.example.service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;

import com.example.entity.Student;
import com.example.repository.StudentRepository;
import com.example.service.StudentService;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private StudentRepository repository;

    @SuppressWarnings("null")
    @NonNull
    private StudentService studentService;

    @SuppressWarnings("null")
    @NonNull
    private Student student;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(repository, "example.com");
        student = new Student(1L, "Jane Doe", "jane.doe@example.com");
    }

    @Test
    void createShouldSaveAndReturnStudent() {
        given(repository.save(student)).willReturn(student);

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
    void updateNameShouldUpdateNameAndDeriveEmail() {
        Student updatedStudent = new Student(1L, "John Doe", "john.doe@example.com");

        given(repository.findById(1L)).willReturn(Optional.of(student));
        given(repository.save(student)).willReturn(updatedStudent);

        Optional<Student> result = studentService.updateName(1L, "John Doe");

        assertThat(result).contains(updatedStudent);
        then(repository).should().findById(1L);
        then(repository).should().save(student);
        assertThat(student.getName()).isEqualTo("John Doe");
        assertThat(student.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @SuppressWarnings("null")
    void updateNameShouldReturnEmptyWhenNotFound() {
        given(repository.findById(1L)).willReturn(Optional.empty());

        Optional<Student> result = studentService.updateName(1L, "John Doe");

        assertThat(result).isEmpty();
        then(repository).should().findById(1L);
        then(repository).should(never()).save(any(Student.class));
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
    @SuppressWarnings("null")
    void deleteShouldReturnFalseWhenNotFound() {
        given(repository.findById(1L)).willReturn(Optional.empty());

        boolean result = studentService.delete(1L);

        assertThat(result).isFalse();
        then(repository).should().findById(1L);
        then(repository).should(never()).delete(any(Student.class));
    }

    @Test
    void findByEmailShouldReturnStudentWhenFound() {
        given(repository.findByEmail("jane.doe@example.com")).willReturn(Optional.of(student));

        Optional<Student> result = studentService.findByEmail("jane.doe@example.com");

        assertThat(result).contains(student);
        then(repository).should().findByEmail("jane.doe@example.com");
    }
}
