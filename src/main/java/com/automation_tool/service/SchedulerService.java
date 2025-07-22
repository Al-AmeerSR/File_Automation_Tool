package com.automation_tool.service;

import com.automation_tool.constants.FAT_Constants;
import com.automation_tool.dto.JobRequestDTO;
import com.automation_tool.dto.PaginatedResponseDTO;
import com.automation_tool.entity.JobDetails;
import com.automation_tool.utils.JobRequestValidationUtils;
import com.automation_tool.utils.JobUtils;
import jakarta.transaction.Transactional;
import org.quartz.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;



@Service
public class SchedulerService {


    private final Scheduler scheduler;
    private final JobDetailsService jobDetailsService;
    private final JobRequestValidationUtils jobRequestValidationUtils;

    public SchedulerService( Scheduler scheduler,
                             JobDetailsService jobDetailsService,
                             JobRequestValidationUtils jobRequestValidationUtils) {
        this.scheduler = scheduler;
        this.jobDetailsService = jobDetailsService;
        this.jobRequestValidationUtils = jobRequestValidationUtils;

    }

    public String createJob(JobRequestDTO request) {

        try {

            jobRequestValidationUtils.validate(request);
            // Dynamically load the job class
            Class<?> loadedClass = Class.forName("com.automation_tool.jobs." + request.getJobClassName());
            @SuppressWarnings("unchecked")
            Class<? extends Job> jobClass = (Class<? extends Job>) loadedClass;

            // Prepare JobDataMap
            JobDataMap jobDataMap = request.getJobData() != null
                    ? new JobDataMap(request.getJobData())
                    : new JobDataMap();

            // Build the job detail
            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                    .withIdentity(request.getJobName(), request.getJobGroup())
                    .usingJobData(jobDataMap)
                    .storeDurably()
                    .build();

            LocalDateTime dateTime = LocalDateTime.parse(request.getStartTime(), DateTimeFormatter.ISO_DATE_TIME);
            Date runAt = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());

            Trigger trigger;
            trigger = JobUtils.buildTrigger(request, runAt);

            scheduler.scheduleJob(jobDetail, trigger);
            jobDetailsService.saveJobDetails(request);
            return "Job created";

        }catch(Exception e){

            return "Error creating job : "+e.getMessage();
        }

    }
    @Transactional
    public String deleteJob(String jobName, String jobGroup) {
        try{

            JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
            scheduler.deleteJob(jobKey);
            jobDetailsService.deleteJobDetails(jobName,jobGroup);
            return "Job deleted";

        }catch(Exception e){
            return "Error deleting job : "+e.getMessage();
        }
    }

    public String updateJob(JobRequestDTO request) {

        try{
            jobRequestValidationUtils.validate(request);
            LocalDateTime dateTime = LocalDateTime.parse(request.getStartTime(), DateTimeFormatter.ISO_DATE_TIME);
            Date runAt = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());

            TriggerKey triggerKey = TriggerKey.triggerKey(request.getJobName(), request.getJobGroup());
            Trigger newTrigger;
            newTrigger = JobUtils.buildTrigger(request, runAt);

            scheduler.rescheduleJob(triggerKey, newTrigger);
            jobDetailsService.updateJobDetails(request);
            return "Job updated";

        }catch(Exception e){

            return "Error updating job : "+e.getMessage();
        }

    }

    public PaginatedResponseDTO<JobDetails> getJobs(HashMap<String, String> filters) {
        return jobDetailsService.getJobs(filters);
    }


    public String pauseJob(String jobName, String jobGroup) {

        try{
            scheduler.pauseJob(JobKey.jobKey(jobName, jobGroup));
            jobDetailsService.updateJobStatus(jobName,jobGroup,FAT_Constants.JOB_STATUS_PAUSED);
            return "Job paused";
        }catch(Exception e){
            return "Error pausing job : "+e.getMessage();
        }
    }

    public String resumeJob(String jobName, String jobGroup) {

        try{
            scheduler.resumeJob(JobKey.jobKey(jobName, jobGroup));
            jobDetailsService.updateJobStatus(jobName,jobGroup,FAT_Constants.JOB_STATUS_RESUMED);
            return "Job resumed";
        }catch(Exception e){
            return "Error resuming job : "+e.getMessage();
        }
    }

}
