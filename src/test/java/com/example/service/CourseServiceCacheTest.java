package com.example.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.example.entity.Course;
import com.example.repository.CourseRepository;
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

@SpringBootTest(classes = CourseServiceCacheTest.CacheTestConfig.class)
class CourseServiceCacheTest {

  @Configuration
  @EnableCaching
  @Import({CourseService.class, CacheManagerConfig.class})
  static class CacheTestConfig {}

  @Configuration
  static class CacheManagerConfig {

    @Bean
    CacheManager cacheManager() {
      return new ConcurrentMapCacheManager("courses", "courseById");
    }
  }

  @Autowired private CourseService courseService;

  @Autowired private CacheManager cacheManager;

  @MockitoBean private CourseRepository repository;

  @BeforeEach
  void clearCaches() {
    cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
  }

  @Test
  void getCachesCourseByIdAfterFirstLookup() {
    Course course = new Course(1L, "Mathematics");
    given(repository.findById(1L)).willReturn(Optional.of(course));

    assertThat(courseService.get(1L)).contains(course);
    assertThat(courseService.get(1L)).contains(course);

    then(repository).should(times(1)).findById(1L);
  }

  @Test
  void updateRefreshesCourseCacheAndInvalidatesCourseList() {
    Course original = new Course(1L, "Mathematics");
    Course updated = new Course(1L, "Physics");

    given(repository.findAll()).willReturn(List.of(original)).willReturn(List.of(updated));
    given(repository.findById(1L)).willReturn(Optional.of(original));
    given(repository.save(any(Course.class))).willAnswer(invocation -> invocation.getArgument(0));

    assertThat(courseService.list()).extracting(Course::getName).containsExactly("Mathematics");
    assertThat(courseService.list()).extracting(Course::getName).containsExactly("Mathematics");

    assertThat(courseService.updateName(1L, "Physics")).isPresent();
    assertThat(courseService.get(1L)).map(Course::getName).contains("Physics");
    assertThat(courseService.list()).extracting(Course::getName).containsExactly("Physics");

    then(repository).should(times(2)).findAll();
    then(repository).should(times(1)).findById(1L);
  }
}
