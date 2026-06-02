package com.foodly.user.service;

import com.foodly.common.exception.ResourceNotFoundException;
import com.foodly.user.domain.User;
import com.foodly.user.dto.UpdateProfileRequest;
import com.foodly.user.dto.UserResponse;
import com.foodly.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository users;

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return UserResponse.from(find(id));
    }

    @Transactional
    public UserResponse updateProfile(UUID id, UpdateProfileRequest request) {
        User user = find(id);
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        return UserResponse.from(user);
    }

    private User find(UUID id) {
        return users.findById(id).orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }
}
