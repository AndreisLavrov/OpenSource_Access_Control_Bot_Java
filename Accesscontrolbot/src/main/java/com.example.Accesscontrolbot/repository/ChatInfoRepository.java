package com.example.Accesscontrolbot.repository;

import com.example.Accesscontrolbot.model.ChatInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatInfoRepository extends JpaRepository<ChatInfo, Long> {
    Optional<ChatInfo> findByUserId(String userId);
}
