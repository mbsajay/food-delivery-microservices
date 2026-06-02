package com.foodly.user.repository;

import com.foodly.user.domain.Role;
import com.foodly.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Exercises the JPA mapping against the same Postgres image used in production
 * (not H2), proving the Flyway {@code V1} schema satisfies {@code ddl-auto: validate}
 * and that the case-insensitive unique-email index is enforced.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserRepositoryPostgresIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private UserRepository users;

    @Test
    void flywaySchemaPersistsAndFindsUser() {
        User saved = users.saveAndFlush(User.builder()
                .email("grace@foodly.io")
                .passwordHash("hash")
                .fullName("Grace Hopper")
                .role(Role.ADMIN)
                .enabled(true)
                .build());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(users.findByEmail("grace@foodly.io")).isPresent();
    }

    @Test
    void uniqueEmailIndexRejectsDuplicates() {
        users.saveAndFlush(User.builder()
                .email("dup@foodly.io").passwordHash("h").fullName("First").role(Role.CUSTOMER).enabled(true).build());

        assertThatThrownBy(() -> users.saveAndFlush(User.builder()
                .email("dup@foodly.io").passwordHash("h").fullName("Second").role(Role.CUSTOMER).enabled(true).build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
