package com.automation_tool.checker;

import java.io.File;

public interface FileCorruptionChecker {
    boolean isCorrupted(File file) throws Exception;
}
