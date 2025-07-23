package com.automation_tool.service;

import com.automation_tool.dto.FileCheckResult;
import com.automation_tool.dto.FileDeletionSummary;
import com.automation_tool.dto.FileMetadata;
import com.automation_tool.utils.FileUtils;
import org.quartz.JobDataMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardOpenOption.CREATE;

@Service
public class FileService {

    private final Logger logger = LoggerFactory.getLogger(FileService.class);
    private final ReportService reportService;

    public FileService(ReportService reportService) {
        this.reportService = reportService;
    }

    public void cleanupFiles(JobDataMap dataMap) {
        String rootDir = dataMap.getString("rootDir");
        int fileAgeInDays = dataMap.getInt("fileAgeInDays");

        File root = new File(rootDir);
        Map<String, File> hashMap = new HashMap<>();

        List<FileCheckResult> results = new ArrayList<>();
        FileUtils.walkFiles(root, file -> {
            boolean isOld = FileUtils.isOld(file, fileAgeInDays);
            boolean isCorrupted = FileUtils.isCorrupted(file);
            boolean isDuplicate = FileUtils.isDuplicate(file, hashMap);
            results.add(new FileCheckResult(file, isOld, isCorrupted, isDuplicate));
        });

        FileDeletionSummary summary = deleteFilesWithReason(results);

        int currentFileCount = FileUtils.countFiles(root);
        reportService.generateExcelReportForCleanUp(summary.deletedFiles(), results.size(), currentFileCount);
    }

    public FileDeletionSummary deleteFilesWithReason(List<FileCheckResult> results) {
        List<List<String>> deletedFiles = new ArrayList<>();
        int oldFileDeleted = 0;
        int corruptFileDeleted = 0;
        int duplicateFileDeleted = 0;

        for (FileCheckResult result : results) {
            File file = result.file();
            if (result.isOld() || result.isCorrupted() || result.isDuplicate()) {
                if (file.delete()) {
                    String reason = result.isOld() ? "Old File"
                            : result.isCorrupted() ? "Corrupted File"
                            : "Duplicate File";

                    deletedFiles.add(Arrays.asList(
                            file.getAbsolutePath(),
                            reason,
                            LocalDate.now().toString()
                    ));

                    if (result.isOld()) oldFileDeleted++;
                    if (result.isCorrupted()) corruptFileDeleted++;
                    if (result.isDuplicate()) duplicateFileDeleted++;

                    logger.info("Deleted file: {}", file.getAbsolutePath());
                } else {
                    logger.warn("Failed to delete file: {}", file.getAbsolutePath());
                }
            }
        }

        return new FileDeletionSummary(deletedFiles, oldFileDeleted, corruptFileDeleted, duplicateFileDeleted);
    }

    public void fileOrganize(JobDataMap dataMap) {
        String rootDir = dataMap.getString("rootDir");
        File root = new File(rootDir);

        Map<File, FileMetadata> filesToOrganize = new LinkedHashMap<>();

        FileUtils.walkFiles(root, file -> {
            LocalDate date = FileUtils.getFileCreationDate(file);
            String year = String.valueOf(date.getYear());
            String month = date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            String category = FileUtils.getFileCategory(file.getName());
            filesToOrganize.put(file, new FileMetadata(year, month, category));
        });

        Map<String, List<String>> organizedFilesByType = moveAndOrganizeFiles(filesToOrganize, root);
        reportService.generateExcelReportForOrganizedFiles(organizedFilesByType);
    }

    public Map<String, List<String>> moveAndOrganizeFiles(Map<File, FileMetadata> filesToOrganize, File root) {
        Map<String, List<String>> organizedFilesByType = new HashMap<>();

        for (Map.Entry<File, FileMetadata> entry : filesToOrganize.entrySet()) {
            File file = entry.getKey();
            FileMetadata meta = entry.getValue();

            try {
                File destDir = new File(root, meta.year() + File.separator + meta.month() + File.separator + meta.category());
                FileUtils.ensureDirectoryExists(destDir);

                File destFile = new File(destDir, file.getName());

                if (!destFile.exists()) {
                    Files.move(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    logger.info("Moved file: {} to {}", file.getAbsolutePath(), destFile.getAbsolutePath());

                    organizedFilesByType
                            .computeIfAbsent(meta.category(), k -> new ArrayList<>())
                            .add(file.getName());
                } else {
                    logger.warn("File already exists in target: {}", destFile.getAbsolutePath());
                }

            } catch (Exception e) {
                logger.error("Failed to move file: {}", file.getAbsolutePath(), e);
            }
        }

        return organizedFilesByType;
    }

    public void backupAndCompressFilesByType(JobDataMap dataMap) {
        String rootDirPath = dataMap.getString("rootDir");
        @SuppressWarnings("unchecked")
        List<String> extensions = (List<String>) dataMap.get("fileExtensions");

        File rootDir = new File(rootDirPath);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File backupDir = new File(rootDir, "backup_" + timestamp);
        FileUtils.ensureDirectoryExists(backupDir);

        File zipFile = new File(backupDir, "backup_" + timestamp + ".zip");

        List<Path> filesToBackup = FileUtils.listAllFiles(rootDir).stream()
                .map(File::toPath)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String name = path.getFileName().toString().toLowerCase();
                    return extensions.stream().anyMatch(name::endsWith);
                })
                .collect(Collectors.toList());

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile.toPath(), CREATE))) {
            for (Path path : filesToBackup) {
                try {
                    FileUtils.addFileToZip(zos, rootDir.toPath(), path);
                    logger.info("Added file to ZIP: {}", path);
                } catch (IOException e) {
                    logger.warn("Failed to add file to ZIP: {}", path, e);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to create ZIP archive", e);
        }

        if (!filesToBackup.isEmpty()) {
            reportService.generateBackupReport(filesToBackup);
        } else {
            logger.info("No files matched for backup.");
        }
    }

}
