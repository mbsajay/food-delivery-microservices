package com.foodly.common.exception;

public class ConflictException extends FoodlyException {

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }

    public static ConflictException duplicate(String resource, String field, Object value) {
        return new ConflictException("%s with %s '%s' already exists".formatted(resource, field, value));
    }
}
