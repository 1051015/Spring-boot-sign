package org.example.newproject.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtProvider {

    private static final Duration ACCESS_TOKEN_VALIDITY = Duration.ofMinutes(30);
    private static final Duration REFRESH_TOKEN_VALIDITY = Duration.ofDays(14);

    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String BEARER_PREFIX = "Bearer ";

    private final Key secretKey;

    public JwtProvider(@Value("${jwt.secret:defaultSecretKeyWithAtLeast32BytesLengthForSecurity!}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String loginId) {
        return createToken(loginId, ACCESS_TOKEN_TYPE, ACCESS_TOKEN_VALIDITY);
    }

    public String createRefreshToken(String loginId) {
        return createToken(loginId, REFRESH_TOKEN_TYPE, REFRESH_TOKEN_VALIDITY);
    }

    public boolean validateAccessToken(String token) {
        return hasType(token, ACCESS_TOKEN_TYPE);
    }

    public boolean validateRefreshToken(String token) {
        return hasType(token, REFRESH_TOKEN_TYPE);
    }

    public String getLoginId(String token) {
        return parseClaims(token).getSubject();
    }

    public Duration getRemainingValidity(String token) {
        long remainingMillis = parseClaims(token).getExpiration().getTime() - System.currentTimeMillis();
        return Duration.ofMillis(Math.max(remainingMillis, 0));
    }

    public String resolveBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorizationHeader.substring(BEARER_PREFIX.length()).trim();
    }

    public long getAccessTokenExpiresInSeconds() {
        return ACCESS_TOKEN_VALIDITY.toSeconds();
    }

    public Duration getRefreshTokenValidity() {
        return REFRESH_TOKEN_VALIDITY;
    }

    private String createToken(String loginId, String type, Duration validity) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(loginId)
                .claim(TOKEN_TYPE_CLAIM, type)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validity.toMillis()))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean hasType(String token, String type) {
        try {
            return type.equals(parseClaims(token).get(TOKEN_TYPE_CLAIM, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
