package com.ashwani.HealthCare.ExceptionHandlers.auth;

/**
 * Thrown when a user attempts to access a resource they don't have permission for
 * Examples: accessing another user's appointment, unauthorized profile updates
 */
public class UnauthorizedAccessException extends RuntimeException {
    private final Long userId;
    private final String resourceType;

    public UnauthorizedAccessException(String message, Long userId, String resourceType) {
        super(message);
        this.userId = userId;
        this.resourceType = resourceType;
    }

    public UnauthorizedAccessException(String message) {
        super(message);
        this.userId = null;
        this.resourceType = null;
    }

    public Long getUserId() {
        return userId;
    }

    public String getResourceType() {
        return resourceType;
    }
}
