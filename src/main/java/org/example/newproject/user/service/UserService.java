package org.example.newproject.user.service;

import lombok.RequiredArgsConstructor;
import org.example.newproject.config.jwt.JwtProvider;
import org.example.newproject.user.domain.User;
import org.example.newproject.user.dto.TokenRefreshResponse;
import org.example.newproject.user.dto.UserLoginRequest;
import org.example.newproject.user.dto.UserLoginResponse;
import org.example.newproject.user.repository.UserRepo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate; // 👈 Redis 주입

    @Transactional
    public User create(String loginId, String rawPassword, String nickname) {
        User user = new User();
        user.setLoginId(loginId);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname(nickname);

        return userRepo.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResult login(UserLoginRequest request) {
        User user = userRepo.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getLoginId());
        String refreshToken = jwtProvider.createRefreshToken(user.getLoginId());

        redisTemplate.opsForValue().set(
                "refresh:" + user.getId(),
                refreshToken,
                14,
                TimeUnit.DAYS
        );

        UserLoginResponse response = UserLoginResponse.builder()
                .accessToken(accessToken)
                .expiresIn(jwtProvider.getAccessTokenExpiresInSeconds())
                .nickname(user.getNickname())
                .build();

        String savedValue = redisTemplate.opsForValue().get("refresh:" + user.getId());
        System.out.println("Redis 저장 완료 -> Key: refresh:" + user.getId() + ", Value: " + savedValue);

        return new LoginResult(response, refreshToken);
    }

    @Transactional(readOnly = true)
    public TokenRefreshResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 Refresh Token입니다.");
        }

        String loginId = jwtProvider.getLoginId(refreshToken);
        User user = userRepo.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        String savedToken = redisTemplate.opsForValue().get("refresh:" + user.getId());
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw new IllegalArgumentException("이미 로그아웃되었거나 유효하지 않은 Refresh Token입니다.");
        }

        String newAccessToken = jwtProvider.createAccessToken(user.getLoginId());

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .expiresIn(jwtProvider.getAccessTokenExpiresInSeconds())
                .build();
    }

    public void logout(String refreshToken) {
        if (refreshToken != null && jwtProvider.validateToken(refreshToken)) {
            String loginId = jwtProvider.getLoginId(refreshToken);
            userRepo.findByLoginId(loginId).ifPresent(user -> {
                redisTemplate.delete("refresh:" + user.getId());
            });
        }

    }

    public record LoginResult(UserLoginResponse response, String refreshToken) {}
}