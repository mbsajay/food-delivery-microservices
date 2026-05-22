package com.foodly.common.exception;

public class UnauthorizedException extends FoodlyException {

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }

    public static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException("Invalid credentials");
    }

    public static UnauthorizedException expiredToken() {
        return new UnauthorizedException("Authentication token has expired");
    }
}
