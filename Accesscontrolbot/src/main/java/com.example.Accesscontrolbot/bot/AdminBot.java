package com.example.Accesscontrolbot.bot;


import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.beans.factory.annotation.Value;


import java.util.ArrayList;
import java.util.List;


@Component
public class AdminBot extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String botToken;
    public AdminBot() {
        super();
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {

        return "AdminBot";
    }

    private static final Logger LOG = LoggerFactory.getLogger(AdminBot.class);

    private static final String START = "/start";
    private static final String HELP = "/help";
    private static final String EMAIL = "/email";

    private boolean isWaitingForEmail = false;

    private final EmailCommandHandler emailCommandHandler = new EmailCommandHandler(this);



    @SneakyThrows
    @Override

    public void onUpdateReceived(Update update) {
        // Проверяем, есть ли сообщение и новый участник в чате
        if (update.hasMessage() && update.getMessage().getNewChatMembers() != null && !update.getMessage().getNewChatMembers().isEmpty()) {
            sendWelcomeMessage(update.getMessage().getChatId().toString());
        }

        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        }

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        if (update.hasMessage() && ("group".equals(update.getMessage().getChat().getType()) || "supergroup".equals(update.getMessage().getChat().getType()) || "channel".equals(update.getMessage().getChat().getType()))) {
            return;
        }

        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        String username = update.getMessage().getFrom().getId().toString();

        if (isWaitingForEmail) {
            emailCommandHandler.processEmail(chatId, message, username);
            isWaitingForEmail = false;
            return;
        }


        switch (message) {
            case START -> {
                String userName = update.getMessage().getChat().getUserName();
                startCommand(chatId, userName);
            }
            case HELP -> helpCommand(chatId);
            case EMAIL -> {
//                emailCommand(chatId);
//                isWaitingForEmail = true;
                emailCommandHandler.handleEmailCommand(chatId);
                isWaitingForEmail = true;
            }
            default -> {
                var defaultMessage = "Я не понимаю эту команду. Пожалуйста, используйте /start, /help или /email.";
                sendMessage(chatId, defaultMessage);
            }
        }
    }

    private void startCommand(Long chatId, String userName) {
        String responseText = String.format("Добро пожаловать в бот, %s!\n", userName);
        responseText += "\nДругие команды:\n/email - отправить корпоративную почту\n/help - получение справки";
        sendMessage(chatId, responseText);

        emailCommandHandler.handleEmailCommand(chatId);
        isWaitingForEmail = true; // Установка флага, что мы ожидаем ввода email
    }

    private void helpCommand(Long chatId) {
        var text = """
                Пишите @andrei_lavrov
                """;
        sendMessage(chatId, text);
    }

    private void sendWelcomeMessage(String chatId) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton startButton = InlineKeyboardButton.builder()
                .text("Начать чат с ботом")
                .url("https://t.me/OS_Access_Control_Bot?start")
                .build();

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> keyboardRow = new ArrayList<>();
        keyboardRow.add(startButton);
        keyboard.add(keyboardRow);

        inlineKeyboardMarkup.setKeyboard(keyboard);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Для начала общения с ботом, пожалуйста, нажмите на кнопку ниже. Затем в чате с ботом отправьте команду /start.");
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
        if (answerCallbackQuery.getData().equals("start_command")) {
            String chatId = answerCallbackQuery.getMessage().getChatId().toString();
            String userName = answerCallbackQuery.getFrom().getUserName();
            startCommand(Long.valueOf(chatId), userName);
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

