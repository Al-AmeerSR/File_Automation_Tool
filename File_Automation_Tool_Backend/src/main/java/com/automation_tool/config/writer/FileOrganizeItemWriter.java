package com.automation_tool.config.writer;

import com.automation_tool.dto.FileCheckResult;
import com.automation_tool.dto.FileMetadata;
import com.automation_tool.service.FileService;
import com.automation_tool.service.ReportService;
import com.automation_tool.utils.FileUtils;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;


public class FileOrganizeItemWriter implements ItemWriter<FileCheckResult> {

    private final ReportService reportService;
    private final String rootDir;
    private final FileService fileService;

    public FileOrganizeItemWriter(FileService fileService,
                                  ReportService reportService,
                                  String rootDir) {
        this.fileService = fileService;
        this.reportService = reportService;
        this.rootDir = rootDir;
    }

    @Override
    public void write(Chunk<? extends FileCheckResult> items) {
        File root = new File(rootDir);
        Map<File, FileMetadata> filesToOrganize = new LinkedHashMap<>();

        for (FileCheckResult result : items) {
            File file = result.file();
            LocalDate date = FileUtils.getFileCreationDate(file);
            String year = String.valueOf(date.getYear());
            String month = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            String category = FileUtils.getFileCategory(file.getName());
            filesToOrganize.put(file, new FileMetadata (year, month, category));
        }

        // Reuse logic from FileService to move files and get summary
        Map<String, List<String>> organizedFilesByType = fileService.moveAndOrganizeFiles(filesToOrganize, root);

        // Now this matches the expected parameter type
        reportService.generateExcelReportForOrganizedFiles(organizedFilesByType);
    }


}

