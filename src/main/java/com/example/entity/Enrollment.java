package com.example.entity;

import jakarta.persistence.*;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Enrollment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "course_id", nullable = false)
  private Course course;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant enrolledAt;

  public Enrollment() {}

  public Enrollment(Student student, Course course) {
    this.student = student;
    this.course = course;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public Student getStudent() {
    return student;
  }

  public Course getCourse() {
    return course;
  }

  public Instant getEnrolledAt() {
    return enrolledAt;
  }
}
