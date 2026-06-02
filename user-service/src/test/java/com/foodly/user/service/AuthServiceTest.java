package com.foodly.user.service;

import com.foodly.common.exception.ConflictException;
import com.foodly.common.exception.UnauthorizedException;
import com.foodly.user.domain.Role;
import com.foodly.user.domain.User;
import com.foodly.user.dto.LoginRequest;
import com.foodly.user.dto.SignupRequest;
import com.foodly.user.dto.TokenPair;
import com.foodly.user.dto.UserResponse;
import com.foodly.user.repository.UserRepository;
import com.foodly.user.security.JwtIssuer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository users;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Mock
    private JwtIssuer jwtIssuer;

    @InjectMocks
    private AuthService authService;

    @Test
    void signup_persistsNormalisedEmailAndHashesPassword() {
        when(users.existsByEmail("ada@foodly.io")).thenReturn(false);
        when(passwordEncoder.encode("supersecret")).thenReturn("hashed");
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = authService.signup(
                new SignupRequest("Ada@Foodly.io", "supersecret", "Ada Lovelace", null, null));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("ada@foodly.io");
        assertThat(saved.getValue().getPasswordHash()).isEqualTo("hashed");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(saved.getValue().isEnabled()).isTrue();
        assertThat(response.email()).isEqualTo("ada@foodly.io");
    }

    @Test
    void signup_rejectsDuplicateEmail() {
        when(users.existsByEmail("ada@foodly.io")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(
                new SignupRequest("ada@foodly.io", "supersecret", "Ada", null, null)))
                .isInstanceOf(ConflictException.class);

        verify(users, never()).save(any());
    }

    @Test
    void login_returnsTokenPairOnValidCredentials() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("ada@foodly.io")
                .passwordHash("hashed")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
        when(users.findByEmail("ada@foodly.io")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("supersecret", "hashed")).thenReturn(true);
        TokenPair expected = TokenPair.bearer("access", "refresh", 900);
        when(jwtIssuer.issue(user)).thenReturn(expected);

        TokenPair pair = authService.login(new LoginRequest("ada@foodly.io", "supersecret"));

        assertThat(pair).isEqualTo(expected);
    }

    @Test
    void login_rejectsBadPassword() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("ada@foodly.io")
                .passwordHash("hashed")
                .role(Role.CUSTOMER)
                .enabled(true)
                .build();
        when(users.findByEmail("ada@foodly.io")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("wrong"), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ada@foodly.io", "wrong")))
                .isInstanceOf(UnauthorizedException.class);

        verify(jwtIssuer, never()).issue(any());
    }
}
