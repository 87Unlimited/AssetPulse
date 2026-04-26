package com.assetpulse.backend.common.security;

import com.assetpulse.backend.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        System.out.println("Loading user by email: " + email);
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("No user found with email: " + email));
    }
}