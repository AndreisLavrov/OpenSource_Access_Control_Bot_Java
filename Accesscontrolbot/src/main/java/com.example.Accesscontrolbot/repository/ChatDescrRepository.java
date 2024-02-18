package com.example.Accesscontrolbot.repository;
import com.example.Accesscontrolbot.model.ChatDescr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatDescrRepository extends JpaRepository<ChatDescr, Long> {
    // Здесь можно добавить специфичные методы, если это необходимо
}
