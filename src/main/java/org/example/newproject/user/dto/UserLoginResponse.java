package org.example.newproject.user.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({ "accessToken", "expiresIn", "nickname" })
public class UserLoginResponse {

    private String accessToken;
    private long expiresIn;
    private String nickname;
}