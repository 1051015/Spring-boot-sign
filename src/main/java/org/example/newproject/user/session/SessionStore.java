package org.example.newproject.user.session;

import lombok.RequiredArgsConstructor;
import org.example.newproject.common.util.TokenHasher;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SessionStore {

    private static final String REFRESH_TOKEN_HASH = "refreshTokenHash";
    private static final String DEVICE_TYPE = "deviceType";
    private static final String USER_AGENT = "userAgent";
    private static final String CREATED_AT = "createdAt";
    private static final int USER_AGENT_MAX_LENGTH = 200;

    private final StringRedisTemplate redisTemplate;

    public void save(Long userId, String sessionId, String refreshToken, DeviceType deviceType,
                     String userAgent, Duration ttl) {
        String key = sessionKey(userId, sessionId);

        hash().putAll(key, Map.of(
                REFRESH_TOKEN_HASH, TokenHasher.sha256(refreshToken),
                DEVICE_TYPE, deviceType.name(),
                USER_AGENT, truncate(userAgent),
                CREATED_AT, Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()
        ));
        redisTemplate.expire(key, ttl);
    }

    public boolean matchesRefreshToken(Long userId, String sessionId, String refreshToken) {
        String storedHash = hash().get(sessionKey(userId, sessionId), REFRESH_TOKEN_HASH);
        return TokenHasher.matches(refreshToken, storedHash);
    }

    public void delete(Long userId, String sessionId) {
        redisTemplate.delete(sessionKey(userId, sessionId));
    }

    private HashOperations<String, String, String> hash() {
        return redisTemplate.opsForHash();
    }

    private String sessionKey(Long userId, String sessionId) {
        return "session:" + userId + ":" + sessionId;
    }

    private String truncate(String userAgent) {
        if (userAgent == null) {
            return "";
        }
        return userAgent.length() > USER_AGENT_MAX_LENGTH ? userAgent.substring(0, USER_AGENT_MAX_LENGTH) : userAgent;
    }
}
