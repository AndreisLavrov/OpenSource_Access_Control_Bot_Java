package com.example.Accesscontrolbot.repository;

import com.example.Accesscontrolbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    Optional<User> findByIdAndEmail(Long id, String email);
    List<User> findByEmailIsNull();

}
