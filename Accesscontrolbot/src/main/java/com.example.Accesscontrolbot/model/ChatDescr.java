package com.example.Accesscontrolbot.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "chat_descr")
public class ChatDescr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String chatId;
    @Column(name = "join_time_stamp", nullable = false)
    private LocalDateTime joinTimeStamp;

    public LocalDateTime getJoinTimeStamp() {
        return joinTimeStamp;
    }

    public void setJoinTimeStamp(LocalDateTime joinTimeStamp) {
        this.joinTimeStamp = joinTimeStamp;
    }

    // Конструкторы, геттеры и сеттеры
    public ChatDescr() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
