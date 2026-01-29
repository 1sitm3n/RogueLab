package com.roguelab.telemetry;

/**
 * Exception thrown when telemetry operations fail.
 */
public class TelemetryException extends RuntimeException {
    
    public TelemetryException(String message) {
        super(message);
    }
    
    public TelemetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
