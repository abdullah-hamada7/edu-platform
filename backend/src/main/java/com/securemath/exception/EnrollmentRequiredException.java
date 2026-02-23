package com.securemath.exception;

public class EnrollmentRequiredException extends RuntimeException {
    
    public EnrollmentRequiredException(String message) {
        super(message);
    }
    
    public EnrollmentRequiredException() {
        super("Enrollment required for this resource");
    }
}
