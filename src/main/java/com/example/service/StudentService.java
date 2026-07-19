package com.example.service;

import com.example.config.StudentEmailProperties;
import com.example.entity.Student;
import com.example.repository.StudentRepository;
import com.example.util.StudentGenerator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

  private final StudentRepository repository;

  private String emailDomain;

  public StudentService(StudentRepository repository, StudentEmailProperties emailProperties) {
    this.repository = repository;
    this.emailDomain = emailProperties.getDomain();
  }

  @Caching(
      put = @CachePut(cacheNames = "studentById", key = "#result.id"),
      evict = @CacheEvict(cacheNames = "students", key = "'all'"))
  public Student create(@NonNull Student student) {
    return repository.save(student);
  }

  @Cacheable(cacheNames = "students", key = "'all'")
  public List<Student> list() {
    return repository.findAll();
  }

  @Cacheable(cacheNames = "studentById", key = "#id", unless = "#result == null")
  public Optional<Student> get(@NonNull Long id) {
    return repository.findById(id);
  }

  public Optional<Student> findByEmail(String email) {
    return repository.findByEmail(email);
  }

  @Caching(
      put = @CachePut(cacheNames = "studentById", key = "#id", unless = "#result == null"),
      evict = @CacheEvict(cacheNames = "students", key = "'all'"))
  public Optional<Student> updateName(@NonNull Long id, String name) {
    return repository
        .findById(id)
        .map(
            existing -> {
              String email = StudentGenerator.generateEmailFromName(name, emailDomain);
              existing.setName(name);
              existing.setEmail(email != null ? email : name.toLowerCase().replace(" ", "."));
              return repository.save(existing);
            });
  }

  @Caching(
      evict = {
        @CacheEvict(cacheNames = "studentById", key = "#id", beforeInvocation = true),
        @CacheEvict(cacheNames = "students", key = "'all'", beforeInvocation = true)
      })
  public boolean delete(@NonNull Long id) {
    return repository
        .findById(id)
        .map(
            existing -> {
              repository.delete(Objects.requireNonNull(existing));
              return true;
            })
        .orElse(false);
  }
}
