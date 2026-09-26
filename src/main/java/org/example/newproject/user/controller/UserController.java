package org.example.newproject.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.newproject.user.domain.User;
import org.example.newproject.user.dto.*;
import org.example.newproject.user.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {

        // 👈 2. UserService.LoginResult 타입으로 반환받음
        UserService.LoginResult loginResult = userService.login(request);

        // Set-Cookie (refreshToken) 헤더 생성
        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResult.refreshToken())
                .httpOnly(true)
                .secure(false) // 💡 로컬(http://localhost) 테스트 시 쿠키 저장이 안 된다면 임시로 false 지정 필요
                .path("/api/auth/refresh")
                .maxAge(1209600) // 14일
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(loginResult.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        TokenRefreshResponse response = userService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        userService.logout(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth/refresh")
                .maxAge(0) // 쿠키 만료
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "로그아웃되었습니다"));
    }

}
