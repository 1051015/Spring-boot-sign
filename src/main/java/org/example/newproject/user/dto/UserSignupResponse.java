package org.example.newproject.user.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
@JsonPropertyOrder({ "id", "nickname", "createdAt" })
public class UserSignupResponse {
    private Long id;
    private String nickname;
    private OffsetDateTime createdAt;
}
