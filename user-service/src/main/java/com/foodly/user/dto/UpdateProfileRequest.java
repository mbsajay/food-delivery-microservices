package com.foodly.user.dto;

import jakarta.validation.constraints.Size;

/** Partial profile update — null fields are left unchanged. */
public record UpdateProfileRequest(

        @Size(max = 150)
        String fullName,

        @Size(max = 30)
        String phone
) {
}
