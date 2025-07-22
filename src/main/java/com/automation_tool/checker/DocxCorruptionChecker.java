package com.automation_tool.checker;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DocxCorruptionChecker implements FileCorruptionChecker {
    @Override
    public boolean isCorrupted(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            new XWPFDocument(fis);
        }
        return false;
    }
}

