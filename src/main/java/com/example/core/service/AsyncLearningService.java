package com.example.core.service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AsyncLearningService {

  private static final Logger logger = LoggerFactory.getLogger(AsyncLearningService.class);
  private final Map<String, String> jobStatus = new ConcurrentHashMap<>();

  public String startReportJob(String requestedBy) {
    String jobId = UUID.randomUUID().toString();
    jobStatus.put(jobId, "IN_PROGRESS");
    runReportJob(jobId, requestedBy);
    return jobId;
  }

  public String getStatus(String jobId) {
    return jobStatus.getOrDefault(jobId, "NOT_FOUND");
  }

  @Async("taskExecutor")
  public CompletableFuture<Void> runReportJob(String jobId, String requestedBy) {
    logger.info(
        "Started async report job {} for user {} on thread {}",
        jobId,
        requestedBy,
        Thread.currentThread().getName());

    try {
      Thread.sleep(4000);
      jobStatus.put(jobId, "COMPLETED");
      logger.info(
          "Completed async report job {} on thread {}", jobId, Thread.currentThread().getName());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      jobStatus.put(jobId, "FAILED");
      logger.warn("Async report job {} interrupted", jobId, e);
    } catch (Exception e) {
      jobStatus.put(jobId, "FAILED");
      logger.error("Async report job {} failed", jobId, e);
    }

    return CompletableFuture.completedFuture(null);
  }
}
