package com.example.Accesscontrolbot.bot;


import lombok.SneakyThrows;
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
import java.util.regex.Pattern;


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
    private static final String EMAIL = "/email";


    @SneakyThrows
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

            case EMAIL -> emailCommand(chatId);
            default -> {
                if (EMAIL_PATTERN.matcher(message).matches()) {
                    // Обработка корректного электронного адреса
                    var confirmationText = "Ваш электронный адрес успешно сохранен: " + message;
                    sendMessage(chatId, confirmationText);
                } else {
                    // Ответить на несоответствующие сообщения
                    var errorText = "Это не похоже на корректный электронный адрес. Попробуйте еще раз.";
                    sendMessage(chatId, errorText);
                }
            }
        }

    }

    private void startCommand(Long chatId, String userName) {
        var text = """
                Добро пожаловать в бот, %s!
                
                Другие команды:
                /email - отправить корпоративную почту
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

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,6})$");

    private void emailCommand(Long chatId) {
        var text = "Пожалуйста, введите ваш корпоративный электронный адрес в формате 'example@domain.com'";
        sendMessage(chatId, text);
    }


    private void sendWelcomeMessage(String chatId) throws TelegramApiException {

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

//    private void sendPrivateMessage(String userId) {
//        SendMessage message = new SendMessage();
//        message.setChatId(userId);
//        message.setText("Спасибо за нажатие на кнопку!");
//        try {
//            execute(message);
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//            LOG.error("Ошибка отправки личного сообщения", e);
//        }
//    }

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

