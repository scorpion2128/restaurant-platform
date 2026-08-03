package io.restaurant.platform.shared.api;

public record ValidationError(
        String field,
        String message
) {

}