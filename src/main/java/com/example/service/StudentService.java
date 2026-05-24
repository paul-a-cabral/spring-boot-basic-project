package com.example.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.example.entity.Student;
import com.example.repository.StudentRepository;
import com.example.util.StudentGenerator;

@Service
public class StudentService {

    private final StudentRepository repository;
    private final String emailDomain;

    public StudentService(StudentRepository repository,
            @Value("${app.students.email.domain:example.com}") String emailDomain) {
        this.repository = repository;
        this.emailDomain = emailDomain;
    }

    public Student create(@NonNull Student student) {
        return repository.save(student);
    }

    public List<Student> list() {
        return repository.findAll();
    }

    public Optional<Student> get(@NonNull Long id) {
        return repository.findById(id);
    }

    public Optional<Student> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Optional<Student> updateName(@NonNull Long id, String name) {
        return repository.findById(id)
                .map(existing -> {
                    String email = StudentGenerator.generateEmailFromName(name, emailDomain);
                    existing.setName(name);
                    existing.setEmail(email != null ? email : name.toLowerCase().replace(" ", "."));
                    return repository.save(existing);
                });
    }

    public boolean delete(@NonNull Long id) {
        return repository.findById(id)
                .map((@NonNull var existing) -> {
                    repository.delete(existing);
                    return true;
                })
                .orElse(false);
    }
}
