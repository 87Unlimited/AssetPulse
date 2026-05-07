package com.assetpulse.backend.auth;

import com.assetpulse.backend.common.model.Role;
import com.assetpulse.backend.common.model.User;
import com.assetpulse.backend.common.repository.UserRepository;
import com.assetpulse.backend.common.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthService authService;

    @Test
    void register_newEmail_savesUserAndReturnsToken() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@example.com");
        req.setPassword("password123");
        req.setFullName("New User");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("bcrypt-hash");
        when(jwtService.generateToken(any(User.class))).thenReturn("generated-jwt");

        AuthResponse response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("generated-jwt");
        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("new@example.com")
                        && u.getPassword().equals("bcrypt-hash")
                        && u.getRole() == Role.USER));
    }

    @Test
    void register_duplicateEmail_throwsWithoutPersisting() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("taken@example.com");
        req.setPassword("password123");
        req.setFullName("User");

        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_authenticatesAndReturnsToken() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("password123");

        User user = User.builder()
                .email("user@example.com")
                .password("bcrypt-hash")
                .fullName("User")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("generated-jwt");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("generated-jwt");
        verify(authenticationManager).authenticate(
                argThat(t -> t instanceof UsernamePasswordAuthenticationToken));
    }

    @Test
    void login_badCredentials_propagatesAuthenticationException() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("wrong-password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadCredentialsException.class);
    }
}
