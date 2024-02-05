package com.example.Accesscontrolbot.repository;

import com.example.Accesscontrolbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
//    List<User> findByUsername(String username);
// В интерфейсе UserRepository
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

}
