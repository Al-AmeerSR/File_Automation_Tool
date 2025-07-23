package com.automation_tool.repository;

import com.automation_tool.entity.JobDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface JobDetailsRepository extends JpaRepository<JobDetails, Long>, JpaSpecificationExecutor<JobDetails> {
    Optional<JobDetails> findByJobNameAndJobGroup(String jobName, String jobGroup);
    void deleteByJobNameAndJobGroup(String jobName, String jobGroup);

}
