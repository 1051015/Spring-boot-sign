package org.example.newproject.user.service;

import lombok.RequiredArgsConstructor;
import org.example.newproject.common.exception.BusinessException;
import org.example.newproject.common.exception.ErrorCode;
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
import org.example.newproject.user.session.DeviceType;
import org.example.newproject.user.session.SessionStore;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final SessionStore sessionStore;
    private final AccessTokenBlacklist accessTokenBlacklist;

    @Transactional
    public UserSignupResponse signup(UserSignupRequest request) {
        if (userRepo.existsByLoginId(request.getLoginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        User user = User.create(
                request.getLoginId(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname()
        );

        return UserSignupResponse.from(userRepo.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResult login(UserLoginRequest request, String userAgent) {
        User user = userRepo.findByLoginId(request.getLoginId())
                .filter(found -> passwordEncoder.matches(request.getPassword(), found.getPassword()))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        String sessionId = UUID.randomUUID().toString();
        String accessToken = jwtProvider.createAccessToken(user.getLoginId(), sessionId, request.getDeviceType());
        String refreshToken = jwtProvider.createRefreshToken(user.getLoginId(), sessionId, request.getDeviceType());

        sessionStore.save(user.getId(), sessionId, refreshToken, request.getDeviceType(), userAgent,
                jwtProvider.getRefreshTokenValidity());

        UserLoginResponse response = UserLoginResponse.builder()
                .accessToken(accessToken)
                .expiresIn(jwtProvider.getAccessTokenExpiresInSeconds())
                .nickname(user.getNickname())
                .build();

        return new LoginResult(response, refreshToken);
    }

    @Transactional(readOnly = true)
    public TokenRefreshResponse refreshAccessToken(String refreshToken) {
        ActiveSession session = findActiveSession(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        sessionStore.touch(session.user().getId(), session.sessionId());

        return TokenRefreshResponse.builder()
                .accessToken(jwtProvider.createAccessToken(
                        session.user().getLoginId(), session.sessionId(), session.deviceType()))
                .expiresIn(jwtProvider.getAccessTokenExpiresInSeconds())
                .build();
    }

    @Transactional(readOnly = true)
    public MypageResponse getMypage(String loginId) {
        return MypageResponse.from(findUser(loginId));
    }

    @Transactional(readOnly = true)
    public void logout(String refreshToken, String accessToken) {
        findActiveSession(refreshToken)
                .ifPresent(session -> sessionStore.delete(session.user().getId(), session.sessionId()));

        if (accessToken != null && jwtProvider.validateAccessToken(accessToken)) {
            accessTokenBlacklist.add(accessToken, jwtProvider.getRemainingValidity(accessToken));
        }
    }

    private Optional<ActiveSession> findActiveSession(String refreshToken) {
        if (refreshToken == null || !jwtProvider.validateRefreshToken(refreshToken)) {
            return Optional.empty();
        }

        String sessionId = jwtProvider.getSessionId(refreshToken);
        DeviceType deviceType = jwtProvider.getDeviceType(refreshToken);
        if (sessionId == null || deviceType == null) {
            return Optional.empty();
        }

        return userRepo.findByLoginId(jwtProvider.getLoginId(refreshToken))
                .filter(user -> sessionStore.findRefreshToken(user.getId(), sessionId)
                        .filter(refreshToken::equals)
                        .isPresent())
                .map(user -> new ActiveSession(user, sessionId, deviceType));
    }

    private User findUser(String loginId) {
        return userRepo.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private record ActiveSession(User user, String sessionId, DeviceType deviceType) {}

    public record LoginResult(UserLoginResponse response, String refreshToken) {}
}
