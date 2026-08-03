package io.restaurant.platform.shared.exception;

/**
 * Exception thrown when the authenticated user does not have permissions to access the resource.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

}
