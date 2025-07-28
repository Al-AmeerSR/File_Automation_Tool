package com.automation_tool.controller;

import com.automation_tool.dto.JobRequestDTO;
import com.automation_tool.dto.PaginatedResponseDTO;
import com.automation_tool.entity.JobDetails;
import com.automation_tool.service.SchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;


@RestController
@RequestMapping("/api/v1/job")
@Tag(name = "Job Handling", description = "APIs for handling Job Scheduling")
public class JobController {

    private final SchedulerService schedulerService;

    public JobController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    @Operation(summary = "Creates a scheduled job", description = "create a job with scheduled time to run")
    public ResponseEntity<String> createJob(@RequestBody JobRequestDTO request) {
        String result = schedulerService.createJob(request);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/update")
    @Operation(summary = "Rescheduling the job", description = "Changes the running time of a job")
    public ResponseEntity<String> updateJob(@RequestBody JobRequestDTO request) {
        String result = schedulerService.updateJob(request);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete")
    @Operation(summary = "Deletes the job", description = "deleting the job entry from db")
    public ResponseEntity<String> deleteJob(
            @RequestParam String jobName,
            @RequestParam String jobGroup) {
        String result = schedulerService.deleteJob(jobName, jobGroup);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/pause")
    @Operation(summary = "Pauses the job", description = "Pauses the active job")
    public ResponseEntity<String> pauseJob(
            @RequestParam String jobName,
            @RequestParam String jobGroup) {
        String result = schedulerService.pauseJob(jobName, jobGroup);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/resume")
    @Operation(summary = "Resuming the job", description = "Resumes the paused job")
    public ResponseEntity<String> resumeJob(
            @RequestParam String jobName,
            @RequestParam String jobGroup) {
        String result = schedulerService.resumeJob(jobName, jobGroup);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
    @Operation(summary = "Returns the list of jobs", description = "Returns the list of jobs with the filter criteria specified")
    public ResponseEntity<PaginatedResponseDTO<JobDetails>> getJobs(@RequestParam (required = false)String jobGroup ,
                                                                          @RequestParam (required = false)String jobName,
                                                                          @RequestParam (required = false)String status,
                                                                          @RequestParam (required = false)String triggerType,
                                                                          @RequestParam (required = false, defaultValue ="0")String pageNumber,
                                                                          @RequestParam (required = false,defaultValue = "10")String pageSize
    ) {
        HashMap<String, String> map = new HashMap<>();
        map.put("jobGroup",jobGroup);
        map.put("jobName",jobName);
        map.put("status",status);
        map.put("triggerType",triggerType);
        map.put("pageNumber",pageNumber);
        map.put("pageSize",pageSize);
        PaginatedResponseDTO<JobDetails> result = schedulerService.getJobs(map);
        return ResponseEntity.ok(result);
    }
}
