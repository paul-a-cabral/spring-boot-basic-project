package com.example;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public Student create(Student student) {
        return repository.save(student);
    }

    public List<Student> list() {
        return repository.findAll();
    }

    public Optional<Student> get(Long id) {
        return repository.findById(id);
    }

    public Optional<Student> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Optional<Student> update(Long id, Student student) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setName(student.getName());
                    existing.setEmail(student.getEmail());
                    return repository.save(existing);
                });
    }

    public boolean delete(Long id) {
        return repository.findById(id)
                .map(existing -> {
                    repository.delete(existing);
                    return true;
                })
                .orElse(false);
    }
}
