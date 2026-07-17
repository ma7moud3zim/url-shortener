package com.azim.url_shortener.url_shortener.service;

import com.azim.url_shortener.url_shortener.entity.User;
import com.azim.url_shortener.url_shortener.repository.UserRepository;
import com.azim.url_shortener.url_shortener.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    // Spring Security calls this to load user by email
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    // Register new user
    public String register(String username, String email, String password) {
        // check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        // generate verification token
        String token = UUID.randomUUID().toString();

        // build and save user
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role("ROLE_USER")
                .enabled(false) // not enabled until email verified
                .verificationToken(token)
                .build();

        userRepository.save(user);

        // send verification email
        emailService.sendVerificationEmail(email, token);

        return "Registration successful. Please check your email to verify your account.";
    }

    // Verify email
    public String verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return "Email verified successfully. You can now login.";
    }

    // Login
    public String login(String email, String password) {
        // this throws exception if credentials are wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        // if we reach here, credentials are correct
        return jwtUtil.generateToken(email);
    }
}