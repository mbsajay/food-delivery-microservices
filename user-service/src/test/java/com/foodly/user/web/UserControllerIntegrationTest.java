package com.foodly.user.web;

import com.foodly.user.domain.Role;
import com.foodly.user.dto.UserResponse;
import com.foodly.user.security.SecurityConfig;
import com.foodly.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // SecurityConfig wires a JwtDecoder; the jwt() post-processor bypasses it, so a stub bean suffices.
    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void me_requiresAuthentication() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_returnsProfileForAuthenticatedUser() throws Exception {
        UUID id = UUID.randomUUID();
        UserResponse profile = new UserResponse(
                id, "ada@foodly.io", "Ada Lovelace", "+10000000000", Role.CUSTOMER, Instant.parse("2026-06-02T10:15:30Z"));
        when(userService.getById(eq(id))).thenReturn(profile);

        mockMvc.perform(get("/users/me").with(jwt().jwt(j -> j.subject(id.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("ada@foodly.io"))
                .andExpect(jsonPath("$.data.role").value("CUSTOMER"));
    }
}
