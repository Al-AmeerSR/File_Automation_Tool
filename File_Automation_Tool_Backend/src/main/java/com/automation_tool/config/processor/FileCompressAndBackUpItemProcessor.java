package com.automation_tool.config.processor;
import org.springframework.batch.item.ItemProcessor;

import java.nio.file.Path;

public class FileCompressAndBackUpItemProcessor implements ItemProcessor<Path, Path> {
    @Override
    public Path process(Path item) {
        return item; // no transformation
    }
}
