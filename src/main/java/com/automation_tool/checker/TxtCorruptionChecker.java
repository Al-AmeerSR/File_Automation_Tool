package com.automation_tool.checker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TxtCorruptionChecker implements FileCorruptionChecker {
    @Override
    public boolean isCorrupted(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            char[] buffer = new char[1024];
            reader.read(buffer);
        }
        return false;
    }
}

