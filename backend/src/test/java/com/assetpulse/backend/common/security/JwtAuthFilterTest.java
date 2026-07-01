package com.assetpulse.backend.common.security;

import com.assetpulse.backend.common.model.Role;
import com.assetpulse.backend.common.model.User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock JwtService jwtService;
    @Mock UserDetailsServiceImpl userDetailsService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private User buildUser(String email) {
        return User.builder()
                .email(email)
                .password("hashed")
                .fullName("Alice")
                .role(Role.USER)
                .build();
    }

    @Test
    void noAuthHeader_passesChainWithoutSettingAuth() throws Exception {
        JwtAuthFilter filter = new JwtAuthFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    @Test
    void validToken_setsAuthenticationInSecurityContext() throws Exception {
        User user = buildUser("alice@example.com");
        when(jwtService.extractEmail("valid-token")).thenReturn("alice@example.com");
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(user);
        when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);

        JwtAuthFilter filter = new JwtAuthFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("alice@example.com");
        verify(chain).doFilter(request, response);
    }

    @Test
    void invalidToken_doesNotSetAuthentication() throws Exception {
        User user = buildUser("alice@example.com");
        when(jwtService.extractEmail("bad-token")).thenReturn("alice@example.com");
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(user);
        when(jwtService.isTokenValid("bad-token", user)).thenReturn(false);

        JwtAuthFilter filter = new JwtAuthFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void expiredToken_returns401() throws Exception{
        User user = buildUser("alice@example.com");
        // 1. Create a mock request with "Bearer some-expired-token" header
        JwtAuthFilter filter = new JwtAuthFilter(jwtService, userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer some-expired-token");
        // 2. Create a mock response
        MockHttpServletResponse response = new MockHttpServletResponse();
        // 3. Create a mock filter chain
        FilterChain chain = mock(FilterChain.class);
        // 4. Tell jwtService: when extractEmail() is called, throw ExpiredJwtException
        when(jwtService.extractEmail(any()))
                .thenThrow(new ExpiredJwtException(null, null, "expired"));
        // 5. Run the filter
        filter.doFilterInternal(request, response, chain);
        // 6. Assert response status is 401
        assertThat(response.getStatus()).isEqualTo(401);
        // 7. Assert filterChain.doFilter() was NEVER called
        verify(chain, never()).doFilter(request, response);
    }
}
