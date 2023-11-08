package com.example.Accesscontrolbot.Bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class Admin_Bot extends TelegramLongPollingBot {

    public Admin_Bot(@Value("${bot.toke}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        
    }

    @Override
    public String getBotUsername() {
        return "OS_Telegram_Bot";
    }

}

