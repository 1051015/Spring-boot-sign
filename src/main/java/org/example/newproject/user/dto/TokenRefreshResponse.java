package org.example.newproject.user.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonPropertyOrder({ "accessToken", "expiresIn" })
public class TokenRefreshResponse {

    private String accessToken;
    private long expiresIn;
}