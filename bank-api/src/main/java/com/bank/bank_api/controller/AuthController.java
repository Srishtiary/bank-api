package com.bank.bank_api.controller;

import com.bank.bank_api.dto.LoginRequest;
import com.bank.bank_api.dto.LoginResponse;
import com.bank.bank_api.dto.RegisterRequest;
import com.bank.bank_api.entity.User;
import com.bank.bank_api.security.JwtUtil;
import com.bank.bank_api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService,
                          JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        User savedUser = userService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully with email: " + savedUser.getEmail());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String authenticatedEmail = authentication.getName();

        User user = userService.findByEmail(authenticatedEmail);
        String role = user.getRole().name();

        String token = jwtUtil.generateToken(authenticatedEmail, role);

        LoginResponse loginResponse = new LoginResponse(token, authenticatedEmail, role);
        return ResponseEntity.ok(loginResponse);
    }
}
