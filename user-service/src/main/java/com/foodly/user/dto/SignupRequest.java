package com.foodly.user.dto;

import com.foodly.user.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Self-service registration payload. {@code role} is optional and defaults to
 * {@link Role#CUSTOMER} — privileged roles are never self-assignable here.
 */
public record SignupRequest(

        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, max = 72)
        String password,

        @NotBlank @Size(max = 150)
        String fullName,

        @Size(max = 30)
        String phone,

        Role role
) {
}
