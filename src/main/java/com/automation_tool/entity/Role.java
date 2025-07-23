package com.automation_tool.entity;
import lombok.Getter;
import java.util.Set;
@Getter
public enum Role {
    ADMIN(Set.of(Permissions.JOB_SCHEDULE,Permissions.JOB_DELETE,Permissions.JOB_PAUSE,Permissions.JOB_RESUME,Permissions.JOB_RESCHEDULE,Permissions.JOB_VIEW)),
    USER(Set.of(Permissions.JOB_PAUSE,Permissions.JOB_RESUME,Permissions.JOB_VIEW));

    final Set<Permissions> permissions;
     Role(Set<Permissions> permissions){
        this.permissions = permissions;
    }
}
