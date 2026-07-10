package com.example.repository;

import com.example.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByCourseId(Long courseId);
    List<Enrollment> findByStudentId(Long studentId);
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
}