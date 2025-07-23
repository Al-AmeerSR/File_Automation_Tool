package com.automation_tool.utils;

import com.automation_tool.dto.JobRequestDTO;
import com.automation_tool.exception.DuplicateJobNameException;
import com.automation_tool.exception.InValidDirectoryException;
import com.automation_tool.exception.InValidJobStartTimeException;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class JobRequestValidationUtils {

    private final  Scheduler scheduler;
    public JobRequestValidationUtils(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void validate(JobRequestDTO request) throws SchedulerException {
        // 1. Validate job name duplication
        JobKey jobKey = new JobKey(request.getJobName(), request.getJobGroup());
        if (scheduler.checkExists(jobKey)) {
            throw new DuplicateJobNameException("Job with name '" + request.getJobName() + "' already exists.");
        }

        // 2. Validate if job class exists and implements Job
        try {
            Class<?> clazz = Class.forName("com.automation_tool.jobs." + request.getJobClassName());
            if (!Job.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Class " + clazz.getName() + " does not implement org.quartz.Job");
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Job class not found: " + request.getJobClassName());
        }

        // 3. Validate trigger type value
        if (!List.of("CRON", "ONETIME").contains(request.getTriggerType().toUpperCase())) {
            throw new IllegalArgumentException("Invalid triggerType. Must be CRON or ONETIME.");
        }

        // 4. If triggerType is CRON, validate cron expression
        if ("CRON".equalsIgnoreCase(request.getTriggerType())) {
            if (request.getCronExpression() == null || request.getCronExpression().isBlank()) {
                throw new IllegalArgumentException("Cron expression is required for CRON trigger type.");
            }
            try {
                CronScheduleBuilder.cronSchedule(request.getCronExpression());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid cron expression: " + request.getCronExpression());
            }
        }

        // 5. Validate root directory
        if (request.getJobData() != null && request.getJobData().containsKey("rootDir")) {
            String rootDir = request.getJobData().get("rootDir").toString();
            File dir = new File(rootDir);
            if (!dir.exists() || !dir.isDirectory()) {
                throw new InValidDirectoryException("Invalid directory: " + rootDir);
            }
        }

        // 6. Validate that startTime is not in the past
        try {
            LocalDateTime startTime = LocalDateTime.parse(request.getStartTime(), DateTimeFormatter.ISO_DATE_TIME);
            if (startTime.isBefore(LocalDateTime.now())) {
                throw new InValidJobStartTimeException("Start time cannot be in the past.");
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format for startTime. Use ISO_DATE_TIME.");
        }
    }
}

