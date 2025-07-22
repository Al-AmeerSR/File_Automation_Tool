package com.automation_tool.exception;

public class DuplicateJobNameException extends RuntimeException {
    public DuplicateJobNameException(String message) {
        super(message);
    }
}
