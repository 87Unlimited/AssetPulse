package com.assetpulse.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthService authService;

    MockMvc mockMvc;
    final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    @Test
    void register_validRequest_returns200WithToken() throws Exception {
        RegisterRequest body = new RegisterRequest();
        body.setEmail("new@example.com");
        body.setPassword("password123");
        body.setFullName("New User");

        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse("test-jwt"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt"));
    }

    @Test
    void register_duplicateEmail_throwsUnhandledException() throws Exception {
        // IllegalArgumentException is currently unhandled — no @ControllerAdvice maps it.
        // In Spring 7, unhandled controller exceptions are rethrown by MockMvc.
        // Adding @ControllerAdvice to return 409 Conflict would improve this.
        RegisterRequest body = new RegisterRequest();
        body.setEmail("taken@example.com");
        body.setPassword("password123");
        body.setFullName("User");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already registered"));

        assertThatThrownBy(() -> mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        LoginRequest body = new LoginRequest();
        body.setEmail("user@example.com");
        body.setPassword("password123");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("test-jwt"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt"));
    }

    @Test
    void login_badCredentials_throwsAuthenticationException() throws Exception {
        // BadCredentialsException is unhandled here (no ExceptionTranslationFilter in
        // standalone setup). In the real app it becomes 401 via ExceptionTranslationFilter.
        LoginRequest body = new LoginRequest();
        body.setEmail("user@example.com");
        body.setPassword("wrong");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))))
                .hasCauseInstanceOf(BadCredentialsException.class);
    }
}
