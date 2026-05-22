package com.foodly.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private final String code;
    private final String message;
    private final String path;
    private final List<FieldViolation> fieldErrors;
    private final Map<String, Object> details;

    @Getter
    @Builder
    public static class FieldViolation {
        private final String field;
        private final String message;
        private final Object rejectedValue;
    }
}
