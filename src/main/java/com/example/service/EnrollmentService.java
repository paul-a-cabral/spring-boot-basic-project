package com.example.service;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EnrollmentService {
    // Maps Course ID to a Set of Student IDs
    private final Map<Long, Set<Long>> courseToStudents = new ConcurrentHashMap<>();
    // Maps Student ID to a Set of Course IDs (Internal index for lookup)
    private final Map<Long, Set<Long>> studentToCourses = new ConcurrentHashMap<>();

    public void enroll(Long courseId, Long studentId) {
        courseToStudents.computeIfAbsent(courseId, k -> ConcurrentHashMap.newKeySet()).add(studentId);
        studentToCourses.computeIfAbsent(studentId, k -> ConcurrentHashMap.newKeySet()).add(courseId);
    }

    public Set<Long> getStudentIdsInCourse(Long courseId) {
        return courseToStudents.getOrDefault(courseId, Collections.emptySet());
    }

    public Set<Long> getCourseIdsForStudent(Long studentId) {
        return studentToCourses.getOrDefault(studentId, Collections.emptySet());
    }
}