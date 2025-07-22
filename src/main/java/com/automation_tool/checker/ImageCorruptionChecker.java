package com.automation_tool.checker;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageCorruptionChecker implements FileCorruptionChecker {
    @Override
    public boolean isCorrupted(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            BufferedImage img = ImageIO.read(is);
            if (img == null) throw new IOException("Unsupported or corrupted image");
        }
        return false;
    }
}

