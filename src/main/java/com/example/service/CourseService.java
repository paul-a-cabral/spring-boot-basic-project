package com.example.service;

import java.util.List;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import com.example.entity.Course;
import com.example.repository.CourseRepository;

@Service
public class CourseService {

    private final CourseRepository repository;

    public CourseService(CourseRepository repository) {
        this.repository = repository;
    }

    public Course create(@NonNull Course course) {
        return repository.save(course);
    }

    public List<Course> list() {
        return repository.findAll();
    }

    public Optional<Course> get(@NonNull Long id) {
        return repository.findById(id);
    }

    public Optional<Course> updateName(@NonNull Long id, String name) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setName(name);
                    return repository.save(existing);
                });
    }

    public boolean delete(@NonNull Long id) {
        return repository.findById(id)
                .map(existing -> {
                    repository.delete(existing);
                    return true;
                })
                .orElse(false);
    }
}