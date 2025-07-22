package com.automation_tool.service;

import com.automation_tool.constants.FAT_Constants;
import com.automation_tool.dto.JobRequestDTO;
import com.automation_tool.dto.PaginatedResponseDTO;
import com.automation_tool.entity.JobDetails;
import com.automation_tool.repository.JobDetailsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class JobDetailsService {

    JobDetailsRepository jobDetailsRepository;
    public JobDetailsService(JobDetailsRepository jobDetailsRepository) {
        this.jobDetailsRepository = jobDetailsRepository;
    }
    public void saveJobDetails(JobRequestDTO request) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        String jobDataJson = objectMapper.writeValueAsString(request.getJobData());

        JobDetails jobDetails =  JobDetails.builder()
                .jobName(request.getJobName())
                .jobGroup(request.getJobGroup())
                .jobClassName(request.getJobClassName())
                .triggerType(request.getTriggerType())
                .cronExpression(request.getCronExpression())
                .startTime(LocalDateTime.parse(request.getStartTime()))
                .status(FAT_Constants.JOB_STATUS_CREATED)
                .createdAt(LocalDateTime.now())
                .jobDataJson(jobDataJson)
                .build();
        jobDetailsRepository.save(jobDetails);
    }

    public void deleteJobDetails(String jobName, String jobGroup) {

        jobDetailsRepository.deleteByJobNameAndJobGroup(jobName,jobGroup);
    }

    public void updateJobDetails(JobRequestDTO request) {

        Optional<JobDetails> jobDetails = jobDetailsRepository.findByJobNameAndJobGroup(request.getJobName(),request.getJobGroup());
        jobDetails.ifPresent(details->{
            details.setStartTime(LocalDateTime.parse(request.getStartTime()));
            details.setStatus(FAT_Constants.JOB_STATUS_UPDATED);
            details.setUpdatedAt(LocalDateTime.now());
            details.setCronExpression(request.getCronExpression());
            jobDetailsRepository.save(details);
        });
    }

    public void updateJobStatus(String jobName, String jobGroup, String status) {

        jobDetailsRepository.findByJobNameAndJobGroup(jobName,jobGroup).ifPresent(details->{
            details.setStatus(status);
            details.setUpdatedAt(LocalDateTime.now());
            jobDetailsRepository.save(details);
        });
    }

    public PaginatedResponseDTO<JobDetails> getJobs(HashMap<String, String> filters) {

        int pageNumber = Integer.parseInt(filters.getOrDefault("pageNumber", "0"));
        int pageSize = Integer.parseInt(filters.getOrDefault("pageSize", "10"));
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Specification<JobDetails> spec = buildJobDetailsSpecification(filters);
        Page<JobDetails> jobPage = jobDetailsRepository.findAll(spec, pageable);

        return new PaginatedResponseDTO<>(
                jobPage.getContent(),
                jobPage.getNumber(),
                jobPage.getSize(),
                jobPage.getTotalElements(),
                jobPage.getTotalPages(),
                jobPage.isLast()
        );
    }

    private Specification<JobDetails> buildJobDetailsSpecification(HashMap<String, String> filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.get("jobGroup") != null) {
                predicates.add(cb.equal(root.get("jobGroup"), filters.get("jobGroup")));
            }
            if (filters.get("jobName") != null) {
                predicates.add(cb.equal(root.get("jobName"), filters.get("jobName")));
            }
            if (filters.get("status") != null) {
                predicates.add(cb.equal(root.get("status"), filters.get("status")));
            }
            if (filters.get("triggerType") != null) {
                predicates.add(cb.equal(root.get("triggerType"), filters.get("triggerType")));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


}
