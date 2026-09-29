package org.example.newproject.user.repository;

import org.example.newproject.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);
}
