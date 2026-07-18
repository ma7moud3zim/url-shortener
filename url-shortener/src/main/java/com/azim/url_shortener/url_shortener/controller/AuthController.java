package com.azim.url_shortener.url_shortener.controller;

import com.azim.url_shortener.url_shortener.dto.LoginRequest;
import com.azim.url_shortener.url_shortener.dto.RegisterRequest;
import com.azim.url_shortener.url_shortener.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        String message = userService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );
        return ResponseEntity.ok(message);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        String message = userService.verifyEmail(token);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(token);
    }
}