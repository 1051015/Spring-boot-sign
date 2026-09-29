package org.example.newproject.config.jwt;

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

    private final Key secretKey;

    public JwtProvider(@Value("${jwt.secret:defaultSecretKeyWithAtLeast32BytesLengthForSecurity!}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(String loginId) {
        return createToken(loginId, ACCESS_TOKEN_VALIDITY);
    }

    public String createRefreshToken(String loginId) {
        return createToken(loginId, REFRESH_TOKEN_VALIDITY);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getLoginId(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public long getAccessTokenExpiresInSeconds() {
        return ACCESS_TOKEN_VALIDITY.toSeconds();
    }

    public Duration getRefreshTokenValidity() {
        return REFRESH_TOKEN_VALIDITY;
    }

    private String createToken(String loginId, Duration validity) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(loginId)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validity.toMillis()))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
