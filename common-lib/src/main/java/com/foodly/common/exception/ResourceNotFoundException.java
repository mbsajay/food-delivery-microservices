package com.foodly.common.exception;

public class ResourceNotFoundException extends FoodlyException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException("%s with id %s was not found".formatted(resource, id));
    }
}
