package com.automation_tool.checker;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;

public class PdfCorruptionChecker implements FileCorruptionChecker {
    @Override
    public boolean isCorrupted(File file) throws IOException {
        try (PDDocument doc = PDDocument.load(file)) {
            // No-op: load throws if corrupt
        }
        return false;
    }
}

