package com.example.devso.batch.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class CleanupScheduler {

    private final JobLauncher jobLauncher;
    private final Job cleanupDeletedRowsJob;

    @Value("${cleanup.execution-date}")
    private String executionDate;

    public CleanupScheduler(JobLauncher jobLauncher, Job cleanupDeletedRowsJob) {
        this.jobLauncher = jobLauncher;
        this.cleanupDeletedRowsJob = cleanupDeletedRowsJob;
    }

    // Run every day at midnight to check if it's the target date
    @Scheduled(cron = "0 29 10 * * *", zone = "Asia/Seoul")
    public void runCleanupJob() {
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Seoul"));
        LocalDate targetDate = LocalDate.parse(executionDate, DateTimeFormatter.ISO_LOCAL_DATE);

        if (today.isEqual(targetDate)) {
            try {
                JobParameters jobParameters = new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters();
                jobLauncher.run(cleanupDeletedRowsJob, jobParameters);
                System.out.println("Cleanup job executed for date: " + today);
            } catch (Exception e) {
                System.err.println("Failed to execute cleanup job: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
