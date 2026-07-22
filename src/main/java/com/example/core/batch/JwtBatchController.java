package com.example.core.batch;

import com.example.core.security.AuthenticationMode;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
@ConditionalOnProperty(
    prefix = "app.security",
    name = "authentication",
    havingValue = AuthenticationMode.JWT_VALUE)
public class JwtBatchController {

  private final JobLauncher jobLauncher;
  private final Job importEmployeeJob;
  private final JobExplorer jobExplorer;

  public JwtBatchController(
      @Qualifier("asyncJobLauncher") JobLauncher jobLauncher,
      Job importEmployeeJob,
      JobExplorer jobExplorer) {
    this.jobLauncher = jobLauncher;
    this.importEmployeeJob = importEmployeeJob;
    this.jobExplorer = jobExplorer;
  }

  @PostMapping("/import-employees-async")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> runEmployeeBatchImportAsync() throws Exception {

    JobParameters jobParameters =
        new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();

    JobExecution execution = jobLauncher.run(importEmployeeJob, jobParameters);

    // return initial job status immediately
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(
            Map.of(
                "jobId", execution.getJobId(),
                "status", execution.getStatus().toString(), // e.g., "STARTING" or "STARTED"
                "exitStatus",
                    execution
                        .getExitStatus()
                        .getExitCode(), // e.g., "UNKNOWN" (since it's still running)
                "message",
                    "Batch import started asynchronously. Use the job ID to poll execution status."));
  }

  @GetMapping("/status/{jobExecutionId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> getBatchStatus(@PathVariable Long jobExecutionId) {

    JobExecution jobExecution = jobExplorer.getJobExecution(jobExecutionId);

    if (jobExecution == null) {
      return ResponseEntity.notFound().build();
    }

    // Map internal Spring Batch statuses to user-friendly status names
    String customStatus =
        switch (jobExecution.getStatus()) {
          case STARTING -> "STARTING";
          case STARTED -> "IN_PROGRESS";
          case COMPLETED -> "SUCCESS";
          case FAILED, STOPPED -> "FAIL";
          default -> jobExecution.getStatus().toString();
        };

    return ResponseEntity.ok(
        Map.of(
            "jobId", jobExecution.getJobId(),
            "rawStatus", jobExecution.getStatus().toString(),
            "status", customStatus, // 👈 Returns "STARTING", "IN_PROGRESS", "SUCCESS", or "FAIL"
            "exitStatus", jobExecution.getExitStatus().getExitCode(),
            "startTime", String.valueOf(jobExecution.getStartTime()),
            "endTime", String.valueOf(jobExecution.getEndTime())));
  }
}
