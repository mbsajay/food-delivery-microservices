package com.foodly.delivery.web;

import com.foodly.common.api.ApiError;
import com.foodly.common.api.ApiError.FieldViolation;
import com.foodly.common.api.ApiResponse;
import com.foodly.common.exception.ErrorCode;
import com.foodly.common.exception.FoodlyException;
import com.foodly.common.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/** Translates exceptions into the shared {@link ApiResponse} envelope. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(ValidationException ex, HttpServletRequest request) {
        ApiError error = baseError(ex.getErrorCode(), ex.getMessage(), request)
                .fieldErrors(ex.getFieldErrors())
                .build();
        return respond(ex.getErrorCode(), error);
    }

    @ExceptionHandler(FoodlyException.class)
    public ResponseEntity<ApiResponse<Object>> handleFoodly(FoodlyException ex, HttpServletRequest request) {
        return respond(ex.getErrorCode(), baseError(ex.getErrorCode(), ex.getMessage(), request).build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleBeanValidation(MethodArgumentNotValidException ex,
                                                                    HttpServletRequest request) {
        List<FieldViolation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> FieldViolation.builder()
                        .field(fe.getField())
                        .message(fe.getDefaultMessage())
                        .rejectedValue(fe.getRejectedValue())
                        .build())
                .toList();
        ApiError error = baseError(ErrorCode.VALIDATION_FAILED, "Request validation failed", request)
                .fieldErrors(violations)
                .build();
        return respond(ErrorCode.VALIDATION_FAILED, error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnexpected(Exception ex, HttpServletRequest request) {
        return respond(ErrorCode.INTERNAL_ERROR,
                baseError(ErrorCode.INTERNAL_ERROR, "Unexpected error", request).build());
    }

    private static ApiError.ApiErrorBuilder baseError(ErrorCode code, String message, HttpServletRequest request) {
        return ApiError.builder().code(code.getCode()).message(message).path(request.getRequestURI());
    }

    private static ResponseEntity<ApiResponse<Object>> respond(ErrorCode code, ApiError error) {
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.fail(error));
    }
}
