package org.example.newproject.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "user2")
@NoArgsConstructor
public class User implements Persistable<String> {

    @Id
    @Column(name = "\"loginId\"") // 👈 DB의 "loginId" 컬럼 매핑
    private String loginId;

    private String password;

    private String nickname;

    @CreationTimestamp
    @Column(name = "\"createdAt\"", nullable = false, updatable = false) // 👈 DB의 "createdAt" 컬럼 매핑
    private OffsetDateTime createdAt;

    // --- Persistable 필수 구현 메서드 ---
    @Override
    public String getId() {
        return this.loginId;
    }

    @Override
    public boolean isNew() {
        // createdAt이 null이면 신규 엔티티로 판단하여 INSERT 수행
        return this.createdAt == null;
    }
}