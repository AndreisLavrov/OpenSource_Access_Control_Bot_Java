package com.example.Accesscontrolbot.bot;


import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


@Component
public class AdminBot extends TelegramLongPollingBot {

//    @Value("${bot.username}")
//    private String username;

//    @Value("${bot.token}")
//    private String token;
    public AdminBot() {
        super("6656464254:AAHszSvOiVgD0L7J1XGb3KkBCYe2WwuRPVU");
    }
    
    @Override
    public String getBotUsername() {
        return "AdminBot";
    }
    private static final Logger LOG = LoggerFactory.getLogger(AdminBot.class);

    private static final String START = "/start";
    private static final String HELP = "/help";



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
            case HELP -> helpCommand(chatId);
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

    private void helpCommand(Long chatId) {
        var text = """
                Пишите @andrei_lavrov
                """;
        sendMessage(chatId, text);
    }

    private void sendWelcomeMessage(String chatId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text("Нажмите здесь")
                .callbackData("welcome_button")
                .build();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> keyboardRow = new ArrayList<>();
        keyboardRow.add(button);
        keyboard.add(keyboardRow);

        inlineKeyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Добро пожаловать в чат! Пожалуйста, нажмите на кнопку ниже.");
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(message); // Отправка сообщения с кнопкой
        } catch (TelegramApiException e) {
            e.printStackTrace();
            LOG.error("Ошибка отправки приветственного сообщения", e);
        }
    }

    private void handleCallback(CallbackQuery answerCallbackQuery) {
        // Идентификатор сообщения и чата для редактирования
        answerCallbackQuery.getMessage().getMessageId();
        answerCallbackQuery.getMessage().getChatId();

        // Обработка нажатия на кнопку
        if (answerCallbackQuery.getData().equals("welcome_button")) {
            sendPrivateMessage(answerCallbackQuery.getFrom().getId().toString());
        }
    }

    private void sendPrivateMessage(String userId) {
        SendMessage message = new SendMessage();
        message.setChatId(userId);
        message.setText("Спасибо за нажатие на кнопку!");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
            LOG.error("Ошибка отправки личного сообщения", e);
        }
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

