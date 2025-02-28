package com.moviezon.moviebackend.controller;

import com.moviezon.moviebackend.auth.entities.RefreshToken;
import com.moviezon.moviebackend.auth.entities.User;
import com.moviezon.moviebackend.auth.service.AuthService;
import com.moviezon.moviebackend.auth.service.JwtService;
import com.moviezon.moviebackend.auth.service.RefreshTokenService;
import com.moviezon.moviebackend.auth.utils.AuthResponse;
import com.moviezon.moviebackend.auth.utils.LoginRequest;
import com.moviezon.moviebackend.auth.utils.RefreshTokenRequest;
import com.moviezon.moviebackend.auth.utils.RegisterRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    private final RefreshTokenService refreshTokenService;

    private final JwtService jwtService;

    public AuthController(AuthService authService,
                          RefreshTokenService refreshTokenService,
                          JwtService jwtService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    // endpoint to register new user
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received user: {}", request);
        return ResponseEntity.ok(authService.register(request));
    }

    // endpoint to authenticate user for login
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();

        String token = this.jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .refreshToken(refreshToken.getRefreshToken())
                .token(token)
                .build());
    }
}
