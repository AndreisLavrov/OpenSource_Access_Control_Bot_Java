package com.example.Accesscontrolbot.Bot;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class Admin_Bot extends TelegramLongPollingBot {

    private static final Logger LOG = LoggerFactory.getLogger(Admin_Bot.class);

    private static final String START = "/start";
    private static final String HELP = "/help";


    @Value("${bot.username}")
    private String username;


    @Value("${bot.token}")
    private String token;

    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, есть ли сообщение и новый участник в чате
        if (update.hasMessage() && update.getMessage().getNewChatMembers() != null && !update.getMessage().getNewChatMembers().isEmpty()) {
            sendWelcomeMessage(update.getMessage().getChatId().toString());
        }

        // Если это callback от нажатия кнопки (реакция)
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }

        if(!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }
        var message = update.getMessage().getText();
        var chatId = update.getMessage().getChatId();
        switch (message) {
            case START -> {
                String userName = update.getMessage().getChat().getUserName();
                startCommand(chatId, userName);
            }
        }
    }

    private void startCommand(Long chatId, String userName) {
        var text = """
                Добро пожаловать в бот, %s!
                
                Другие команды:
                /help - получение справки
                """;
        var formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    private void sendWelcomeMessage(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Добро пожаловать в чат! Пожалуйста, нажмите на кнопку, чтобы получить приветственное сообщение.");
        // Здесь может быть добавление InlineKeyboardMarkup для кнопки

        try {
            execute(message); // Отправка сообщения
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleCallback(CallbackQuery callbackQuery) {
        // Идентификатор сообщения и чата для редактирования
        long messageId = callbackQuery.getMessage().getMessageId();
        long chatId = callbackQuery.getMessage().getChatId();

        // Если пользователь нажал на кнопку, отправляем ему личное сообщение
        if (callbackQuery.getData().equals("welcome_button")) {
            sendPrivateMessage(callbackQuery.getFrom().getId().toString());
        }
    }

    private void sendPrivateMessage(String userId) {
        SendMessage message = new SendMessage();
        message.setChatId(userId);
        message.setText("Привет!");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {

        return username;
    }

    @Override
    public String getBotToken() {

        return token;
    }

    private void sendMessage(Long chatId, String text) {
        var chatIdStr = String.valueOf(chatId);
        var sendMessage = new SendMessage(chatIdStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            LOG.error("Ошибка отправки сообщения", e);
        }
    }
}

