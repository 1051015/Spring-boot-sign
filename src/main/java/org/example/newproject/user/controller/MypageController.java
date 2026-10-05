package org.example.newproject.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.newproject.user.dto.MypageResponse;
import org.example.newproject.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MypageController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<MypageResponse> mypage(@AuthenticationPrincipal String loginId) {
        return ResponseEntity.ok(userService.getMypage(loginId));
    }
}
