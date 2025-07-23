package com.automation_tool.controller;

import com.automation_tool.dto.JobRequestDTO;
import com.automation_tool.dto.PaginatedResponseDTO;
import com.automation_tool.entity.JobDetails;
import com.automation_tool.service.SchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;


@RestController
@RequestMapping("/api/v1/job")
public class JobController {

    private final SchedulerService schedulerService;

    public JobController(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createJob(@RequestBody JobRequestDTO request) {
        String result = schedulerService.createJob(request);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/update")
    public ResponseEntity<String> updateJob(@RequestBody JobRequestDTO request) {
        String result = schedulerService.updateJob(request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteJob(
            @RequestParam String jobName,
            @RequestParam String jobGroup) {
        String result = schedulerService.deleteJob(jobName, jobGroup);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/pause")
    public ResponseEntity<String> pauseJob(
            @RequestParam String jobName,
            @RequestParam String jobGroup) {
        String result = schedulerService.pauseJob(jobName, jobGroup);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/resume")
    public ResponseEntity<String> resumeJob(
            @RequestParam String jobName,
            @RequestParam String jobGroup) {
        String result = schedulerService.resumeJob(jobName, jobGroup);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
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
