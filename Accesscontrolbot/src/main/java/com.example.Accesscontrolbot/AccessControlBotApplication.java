package com.example.Accesscontrolbot;

import com.example.Accesscontrolbot.bot.AdminBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


@EntityScan(basePackages = {"com.example.Accesscontrolbot.model"})
@EnableJpaRepositories(basePackages = {"com.example.Accesscontrolbot.repository"})



@SpringBootApplication
@EnableScheduling
public class AccessControlBotApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(AccessControlBotApplication.class, args);
		AdminBot bot = context.getBean(AdminBot.class);

		try {
			TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
			botsApi.registerBot(bot);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}
	}
}

