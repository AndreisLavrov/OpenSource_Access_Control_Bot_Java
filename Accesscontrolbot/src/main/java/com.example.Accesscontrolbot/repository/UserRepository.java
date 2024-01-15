package com.example.Accesscontrolbot.repository;

import com.example.Accesscontrolbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
