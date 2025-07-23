package com.automation_tool.listener;

import com.automation_tool.constants.FAT_Constants;
import com.automation_tool.repository.JobDetailsRepository;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class JobStatusListener implements JobListener {

    private final JobDetailsRepository jobDetailsRepository;
    private final Logger logger = LoggerFactory.getLogger(JobStatusListener.class);

    public JobStatusListener(JobDetailsRepository jobDetailsRepository) {
        this.jobDetailsRepository = jobDetailsRepository;
    }

    @Override
    public String getName() {
        return "jobStatusListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        String jobName = context.getJobDetail().getKey().getName();
        String jobGroup = context.getJobDetail().getKey().getGroup();
        logger.info("JobName: {} JobGroup: {}", jobName, jobGroup);
        jobDetailsRepository.findByJobNameAndJobGroup(jobName, jobGroup).ifPresent(job -> {
            job.setStatus(FAT_Constants.JOB_STATUS_RUNNING);
            job.setUpdatedAt(LocalDateTime.now());
            jobDetailsRepository.save(job);
        });
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {}

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        String jobName = context.getJobDetail().getKey().getName();
        String jobGroup = context.getJobDetail().getKey().getGroup();
        logger.info("JobName after: {} JobGroup after: {}", jobName, jobGroup);
        jobDetailsRepository.findByJobNameAndJobGroup(jobName, jobGroup).ifPresent(job -> {
            job.setStatus(FAT_Constants.JOB_STATUS_FINISHED);
            job.setUpdatedAt(LocalDateTime.now());
            jobDetailsRepository.save(job);
        });
    }
}

