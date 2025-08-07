package com.milosz.podsiadly.userservice.controller;

import com.milosz.podsiadly.userservice.dto.CreateUserRequest;
import com.milosz.podsiadly.userservice.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Void> createUserIfNotExists(@RequestBody CreateUserRequest request) {
        userService.createUserIfNotExists(request);
        return ResponseEntity.ok().build();
    }
}


