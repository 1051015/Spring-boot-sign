package org.example.newproject.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        // 1. 회원가입 처리 및 엔티티 저장
        User savedUser = userService.create(
                request.getLoginId(),
                request.getPassword(),
                request.getNickname()
        );

        // 2. 응답 DTO 구성
        UserSignupResponse response = UserSignupResponse.builder()
                .loginId(savedUser.getLoginId()) // PK 필드명 매핑
                .nickname(savedUser.getNickname())
                .createdAt(savedUser.getCreatedAt())
                .build();

        // 3. 200 OK 응답 및 JSON 출력
        return ResponseEntity.ok(response);
    }

}
