package org.example.newproject.user.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "\"loginId\"", nullable = false, unique = true)
    private String loginId;

    @Column(name = "\"password\"", nullable = false)
    private String password;

    @Column(name = "\"nickname\"")
    private String nickname;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static User create(String loginId, String encodedPassword, String nickname) {
        User user = new User();
        user.loginId = loginId;
        user.password = encodedPassword;
        user.nickname = nickname;
        return user;
    }
}
