package io.restaurant.platform.shared.exception;

/**
 * Exception thrown when the authenticated user does not have permissions to access the resource.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

}
