package com.automation_tool.config.writer;


import com.automation_tool.service.ReportService;
import com.automation_tool.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardOpenOption.CREATE;

public class FileCompressAndBackUpItemWriter implements ItemWriter<Path> {

    private final Logger logger = LoggerFactory.getLogger(FileCompressAndBackUpItemWriter.class);
    private final String rootDir;
    private final ReportService reportService;
    private boolean initialized = false;
    private ZipOutputStream zos;

    public FileCompressAndBackUpItemWriter(ReportService reportService,
                                           String rootDir) {
        this.reportService = reportService;
        this.rootDir = rootDir;
    }

    @Override
    public void write(Chunk<? extends Path> items) throws Exception {
        if (!initialized) {
            initZipStream();
        }
        logger.info("path : {}",items);
        for (Path path : items) {
            try {
                FileUtils.addFileToZip(zos, Path.of(rootDir), path);
                logger.info("Added to ZIP: {}", path);
            } catch (IOException e) {
                logger.warn("Failed to add to ZIP: {}", path, e);
            }
        }

        if (!items.isEmpty()) {
            List<Path> paths = new ArrayList<>(items.getItems());
            reportService.generateBackupReport(paths);

        }
    }

    private void initZipStream() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupDir = new File(rootDir, "backup_" + timestamp);
        FileUtils.ensureDirectoryExists(backupDir);

        File zipFile = new File(backupDir, "backup_" + timestamp + ".zip");
        zos = new ZipOutputStream(Files.newOutputStream(zipFile.toPath(), CREATE));
        initialized = true;
    }

    // Optionally close the stream manually in a job listener or bean destroy method
}
