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
@JsonPropertyOrder({ "id", "loginId", "nickname", "createdAt" })
public class MypageResponse {

    private Long id;
    private String loginId;
    private String nickname;
    private OffsetDateTime createdAt;

    public static MypageResponse from(User user) {
        return MypageResponse.builder()
                .id(user.getId())
                .loginId(user.getLoginId())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt()
                        .withOffsetSameInstant(ZoneOffset.UTC)
                        .truncatedTo(ChronoUnit.SECONDS))
                .build();
    }
}
