package org.example.newproject.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.newproject.user.domain.User;
import org.example.newproject.user.service.UserService;
import org.example.newproject.user.dto.UserSignupRequest;
import org.example.newproject.user.dto.UserSignupResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponse> signup(@Valid @RequestBody UserSignupRequest request) {

        User savedUser = userService.create(
                request.getLoginId(),
                request.getPassword(),
                request.getNickname()
        );

        UserSignupResponse response = UserSignupResponse.builder()
                .id(savedUser.getId())
                .nickname(savedUser.getNickname())
                .createdAt(savedUser.getCreatedAt())
                .build();

        return ResponseEntity.ok(response);
    }

}
