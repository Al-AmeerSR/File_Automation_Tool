package com.automation_tool.config.reader;

import com.automation_tool.dto.FileCheckResult;
import com.automation_tool.utils.FileUtils;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

public class FileOrganizeItemReader implements ItemReader<FileCheckResult> {


    private final String rootDir;

    public FileOrganizeItemReader(String rootDir) {
        this.rootDir = rootDir;
    }

    private final Queue<FileCheckResult> fileQueue = new LinkedList<>();
    private boolean initialized = false;

    @Override
    public FileCheckResult read() {
        if (!initialized) {
            File root = new File(rootDir);
            FileUtils.walkFiles(root, file -> {
                fileQueue.add(new FileCheckResult(file, false, false, false)); // flags are unused here
            });
            initialized = true;
        }

        return fileQueue.poll();
    }
}

