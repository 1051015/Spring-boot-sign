package org.example.newproject.user.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import org.example.newproject.user.domain.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Getter
@Builder
@JsonPropertyOrder({ "id", "nickname", "createdAt" })
public class UserSignupResponse {

    private Long id;
    private String nickname;
    private OffsetDateTime createdAt;

    public static UserSignupResponse from(User user) {
        return UserSignupResponse.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt()
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .truncatedTo(ChronoUnit.SECONDS))
                .build();
    }
}
