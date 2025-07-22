package com.automation_tool.service;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportService {

    private final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Value("${report.folder.path}")
    private String reportFolderPath;

    @SuppressWarnings("unchecked")
    private void generateExcelReport(Map<String, Object> reportData) throws IOException {
        String mainHeading = (String) reportData.get("mainHeading");
        List<String> headers = (List<String>) reportData.get("headers");
        List<List<String>> rows = (List<List<String>>) reportData.get("rows");
        String fileName = (String) reportData.get("fileName");

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Report");

        int rowIndex = 0;

        // Create title style
        HSSFCellStyle titleStyle = workbook.createCellStyle();
        HSSFFont titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);

        // Main Heading Row
        HSSFRow headingRow = sheet.createRow(rowIndex++);
        HSSFCell titleCell = headingRow.createCell(0);
        titleCell.setCellValue(mainHeading);
        titleCell.setCellStyle(titleStyle);

        // Only merge if there are at least 2 columns
        if (headers.size() > 1) {
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.size() - 1));
        }

        // Header Style
        HSSFCellStyle headerStyle = workbook.createCellStyle();
        HSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Header Row
        HSSFRow headerRow = sheet.createRow(rowIndex++);
        for (int i = 0; i < headers.size(); i++) {
            HSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        // Data Rows
        for (List<String> row : rows) {
            HSSFRow dataRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < row.size(); i++) {
                dataRow.createCell(i).setCellValue(row.get(i));
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Ensure directory exists
        File folder = new File(reportFolderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File file = new File(folder, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }

        workbook.close();
    }


    public void generateExcelReportForCleanUp(List<List<String>> deletedFiles, int totalScanned, int currentFileCount) {
        if (!deletedFiles.isEmpty()) {
            int totalDeleted = deletedFiles.size();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

            List<List<String>> rows = new ArrayList<>(deletedFiles);

            // Add empty row for spacing
            rows.add(Collections.emptyList());

            // Add summary rows
            rows.add(Arrays.asList("Total Files Scanned", String.valueOf(totalScanned)));
            rows.add(Arrays.asList("Total Files Deleted", String.valueOf(totalDeleted)));
            rows.add(Arrays.asList("Current Files Remaining", String.valueOf(currentFileCount)));

            Map<String, Object> reportData = new HashMap<>();
            reportData.put("mainHeading", "Deleted Files Report");
            reportData.put("headers", Arrays.asList("File Path", "Reason", "Deleted On"));
            reportData.put("rows", rows);
            reportData.put("fileName", "deleted_files_report_" + timestamp + ".xls");

            try {
                generateExcelReport(reportData);
            } catch (IOException e) {
                logger.error("Failed to generate deleted files report", e);
            }
        }
    }

    public void generateExcelReportForOrganizedFiles(Map<String, List<String>> organizedFilesByType) {
        if (organizedFilesByType == null || organizedFilesByType.isEmpty()) return;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "organized_files_report_" + timestamp + ".xls";

        List<List<String>> rows = new ArrayList<>();
        int totalFiles = 0;

        for (Map.Entry<String, List<String>> entry : organizedFilesByType.entrySet()) {
            String category = entry.getKey();
            List<String> files = entry.getValue();

            // Subheading row
            rows.add(Collections.singletonList(category.toUpperCase()));

            for (String fileNameOnly : files) {
                rows.add(Collections.singletonList(fileNameOnly));
                totalFiles++;
            }

            // Empty row after each category
            rows.add(Collections.emptyList());

            // Summary per type
            rows.add(Arrays.asList("Total " + category + " files", String.valueOf(files.size())));
            rows.add(Collections.emptyList());
        }

        // Final summary
        rows.add(Arrays.asList("Total Files Organized", String.valueOf(totalFiles)));

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("mainHeading", "Organized Files Report");
        reportData.put("headers", List.of("File Name"));
        reportData.put("rows", rows);
        reportData.put("fileName", fileName);

        try {
            generateExcelReport(reportData);
        } catch (IOException e) {
            logger.error("Failed to generate organized files report", e);
        }
    }

    public void generateBackupReport(List<Path> backedUpFiles) {
        if (backedUpFiles == null || backedUpFiles.isEmpty()) return;

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "backup_report_" + timestamp + ".xls";

        List<List<String>> rows = new ArrayList<>();

        for (Path filePath : backedUpFiles) {
            rows.add(Arrays.asList(
                    filePath.getFileName().toString(),
                    filePath.toAbsolutePath().toString(),
                    timestamp
            ));
        }

        rows.add(Collections.emptyList());
        rows.add(Arrays.asList("Total Files Backed Up", String.valueOf(backedUpFiles.size())));

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("mainHeading", "Backup Report");
        reportData.put("headers", Arrays.asList("File Name", "Full Path", "Backup Timestamp"));
        reportData.put("rows", rows);
        reportData.put("fileName", fileName);

        try {
            generateExcelReport(reportData);
        } catch (IOException e) {
            logger.error("Failed to generate backup report", e);
        }
    }

}
