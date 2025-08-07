package com.milosz.podsiadly.uiservice.controller;

import com.milosz.podsiadly.uiservice.config.JwtTokenUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtTokenUtil jwtService;

    public AuthController(JwtTokenUtil jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/success")
    public ResponseEntity<?> success(Authentication authentication) {
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token));
    }
}