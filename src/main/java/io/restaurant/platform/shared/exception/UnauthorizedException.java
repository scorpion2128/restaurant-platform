package io.restaurant.platform.shared.exception;

/**
 * Exception thrown when authentication credentials are missing or invalid.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

}
