package org.example.newproject.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public User create(String loginId, String rawPassword, String nickname) {
        User user = new User();
        user.setLoginId(loginId);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname(nickname);

        return userRepo.save(user); // 이제 save() 실행 시 안전하게 INSERT문이 나갑니다!
    }
}