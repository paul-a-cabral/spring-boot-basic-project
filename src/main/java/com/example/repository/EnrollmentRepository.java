package com.example.repository;

import com.example.entity.Enrollment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
  List<Enrollment> findByCourseId(Long courseId);

  List<Enrollment> findByStudentId(Long studentId);

  boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
}
