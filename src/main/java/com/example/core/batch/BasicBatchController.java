package com.example.core.batch;

import com.example.core.security.AuthenticationMode;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/batch")
@ConditionalOnProperty(
    prefix = "app.security",
    name = "authentication",
    havingValue = AuthenticationMode.BASIC_VALUE,
    matchIfMissing = true)
public class BasicBatchController {

  private final JobLauncher jobLauncher;
  private final Job importEmployeeJob;

  public BasicBatchController(JobLauncher jobLauncher, Job importEmployeeJob) {
    this.jobLauncher = jobLauncher;
    this.importEmployeeJob = importEmployeeJob;
  }

  @PostMapping("/import-employees")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> runEmployeeBatchImport() throws Exception {

    JobParameters jobParameters =
        new JobParametersBuilder().addLong("time", System.currentTimeMillis()).toJobParameters();

    JobExecution execution = jobLauncher.run(importEmployeeJob, jobParameters);

    return ResponseEntity.ok(
        Map.of(
            "jobId", execution.getJobId(),
            "status", execution.getStatus().toString(),
            "exitStatus", execution.getExitStatus().getExitCode()));
  }
}
