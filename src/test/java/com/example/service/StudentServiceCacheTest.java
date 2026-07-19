package com.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.example.config.StudentEmailProperties;
import com.example.entity.Student;
import com.example.repository.StudentRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(classes = StudentServiceCacheTest.CacheTestConfig.class)
class StudentServiceCacheTest {

  @Configuration
  @EnableCaching
  @Import({StudentService.class, CacheManagerConfig.class})
  static class CacheTestConfig {}

  @Configuration
  static class CacheManagerConfig {

    @Bean
    CacheManager cacheManager() {
      return new ConcurrentMapCacheManager("students", "studentById");
    }
  }

  @Autowired private StudentService studentService;

  @Autowired private CacheManager cacheManager;

  @MockitoBean private StudentRepository repository;

  @MockitoBean private StudentEmailProperties emailProperties;

  @BeforeEach
  void setUp() {
    given(emailProperties.getDomain()).willReturn("example.com");
    cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
  }

  @Test
  void getCachesStudentByIdAfterFirstLookup() {
    Student student = new Student(1L, "Jane Doe", "jane.doe@example.com");
    given(repository.findById(1L)).willReturn(Optional.of(student));

    assertThat(studentService.get(1L)).contains(student);
    assertThat(studentService.get(1L)).contains(student);

    then(repository).should(times(1)).findById(1L);
  }

  @Test
  void updateRefreshesStudentCacheAndInvalidatesStudentList() {
    Student original = new Student(1L, "Jane Doe", "jane.doe@example.com");
    Student updated = new Student(1L, "John Doe", "john.doe@example.com");

    given(repository.findAll()).willReturn(List.of(original)).willReturn(List.of(updated));
    given(repository.findById(1L)).willReturn(Optional.of(original));
    given(repository.save(any(Student.class))).willAnswer(invocation -> invocation.getArgument(0));

    assertThat(studentService.list()).extracting(Student::getName).containsExactly("Jane Doe");
    assertThat(studentService.list()).extracting(Student::getName).containsExactly("Jane Doe");

    assertThat(studentService.updateName(1L, "John Doe")).isPresent();
    assertThat(studentService.get(1L)).map(Student::getName).contains("John Doe");
    assertThat(studentService.list()).extracting(Student::getName).containsExactly("John Doe");

    then(repository).should(times(2)).findAll();
    then(repository).should(times(1)).findById(1L);
  }
}
