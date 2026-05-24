package com.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.entity.Student;
import com.example.service.StudentService;

@Component
public class ScheduledStudentCreator {

    private static final Logger log = LoggerFactory.getLogger(ScheduledStudentCreator.class);

    private final StudentService studentService;
    private final String emailDomain;

    public ScheduledStudentCreator(StudentService studentService,
                                   @Value("${app.students.email.domain:example.com}") String emailDomain) {
        this.studentService = studentService;
        this.emailDomain = emailDomain;
    }

    // runs every 2 minutes
    @Scheduled(fixedRate = 120000)
    public void createRandomStudent() {
        try {
            String name = StudentGenerator.generateName();
            String email = StudentGenerator.generateEmailFromName(name, emailDomain);

            Student student = new Student(null, name, email);
            Student saved = studentService.create(student);
            log.info("Created student {} with id {}", saved.getName(), saved.getId());
        } catch (Exception e) {
            log.error("Failed to create scheduled student", e);
        }
    }
}
