package com.example.service;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CourseCacheWarmupService {

  private static final Logger logger = LoggerFactory.getLogger(CourseCacheWarmupService.class);

  private final CourseService courseService;

  public CourseCacheWarmupService(CourseService courseService) {
    this.courseService = courseService;
  }

  @Async
  public CompletableFuture<Void> warmCourseCache() {
    logger.info("Warming course cache on thread {}", Thread.currentThread().getName());
    courseService.list();
    return CompletableFuture.completedFuture(null);
  }
}
