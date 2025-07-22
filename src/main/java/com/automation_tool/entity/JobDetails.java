package com.automation_tool.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobdetails")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String jobName;
    private String jobGroup;
    private String jobClassName;
    private String triggerType;
    private String cronExpression;
    private LocalDateTime startTime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Column(columnDefinition = "TEXT")
    private String jobDataJson;


}
