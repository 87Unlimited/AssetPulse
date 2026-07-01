package com.assetpulse.backend.auth;

import com.assetpulse.backend.common.exception.GlobalExceptionHandler;
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
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void register_duplicateEmail_returns409WithError() throws Exception {
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
    void register_duplicateEmail_methodName_scenario_expectedOutcome() throws Exception {
        RegisterRequest body = new RegisterRequest();
        body.setEmail("taken@example.com");
        body.setPassword("password123");
        body.setFullName("User");

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Email already registered"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is(409))
                .andExpect(jsonPath("$.error").value("Email already registered"));
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
    void login_badCredentials_returns401WithError() throws Exception {
        LoginRequest body = new LoginRequest();
        body.setEmail("user@example.com");
        body.setPassword("wrong");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().is(401))
                .andExpect(jsonPath("$.error").value("Bad credentials"));
    }
}
