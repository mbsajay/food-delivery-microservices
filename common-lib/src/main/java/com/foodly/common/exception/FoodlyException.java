package com.foodly.common.exception;

import lombok.Getter;

@Getter
public abstract class FoodlyException extends RuntimeException {

    private final ErrorCode errorCode;

    protected FoodlyException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected FoodlyException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
