package com.automation_tool.utils;

import com.automation_tool.checker.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {

private static final  Logger logger = LoggerFactory.getLogger(FileUtils.class);
private static final Map<String, FileCorruptionChecker> strategyMap = new HashMap<>();

    public FileUtils() {
        strategyMap.put(".pdf", new PdfCorruptionChecker());
        strategyMap.put(".docx", new DocxCorruptionChecker());
        strategyMap.put(".txt", new TxtCorruptionChecker());
        strategyMap.put(".zip", new ZipCorruptionChecker());
        strategyMap.put(".jpg", new ImageCorruptionChecker());
        strategyMap.put(".jpeg", new ImageCorruptionChecker());
        strategyMap.put(".png", new ImageCorruptionChecker());
        strategyMap.put(".gif", new ImageCorruptionChecker());
    }

    public static boolean isDuplicate(File file, Map<String, File> hashMap) {
        try {
            String hash = getFileHash(file);
            if (hashMap.containsKey(hash)) {
                return true;
            } else {
                hashMap.put(hash, file);
            }
        } catch (Exception e) {
            logger.warn("Failed to compute hash: {}", file.getAbsolutePath(), e);
        }
        return false;
    }

    private static String getFileHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        byte[] hashBytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String getFileCategory(String filename) {
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf(".") + 1).toLowerCase() : "";
        return switch (ext) {
            case "pdf", "doc", "docx", "xls", "xlsx", "txt" -> "documents";
            case "jpg", "jpeg", "png", "gif", "bmp" -> "images";
            case "mp4", "mkv", "avi" -> "videos";
            case "mp3", "wav", "aac" -> "audio";
            case "zip", "rar", "7z" -> "archives";
            default -> "others";
        };
    }

    public static long getTotalSize(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return 0;

        long size = 0;
        for (File file : files) {
            if (file.isFile()) size += file.length();
            else if (file.isDirectory()) size += getTotalSize(file);
        }
        return size;
    }

    public static  boolean isOld(File file, int ageInDays) {
        Instant cutoff = Instant.now().minusSeconds(ageInDays * 24L * 60 * 60);
        return file.lastModified() < cutoff.toEpochMilli();
    }

    public static int countFiles(File root) {
        return traverse(root, false, null);
    }


    public static List<File> listAllFiles(File root) {
        List<File> result = new ArrayList<>();
        traverse(root, true, result);
        return result;
    }


    private static int traverse(File root, boolean collectFiles, List<File> result) {
        if (root == null || !root.exists()) return 0;

        int count = 0;
        Queue<File> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            File dir = queue.poll();
            File[] files = dir.listFiles();
            if (files == null) continue;

            for (File file : files) {
                if (file.isDirectory()) {
                    queue.add(file);
                } else {
                    count++;
                    if (collectFiles && result != null) {
                        result.add(file);
                    }
                }
            }
        }

        return count;
    }

    public static boolean isCorrupted(File file) {
        String extension = getExtension(file.getName());
        FileCorruptionChecker checker = strategyMap.get(extension);
        if (checker == null) return false; // Assume unknown types are OK
        try {
            return checker.isCorrupted(file);
        } catch (Exception e) {
            logger.info("Corrupt file: {} - {}", file.getAbsolutePath(), e.getMessage());
            return true;
        }
    }

    private static String getExtension(String name) {
        int lastDot = name.lastIndexOf(".");
        return lastDot != -1 ? name.substring(lastDot).toLowerCase() : "";
    }

    public static void walkFiles(File root, Consumer<File> fileConsumer) {
        Queue<File> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            File dir = queue.poll();
            File[] files = dir.listFiles();
            if (files == null) continue;

            for (File file : files) {
                if (file.isDirectory()) {
                    queue.add(file);
                } else {
                    fileConsumer.accept(file);
                }
            }
        }
    }

    public static void ensureDirectoryExists(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static LocalDate getFileCreationDate(File file) {
        try {
            FileTime creationTime = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
            return creationTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (IOException e) {
            return Instant.ofEpochMilli(file.lastModified()).atZone(ZoneId.systemDefault()).toLocalDate();
        }
    }

    public static void addFileToZip(ZipOutputStream zos, Path root, Path path) throws IOException {
        ZipEntry zipEntry = new ZipEntry(root.relativize(path).toString());
        zos.putNextEntry(zipEntry);
        Files.copy(path, zos);
        zos.closeEntry();
    }

}
