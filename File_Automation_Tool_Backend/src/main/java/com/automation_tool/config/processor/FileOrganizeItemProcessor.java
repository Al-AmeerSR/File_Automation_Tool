package com.automation_tool.config.processor;


import com.automation_tool.dto.FileCheckResult;
import org.springframework.batch.item.ItemProcessor;

public class FileOrganizeItemProcessor implements ItemProcessor<FileCheckResult, FileCheckResult> {

    @Override
    public FileCheckResult process(FileCheckResult item) {
        return item;
    }
}
