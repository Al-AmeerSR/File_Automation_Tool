package com.automation_tool.dto;

import java.io.File;

public record FileCheckResult (File file,
                               boolean isOld,
                               boolean isCorrupted,
                               boolean isDuplicate){


}

