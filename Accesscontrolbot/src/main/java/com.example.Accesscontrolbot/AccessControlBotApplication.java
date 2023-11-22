package com.example.Accesscontrolbot;

import com.example.Accesscontrolbot.bot.AdminBot;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class AccessControlBotApplication {

	public static void main(String[] args) {

		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(new AdminBot());
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}

