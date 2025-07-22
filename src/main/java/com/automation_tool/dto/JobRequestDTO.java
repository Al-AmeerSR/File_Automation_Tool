package com.automation_tool.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class JobRequestDTO {

    private String jobName;
    private String jobGroup;
    private String jobClassName;
    private String cronExpression;
    private String triggerType;
    private String startTime;
    @JsonProperty
    private Map<String, Object> jobData;
}
