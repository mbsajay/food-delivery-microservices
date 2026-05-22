package com.foodly.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 4xx
    BAD_REQUEST("FOODLY-400", 400),
    UNAUTHORIZED("FOODLY-401", 401),
    FORBIDDEN("FOODLY-403", 403),
    RESOURCE_NOT_FOUND("FOODLY-404", 404),
    CONFLICT("FOODLY-409", 409),
    VALIDATION_FAILED("FOODLY-422", 422),

    // 5xx
    INTERNAL_ERROR("FOODLY-500", 500),
    UPSTREAM_FAILURE("FOODLY-502", 502),
    SERVICE_UNAVAILABLE("FOODLY-503", 503);

    private final String code;
    private final int httpStatus;
}
