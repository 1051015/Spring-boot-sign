package org.example.newproject.user.repository;

import org.example.newproject.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, String> {


}
