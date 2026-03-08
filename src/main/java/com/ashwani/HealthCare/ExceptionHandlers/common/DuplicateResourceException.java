package com.ashwani.HealthCare.ExceptionHandlers.common;

/**
 * Thrown when attempting to create a resource that already exists
 * Examples: duplicate email, username, license number
 */
public class DuplicateResourceException extends RuntimeException {
    private final String resourceType;
    private final String field;

    public DuplicateResourceException(String resourceType, String field) {
        super(String.format("%s with this %s already exists", resourceType, field));
        this.resourceType = resourceType;
        this.field = field;
    }

    public DuplicateResourceException(String message) {
        super(message);
        this.resourceType = null;
        this.field = null;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getField() {
        return field;
    }
}
