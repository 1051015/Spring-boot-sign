package org.example.newproject.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.newproject.user.session.DeviceType;

@Getter
@NoArgsConstructor
public class UserLoginRequest {

    @NotBlank(message = "아이디를 입력해주세요.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    @NotNull(message = "기기 종류(MOBILE 또는 PC)를 입력해주세요.")
    private DeviceType deviceType;
}
