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
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;

import com.example.entity.Course;
import com.example.repository.CourseRepository;
import com.example.service.CourseService;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository repository;

    @NonNull
    private CourseService courseService;

    @NonNull
    private Course course;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(repository);
        course = new Course(1L, "Mathematics");
    }

    @Test
    void createShouldSaveAndReturnCourse() {
        given(repository.save(course)).willReturn(course);

        Course result = courseService.create(course);

        assertThat(result).isSameAs(course);
        then(repository).should().save(course);
    }

    @Test
    void listShouldReturnAllCourses() {
        given(repository.findAll()).willReturn(List.of(course));

        List<Course> result = courseService.list();

        assertThat(result).containsExactly(course);
        then(repository).should().findAll();
    }

    @Test
    void getShouldReturnCourseWhenFound() {
        given(repository.findById(1L)).willReturn(Optional.of(course));

        Optional<Course> result = courseService.get(1L);

        assertThat(result).contains(course);
        then(repository).should().findById(1L);
    }

    @Test
    void updateNameShouldUpdateAndSave() {
        given(repository.findById(1L)).willReturn(Optional.of(course));
        given(repository.save(any(Course.class))).willAnswer(invocation -> invocation.getArgument(0));

        Optional<Course> result = courseService.updateName(1L, "Physics");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Physics");
        then(repository).should().save(course);
    }

    @Test
    void updateNameShouldReturnEmptyWhenNotFound() {
        given(repository.findById(1L)).willReturn(Optional.empty());

        Optional<Course> result = courseService.updateName(1L, "Physics");

        assertThat(result).isEmpty();
        then(repository).should(never()).save(any());
    }

    @Test
    void deleteShouldReturnTrueWhenFound() {
        given(repository.findById(1L)).willReturn(Optional.of(course));

        boolean result = courseService.delete(1L);

        assertThat(result).isTrue();
        then(repository).should().delete(course);
    }
}