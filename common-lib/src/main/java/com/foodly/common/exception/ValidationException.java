package com.foodly.common.exception;

import com.foodly.common.api.ApiError.FieldViolation;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class ValidationException extends FoodlyException {

    private final List<FieldViolation> fieldErrors;

    public ValidationException(String message, List<FieldViolation> fieldErrors) {
        super(ErrorCode.VALIDATION_FAILED, message);
        this.fieldErrors = fieldErrors == null ? List.of() : Collections.unmodifiableList(fieldErrors);
    }

    public ValidationException(String message) {
        this(message, List.of());
    }
}
