package org.example.newproject.user;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class UserSignupResponse {
    private String loginId;       // 또는 Long id (엔티티의 PK 타입에 맞춰 지정)
    private String nickname;
    private OffsetDateTime createdAt;
}
