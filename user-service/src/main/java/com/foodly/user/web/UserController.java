package com.foodly.user.web;

import com.foodly.common.api.ApiResponse;
import com.foodly.user.dto.UpdateProfileRequest;
import com.foodly.user.dto.UserResponse;
import com.foodly.user.dto.UserSummary;
import com.foodly.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Authenticated profile surface. Reached through the gateway as {@code /api/users/**}
 * (StripPrefix=1 → {@code /users/**} here). The caller's id comes from the verified
 * JWT subject, never from a path/body parameter.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok(userService.getById(currentUserId(jwt)));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@AuthenticationPrincipal Jwt jwt,
                                              @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(userService.updateProfile(currentUserId(jwt), request));
    }

    /** Internal lookup used service-to-service (e.g. order-service labelling a customer). */
    @GetMapping("/{id}/summary")
    public ApiResponse<UserSummary> summary(@PathVariable UUID id) {
        return ApiResponse.ok(userService.getSummary(id));
    }

    private static UUID currentUserId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
