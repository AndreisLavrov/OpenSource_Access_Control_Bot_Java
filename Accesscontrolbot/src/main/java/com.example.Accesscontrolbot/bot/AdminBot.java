package com.example.Accesscontrolbot.bot;

import lombok.SneakyThrows;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.beans.factory.annotation.Value;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class AdminBot extends TelegramLongPollingBot {

    @Value("${bot.token}")
    private String botToken;

    @Autowired
    @Lazy
    private EmailCommandHandler emailCommandHandler;

    @Autowired
    @Lazy
    private EmailVerificationService emailVerificationService;

    @Autowired
    @Lazy
    private UserStateService userStateService;

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
    private static final String VERIFICATION = "/verification";

    private final Map<Long, Boolean> userIsWaitingForEmail = new ConcurrentHashMap<>();


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


        if (userIsWaitingForEmail.getOrDefault(chatId, false)) {
            emailCommandHandler.processEmail(chatId, message, username);
            userIsWaitingForEmail.put(chatId, false); // Сброс флага после обработки email
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            if (userStateService.getUserState(chatId) == UserStateService.UserState.AWAITING_VERIFICATION_CODE) {
                try {
                    int verificationCode = Integer.parseInt(messageText);
                    emailVerificationService.verifyCode(chatId, verificationCode);
                    userStateService.clearUserState(chatId);
                } catch (NumberFormatException e) {
                    sendMessage(chatId, "Пожалуйста, введите ваш код верификации.");
                }
                return;
            }
        }


        switch (message) {
            case START -> {
                String firstName = update.getMessage().getFrom().getFirstName();
                String lastName = update.getMessage().getFrom().getLastName();
                startCommand(chatId, firstName, lastName);


            }
            case HELP -> helpCommand(chatId);
            case EMAIL -> {
                emailCommandHandler.handleEmailCommand(chatId);
                userIsWaitingForEmail.put(chatId, true);

            }
            case VERIFICATION -> {
                handleVerificationCommand(update.getMessage(), chatId);
                userStateService.setUserState(chatId, UserStateService.UserState.AWAITING_VERIFICATION_CODE);
            }

            default -> {
                if (!userIsWaitingForEmail.getOrDefault(chatId, false)) {
                    sendMessage(chatId, "Я не понимаю эту команду.\nПожалуйста, используйте /start, /help, /email или /verification.");
                }
            }
        }
    }


    private void startCommand(Long chatId, String firstName, String lastName) {
        String nameToUse = (firstName != null ? firstName : "") + (lastName != null ? " " + lastName : "");
        nameToUse = nameToUse.trim().isEmpty() ? "пользователь" : nameToUse.trim();

        String responseText = String.format("Добро пожаловать в бот, %s!\n", nameToUse);
        responseText += "Другие команды:\n/email - отправить корпоративную почту\n/help - получение справки\n/verification - приступить к верификации";
        sendMessage(chatId, responseText);
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

    private void handleCallback(CallbackQuery callbackQuery) {
        Long chatId = callbackQuery.getMessage().getChatId();

        if (callbackQuery.getData().equals("start_command")) {
            String firstName = callbackQuery.getFrom().getFirstName();
            String lastName = callbackQuery.getFrom().getLastName();
            startCommand(chatId, firstName, lastName);
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


    private void handleVerificationCommand(Message message, Long chatId) {
        Long userId = message.getFrom().getId();
        emailVerificationService.initiateVerification(userId, chatId);
    }

}

