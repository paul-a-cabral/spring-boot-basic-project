package com.example.core.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobStatusListener implements JobExecutionListener {

  private static final Logger log = LoggerFactory.getLogger(JobStatusListener.class);

  @Override
  public void beforeJob(JobExecution jobExecution) {
    // Transition status from STARTING to STARTED / IN_PROGRESS as the background
    // worker picks it up
    jobExecution.setStatus(BatchStatus.STARTED);
    log.info(">>> Job {} (ID: {}) transition: Status is now IN_PROGRESS / STARTED <<<",
        jobExecution.getJobInstance().getJobName(), jobExecution.getId());
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
      log.info(">>> Job {} (ID: {}) finished successfully: SUCCESS / COMPLETED <<<",
          jobExecution.getJobInstance().getJobName(), jobExecution.getId());
    } else {
      log.error(">>> Job {} (ID: {}) finished with failure: FAILED <<<",
          jobExecution.getJobInstance().getJobName(), jobExecution.getId());
    }
  }
}