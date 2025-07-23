package com.automation_tool.config;

import com.automation_tool.config.processor.FileCleanUpItemProcessor;
import com.automation_tool.config.processor.FileOrganizeItemProcessor;
import com.automation_tool.config.reader.FileCleanUpItemReader;
import com.automation_tool.config.reader.FileCompressAndBackUpItemReader;
import com.automation_tool.config.reader.FileOrganizeItemReader;
import com.automation_tool.config.writer.FileCleanUpItemWriter;

import com.automation_tool.config.writer.FileCompressAndBackUpItemWriter;
import com.automation_tool.config.writer.FileOrganizeItemWriter;
import com.automation_tool.dto.FileCheckResult;
import com.automation_tool.service.FileService;
import com.automation_tool.service.ReportService;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;

@Configuration
public class BatchConfig {

    private final ReportService reportService;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final FileService fileService;
    public BatchConfig(ReportService reportService,
                       JobRepository jobRepository,
                       PlatformTransactionManager transactionManager,
                       FileService fileService) {
        this.reportService = reportService;
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.fileService = fileService;
    }

    // ========== FILE CLEANUP JOB ==========
    @Bean
    public Job fileCleanupBatchJob() {
        return new JobBuilder("fileCleanupBatchJob", jobRepository)
                .start(cleanupStep())
                .build();
    }

    @Bean
    public Step cleanupStep() {
        return new StepBuilder("cleanupStep", jobRepository)
                .<FileCheckResult, FileCheckResult>chunk(100, transactionManager)
                .reader(fileCleanUpReader(null,null))
                .processor(fileCleanUpProcessor())
                .writer(fileCleanUpWriter(null))
                .listener(fileCleanUpWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<FileCheckResult> fileCleanUpReader( @Value("#{jobParameters['rootDir']}") String rootDir,
                                                          @Value("#{jobParameters['fileAgeInDays']}") Long fileAgeInDays) {
        return new FileCleanUpItemReader(rootDir, fileAgeInDays);
    }

    @Bean
    public ItemProcessor<FileCheckResult, FileCheckResult> fileCleanUpProcessor() {
        return new FileCleanUpItemProcessor();
    }

    @Bean
    @StepScope
    public ItemWriter<FileCheckResult> fileCleanUpWriter(@Value("#{jobParameters['rootDir']}") String rootDir) {
        return new FileCleanUpItemWriter(reportService,rootDir);
    }

    // ========== FILE COMPRESS & BACKUP JOB ==========
    @Bean
    public Job fileCompressAndBackupBatchJob() {
        return new JobBuilder("fileCompressAndBackupBatchJob", jobRepository)
                .start(compressStep())
                .build();
    }

    @Bean
    public Step compressStep() {
        return new StepBuilder("compressStep", jobRepository)
                .<Path, Path>chunk(100, transactionManager)
                .reader(fileCompressReader(null,null))
                .writer(fileCompressWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Path> fileCompressReader(@Value("#{jobParameters['rootDir']}") String rootDir,
                                               @Value("#{jobParameters['fileExtensions']}") String fileExtensions) {
        return new FileCompressAndBackUpItemReader(rootDir,fileExtensions);
    }


    @Bean
    @StepScope
    public ItemWriter<Path> fileCompressWriter (@Value("#{jobParameters['rootDir']}") String rootDir) {
        return  new FileCompressAndBackUpItemWriter(reportService,rootDir);
    }

    // ========== FILE ORGANIZE JOB ==========
    @Bean
    public Job fileOrganizeBatchJob() {
        return new JobBuilder("fileOrganizeBatchJob", jobRepository)
                .start(organizeStep())
                .build();
    }

    @Bean
    public Step organizeStep() {
        return new StepBuilder("organizeStep", jobRepository)
                .<FileCheckResult, FileCheckResult>chunk(100, transactionManager)
                .reader(fileOrganizeReader(null))
                .processor(fileOrganizeProcessor())
                .writer(fileOrganizeWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<FileCheckResult> fileOrganizeReader(@Value("#{jobParameters['rootDir']}") String rootDir) {
        return new FileOrganizeItemReader(rootDir);
    }

    @Bean
    public ItemProcessor<FileCheckResult, FileCheckResult> fileOrganizeProcessor() {
        return new FileOrganizeItemProcessor();
    }

    @Bean
    @StepScope
    public ItemWriter<FileCheckResult> fileOrganizeWriter(@Value("#{jobParameters['rootDir']}") String rootDir) {
        return new FileOrganizeItemWriter(fileService,reportService,rootDir);
    }

}
