package io.restaurant.platform.shared.exception;

/**
 * Root business domain runtime exception.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

}
