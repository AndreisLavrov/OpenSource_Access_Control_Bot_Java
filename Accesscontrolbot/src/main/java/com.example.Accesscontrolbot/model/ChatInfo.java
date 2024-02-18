package com.example.Accesscontrolbot.model;
import jakarta.persistence.*;

@Entity
@Table(name = "chat_info")
public class ChatInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userId;  // Изменено на String
    private String chatId;  // Изменено на String

    // Конструкторы
    public ChatInfo() {
    }

    public ChatInfo(String userId, String chatId) {
        this.userId = userId;
        this.chatId = chatId;
    }

    // Геттеры и сеттеры
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
