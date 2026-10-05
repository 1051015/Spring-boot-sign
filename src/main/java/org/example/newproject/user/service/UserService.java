package org.example.newproject.user.service;

import lombok.RequiredArgsConstructor;
import org.example.newproject.config.jwt.AccessTokenBlacklist;
import org.example.newproject.config.jwt.JwtProvider;
import org.example.newproject.user.domain.User;
import org.example.newproject.user.dto.MypageResponse;
import org.example.newproject.user.dto.TokenRefreshResponse;
import org.example.newproject.user.dto.UserLoginRequest;
import org.example.newproject.user.dto.UserLoginResponse;
import org.example.newproject.user.dto.UserSignupRequest;
import org.example.newproject.user.dto.UserSignupResponse;
import org.example.newproject.user.repository.UserRepo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private static final String REFRESH_KEY_PREFIX = "refresh:";

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;
    private final AccessTokenBlacklist accessTokenBlacklist;

    @Transactional
    public UserSignupResponse signup(UserSignupRequest request) {
        User user = User.create(
                request.getLoginId(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname()
        );

        return UserSignupResponse.from(userRepo.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResult login(UserLoginRequest request) {
        User user = userRepo.findByLoginId(request.getLoginId())
                .filter(found -> passwordEncoder.matches(request.getPassword(), found.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        String accessToken = jwtProvider.createAccessToken(user.getLoginId());
        String refreshToken = jwtProvider.createRefreshToken(user.getLoginId());

        redisTemplate.opsForValue().set(refreshKey(user), refreshToken, jwtProvider.getRefreshTokenValidity());

        UserLoginResponse response = UserLoginResponse.builder()
                .accessToken(accessToken)
                .expiresIn(jwtProvider.getAccessTokenExpiresInSeconds())
                .nickname(user.getNickname())
                .build();

        return new LoginResult(response, refreshToken);
    }

    @Transactional(readOnly = true)
    public TokenRefreshResponse refreshAccessToken(String refreshToken) {
        User user = findUserByStoredRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 Refresh Token입니다."));

        return TokenRefreshResponse.builder()
                .accessToken(jwtProvider.createAccessToken(user.getLoginId()))
                .expiresIn(jwtProvider.getAccessTokenExpiresInSeconds())
                .build();
    }

    @Transactional(readOnly = true)
    public MypageResponse getMypage(String loginId) {
        return userRepo.findByLoginId(loginId)
                .map(MypageResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    @Transactional(readOnly = true)
    public void logout(String refreshToken, String accessToken) {
        findUserByStoredRefreshToken(refreshToken)
                .ifPresent(user -> redisTemplate.delete(refreshKey(user)));

        if (accessToken != null && jwtProvider.validateAccessToken(accessToken)) {
            accessTokenBlacklist.add(accessToken, jwtProvider.getRemainingValidity(accessToken));
        }
    }

    private Optional<User> findUserByStoredRefreshToken(String refreshToken) {
        if (refreshToken == null || !jwtProvider.validateRefreshToken(refreshToken)) {
            return Optional.empty();
        }

        return userRepo.findByLoginId(jwtProvider.getLoginId(refreshToken))
                .filter(user -> refreshToken.equals(redisTemplate.opsForValue().get(refreshKey(user))));
    }

    private String refreshKey(User user) {
        return REFRESH_KEY_PREFIX + user.getId();
    }

    public record LoginResult(UserLoginResponse response, String refreshToken) {}
}
