package com.assetpulse.backend.common.security;

import com.assetpulse.backend.common.model.Role;
import com.assetpulse.backend.common.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET =
            "test-secret-that-is-at-least-32-bytes-long-for-hmac";

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3_600_000L);
        user = User.builder()
                .email("alice@example.com")
                .password("hashed")
                .fullName("Alice")
                .role(Role.USER)
                .build();
    }

    @Test
    void generateToken_producesNonBlankJwt() {
        assertThat(jwtService.generateToken(user)).isNotBlank();
    }

    @Test
    void extractEmail_returnsEmailEmbeddedInToken() {
        String token = jwtService.generateToken(user);
        assertThat(jwtService.extractEmail(token)).isEqualTo("alice@example.com");
    }

    @Test
    void isTokenValid_trueForMatchingUser() {
        String token = jwtService.generateToken(user);
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_falseForDifferentUser() {
        String token = jwtService.generateToken(user);
        User other = User.builder()
                .email("bob@example.com")
                .password("hashed")
                .fullName("Bob")
                .role(Role.USER)
                .build();
        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void extractEmail_throwsExpiredJwtExceptionForExpiredToken() {
        JwtService shortLived = new JwtService(SECRET, -1000L);
        String expiredToken = shortLived.generateToken(user);

        // JwtAuthFilter currently has no try/catch around extractEmail — this becomes a 500.
        // Wrapping the call in a try/catch there would return 401 instead.
        assertThatThrownBy(() -> jwtService.extractEmail(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
