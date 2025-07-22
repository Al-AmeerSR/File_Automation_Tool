package com.automation_tool.checker;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

public class ZipCorruptionChecker implements FileCorruptionChecker {
    @Override
    public boolean isCorrupted(File file) throws IOException {
        try (ZipFile zip = new ZipFile(file)) {
            zip.entries();
        }
        return false;
    }
}

