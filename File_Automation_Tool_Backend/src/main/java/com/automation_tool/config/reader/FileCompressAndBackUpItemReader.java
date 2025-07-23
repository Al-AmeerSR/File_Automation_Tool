package com.automation_tool.config.reader;

import com.automation_tool.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileCompressAndBackUpItemReader implements ItemReader<Path> {

    private final String rootDir;
    private final String fileExtensions;
    private final Logger logger = LoggerFactory.getLogger(FileCompressAndBackUpItemReader.class);

    private Iterator<Path> fileIterator;

    public FileCompressAndBackUpItemReader(String rootDir, String fileExtensions) {
        this.rootDir = rootDir;
        this.fileExtensions = fileExtensions;
    }

    @Override
    public Path read() {
        if (fileIterator == null) {
            logger.info("Initializing file list for compression...");
            List<String> extensions = Arrays.stream(fileExtensions.replaceAll("[\\[\\]]", "").split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .toList();
            logger.info("extensions from request: {}",extensions.toString());
            File root = new File(rootDir);
            List<Path> paths = FileUtils.listAllFiles(root).stream()
                    .map(File::toPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return extensions.stream().anyMatch(name::endsWith);
                    })
                    .toList();

            logger.info("Found {} files to compress", paths.size());
            fileIterator = paths.iterator();
        }

        if (fileIterator.hasNext()) {
            Path nextPath = fileIterator.next();
            logger.info("Reading file for compression: {}", nextPath);
            return nextPath;
        }

        logger.info("No more files to compress");
        return null;
    }
}
