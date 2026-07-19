package com.example.service;

import com.example.entity.Course;
import com.example.repository.CourseRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class CourseService {

  private final CourseRepository repository;

  public CourseService(CourseRepository repository) {
    this.repository = repository;
  }

  @Caching(
      put = @CachePut(cacheNames = "courseById", key = "#result.id"),
      evict = @CacheEvict(cacheNames = "courses", key = "'all'"))
  public Course create(@NonNull Course course) {
    return repository.save(course);
  }

  @Cacheable(cacheNames = "courses", key = "'all'")
  public List<Course> list() {
    return repository.findAll();
  }

  @Cacheable(cacheNames = "courseById", key = "#id", unless = "#result == null")
  public Optional<Course> get(@NonNull Long id) {
    return repository.findById(id);
  }

  @Caching(
      put = @CachePut(cacheNames = "courseById", key = "#id", unless = "#result == null"),
      evict = @CacheEvict(cacheNames = "courses", key = "'all'"))
  public Optional<Course> updateName(@NonNull Long id, String name) {
    return repository
        .findById(id)
        .map(
            existing -> {
              existing.setName(name);
              return repository.save(existing);
            });
  }

  @Caching(
      evict = {
        @CacheEvict(cacheNames = "courseById", key = "#id", beforeInvocation = true),
        @CacheEvict(cacheNames = "courses", key = "'all'", beforeInvocation = true)
      })
  public boolean delete(@NonNull Long id) {
    return repository
        .findById(id)
        .map(
            existing -> {
              repository.deleteById(id);
              return true;
            })
        .orElse(false);
  }
}
