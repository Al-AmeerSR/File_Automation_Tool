package com.automation_tool.config;

import com.automation_tool.listener.JobStatusListener;
import jakarta.annotation.PostConstruct;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Configuration;
@Configuration
public class JobConfig {

    private final Scheduler scheduler;
    private final JobStatusListener jobStatusListener;


    public JobConfig(Scheduler scheduler, JobStatusListener jobStatusListener) {
        this.scheduler = scheduler;
        this.jobStatusListener = jobStatusListener;
    }

    @PostConstruct
    public void registerJobListener() throws SchedulerException {
        scheduler.getListenerManager().addJobListener(jobStatusListener);
    }
}





