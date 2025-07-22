package com.automation_tool.utils;

import com.automation_tool.constants.FAT_Constants;
import com.automation_tool.dto.JobRequestDTO;
import org.quartz.CronScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.io.File;
import java.util.Date;

public class JobUtils {

    public static Trigger buildTrigger(JobRequestDTO request, Date runAt) {
        String triggerName = request.getJobName() + "Trigger";
        String triggerGroup = request.getJobGroup();

        if (FAT_Constants.CRON_TRIGGER.equals(request.getTriggerType())) {
            return TriggerBuilder.newTrigger()
                    .withIdentity(triggerName, triggerGroup)
                    .startAt(runAt)
                    .withSchedule(CronScheduleBuilder.cronSchedule(request.getCronExpression()))
                    .build();

        } else if (FAT_Constants.ONETIME_TRIGGER.equals(request.getTriggerType())) {
            return TriggerBuilder.newTrigger()
                    .withIdentity(triggerName, triggerGroup)
                    .startAt(runAt)
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withRepeatCount(0))
                    .build();
        }

        throw new IllegalArgumentException("Invalid trigger type. Must be CRON or ONETIME.");
    }

    public static  boolean shouldUseSpringBatch(String rootDirPath) {
        File rootDir = new File(rootDirPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) return false;

        long fileCount = FileUtils.countFiles(rootDir);
        long totalSize = FileUtils.getTotalSize(rootDir);

        return fileCount > 10000 || totalSize > 10000L * 1024 * 1024; // e.g., 10GB
    }
}
