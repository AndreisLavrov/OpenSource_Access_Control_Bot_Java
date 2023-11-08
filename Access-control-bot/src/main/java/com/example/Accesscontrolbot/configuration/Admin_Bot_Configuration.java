package com.example.Accesscontrolbot.configuration;

import com.example.Accesscontrolbot.Bot.Admin_Bot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class Admin_Bot_Configuration {

    @Bean
    public TelegramBotsApi telegramBotsApi(Admin_Bot admin_Bot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(admin_Bot);
        return api;
    }

}