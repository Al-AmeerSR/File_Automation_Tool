package com.automation_tool.jobs;

import com.automation_tool.config.ApplicationContextProvider;
import com.automation_tool.service.BatchJobExecutorService;
import com.automation_tool.service.FileService;
import com.automation_tool.utils.JobUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class FileCleanUpJob implements Job {

    private FileService fileService;
    private BatchJobExecutorService batchJobExecutor;

    @Autowired
    public FileCleanUpJob(FileService fileService, BatchJobExecutorService batchJobExecutor) {
        this.fileService = fileService;
        this.batchJobExecutor = batchJobExecutor;
    }

    public FileCleanUpJob() {

    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (fileService == null || batchJobExecutor == null) {
            var ctx = ApplicationContextProvider.getApplicationContext();
            fileService = ctx.getBean(FileService.class);
            batchJobExecutor = ctx.getBean(BatchJobExecutorService.class);
        }

        JobDataMap dataMap = context.getMergedJobDataMap();
        String rootDir = (String) dataMap.get("rootDir");

        if (JobUtils.shouldUseSpringBatch(rootDir)) {
            try {
                batchJobExecutor.runCleanupJob(dataMap);
            } catch (Exception e) {
                throw new JobExecutionException("Spring Batch job execution failed", e);
            }
        } else {
            fileService.cleanupFiles(dataMap);
        }
    }

}
