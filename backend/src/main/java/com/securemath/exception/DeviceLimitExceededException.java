package com.securemath.exception;

public class DeviceLimitExceededException extends RuntimeException {
    
    public DeviceLimitExceededException(String message) {
        super(message);
    }
    
    public DeviceLimitExceededException(int limit) {
        super("Maximum device limit reached (" + limit + " devices)");
    }
}
