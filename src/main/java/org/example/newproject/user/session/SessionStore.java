package org.example.newproject.user.session;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionStore {

    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String DEVICE_TYPE = "deviceType";
    private static final String USER_AGENT = "userAgent";
    private static final String CREATED_AT = "createdAt";
    private static final String LAST_USED_AT = "lastUsedAt";
    private static final int USER_AGENT_MAX_LENGTH = 200;

    private final StringRedisTemplate redisTemplate;

    public void save(Long userId, String sessionId, String refreshToken, DeviceType deviceType,
                     String userAgent, Duration ttl) {
        String now = now();
        String key = sessionKey(userId, sessionId);

        hash().putAll(key, Map.of(
                REFRESH_TOKEN, refreshToken,
                DEVICE_TYPE, deviceType.name(),
                USER_AGENT, truncate(userAgent),
                CREATED_AT, now,
                LAST_USED_AT, now
        ));
        redisTemplate.expire(key, ttl);
    }

    public Optional<String> findRefreshToken(Long userId, String sessionId) {
        return Optional.ofNullable(hash().get(sessionKey(userId, sessionId), REFRESH_TOKEN));
    }

    public void touch(Long userId, String sessionId) {
        hash().put(sessionKey(userId, sessionId), LAST_USED_AT, now());
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

    private String now() {
        return Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }

    private String truncate(String userAgent) {
        if (userAgent == null) {
            return "";
        }
        return userAgent.length() > USER_AGENT_MAX_LENGTH ? userAgent.substring(0, USER_AGENT_MAX_LENGTH) : userAgent;
    }
}
