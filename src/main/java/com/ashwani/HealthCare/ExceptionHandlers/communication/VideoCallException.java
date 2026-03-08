package com.ashwani.HealthCare.ExceptionHandlers.communication;

/**
 * Thrown when video call operations fail
 * Examples: room creation failure, invalid access token, participant issues
 */
public class VideoCallException extends RuntimeException {
    private final String roomName;
    private final String errorType;

    public VideoCallException(String message, String roomName, String errorType) {
        super(message);
        this.roomName = roomName;
        this.errorType = errorType;
    }

    public VideoCallException(String message) {
        super(message);
        this.roomName = null;
        this.errorType = null;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getErrorType() {
        return errorType;
    }
}
