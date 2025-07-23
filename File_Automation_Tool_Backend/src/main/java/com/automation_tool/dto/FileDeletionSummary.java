package com.automation_tool.dto;

import java.util.List;

public record FileDeletionSummary(List<List<String>> deletedFiles,
                                  int oldFileDeleted,
                                  int corruptFileDeleted,
                                  int duplicateFileDeleted) {
}
