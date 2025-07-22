package com.automation_tool.config.writer;

import com.automation_tool.dto.FileCheckResult;
import com.automation_tool.service.ReportService;
import com.automation_tool.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileCleanUpItemWriter implements ItemWriter<FileCheckResult>, StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(FileCleanUpItemWriter.class);

    private final ReportService reportService;
    private final String rootDir;

    private final List<List<String>> deletedFiles = new ArrayList<>();
    private int old = 0, corrupted = 0, duplicate = 0;

    public FileCleanUpItemWriter(ReportService reportService, String rootDir) {
        this.reportService = reportService;
        this.rootDir = rootDir;
    }

    @Override
    public void write(Chunk<? extends FileCheckResult> items) {
        for (FileCheckResult result : items) {
            File file = result.file();
            String reason = null;

            if (file.exists() && file.delete()) {
                if (result.isOld()) { old++; reason = "Old File"; }
                else if (result.isCorrupted()) { corrupted++; reason = "Corrupted File"; }
                else if (result.isDuplicate()) { duplicate++; reason = "Duplicate File"; }

                deletedFiles.add(List.of(file.getAbsolutePath(), Objects.requireNonNullElse(reason, "Unknown"), LocalDate.now().toString()));
                logger.info("Deleted: {}, Reason: {}", file.getAbsolutePath(), reason);
            } else {
                logger.warn("Failed to delete file: {}", file.getAbsolutePath());
            }
        }
        File root = new File(rootDir);
        int currentFileCount = FileUtils.countFiles(root);
        int totalDeleted = old + corrupted + duplicate;
        reportService.generateExcelReportForCleanUp(deletedFiles, totalDeleted, currentFileCount);
    }

}
