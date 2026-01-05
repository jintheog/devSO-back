package com.example.devso.config;

import com.example.devso.batch.tasklet.SoftDeleteCleanupTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    @Bean
    public Job cleanupDeletedRowsJob(JobRepository jobRepository, Step cleanupStep) {
        return new JobBuilder("cleanupDeletedRowsJob", jobRepository)
                .start(cleanupStep)
                .build();
    }

    @Bean
    public Step cleanupStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, SoftDeleteCleanupTasklet tasklet) {
        return new StepBuilder("cleanupStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }
}
