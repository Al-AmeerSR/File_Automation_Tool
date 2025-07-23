package com.automation_tool.service;

import org.quartz.JobDataMap;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import static com.automation_tool.utils.FileUtils.countFiles;

@Component
public class BatchJobExecutorService {

    private final JobLauncher jobLauncher;
    private final Job batchCleanupJob;
    private final Job compressAndBackupJob;
    private final Job organizeBatchJob;

    @Autowired
    public BatchJobExecutorService(
            JobLauncher jobLauncher,
            @Qualifier("fileCleanupBatchJob") Job batchCleanupJob,
            @Qualifier("fileCompressAndBackupBatchJob") Job compressAndBackupJob,
            @Qualifier("fileOrganizeBatchJob") Job organizeBatchJob
    ) {
        this.jobLauncher = jobLauncher;
        this.batchCleanupJob = batchCleanupJob;
        this.compressAndBackupJob = compressAndBackupJob;
        this.organizeBatchJob = organizeBatchJob;
    }


    public void runCleanupJob(JobDataMap dataMap) throws Exception {
        jobLauncher.run(batchCleanupJob, buildJobParameters(dataMap));
    }

    public void runCompressAndBackupJob(JobDataMap dataMap) throws Exception {
        jobLauncher.run(compressAndBackupJob, buildJobParameters(dataMap));
    }

    public void runOrganizeJob(JobDataMap dataMap) throws Exception {
        jobLauncher.run(organizeBatchJob, buildJobParameters(dataMap));
    }

    private JobParameters buildJobParameters(JobDataMap dataMap) {
        JobParametersBuilder builder = new JobParametersBuilder();

        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();

            switch (value) {
                case String s -> builder.addString(key, s);
                case Long l -> builder.addLong(key, l);
                case Integer i -> builder.addLong(key, i.longValue()); // Convert to long
                case Double v -> builder.addDouble(key, v);
                case java.util.Date date -> builder.addDate(key, date);
                case null, default ->
                    // fallback to string
                        builder.addString(key, Objects.requireNonNull(value).toString());
            }
        }

        // Ensure uniqueness
        builder.addLong("timestamp", System.currentTimeMillis());

        return builder.toJobParameters();
    }
}
