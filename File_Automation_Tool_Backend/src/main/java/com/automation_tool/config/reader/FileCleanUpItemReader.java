package com.automation_tool.config.reader;

import com.automation_tool.dto.FileCheckResult;
import com.automation_tool.utils.FileUtils;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.*;


public class FileCleanUpItemReader implements ItemReader<FileCheckResult> {



    private final String rootDir;
    private final Long fileAgeInDays;
    private boolean initialized = false;

    private final Queue<FileCheckResult> queue = new LinkedList<>();

    public FileCleanUpItemReader(String rootDir, Long fileAgeInDays) {
        this.rootDir = rootDir;
        this.fileAgeInDays = fileAgeInDays;
    }

    @Override
    public FileCheckResult read() {
        if (!initialized) {
            File root = new File(rootDir);
            Map<String, File> hashMap = new HashMap<>();
            List<FileCheckResult> results = new ArrayList<>();

            FileUtils.walkFiles(root, file -> {
                boolean isOld = FileUtils.isOld(file, fileAgeInDays.intValue());
                boolean isCorrupted = FileUtils.isCorrupted(file);
                boolean isDuplicate = FileUtils.isDuplicate(file, hashMap);
                results.add(new FileCheckResult(file, isOld, isCorrupted, isDuplicate));
            });

            queue.addAll(results);
            initialized = true;
        }
        return queue.poll();
    }
}
