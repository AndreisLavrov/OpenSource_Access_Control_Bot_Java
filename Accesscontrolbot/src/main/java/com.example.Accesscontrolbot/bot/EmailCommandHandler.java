package com.example.Accesscontrolbot.bot;

import com.example.Accesscontrolbot.model.User;
import com.example.Accesscontrolbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

@Component
public class EmailCommandHandler {
    private final AdminBot bot;

    @Autowired
    private UserRepository userRepository;

    private final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,6})$");
    private Timer emailTimer;

    public EmailCommandHandler(AdminBot bot) {
        this.bot = bot;
    }

    public void handleEmailCommand(Long chatId) {
        var text = "Пожалуйста, введите ваш корпоративный электронный адрес в формате 'example@domain.com'";
        sendMessage(chatId, text);

        emailTimer = new Timer();
        emailTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                var timeoutMessage = "Время для ввода электронной почты истекло.\nПопробуйте заново /email";
                sendMessage(chatId, timeoutMessage);
            }
        }, 60000); // 60000 миллисекунд = 1 минута
    }

    public void processEmail(Long chatId, String email, String username) {
        emailTimer.cancel();
        if (EMAIL_PATTERN.matcher(email).matches()) {
            var confirmationText = "Ваш электронный адрес успешно сохранен: " + email;
            sendMessage(chatId, confirmationText);
            saveUser(email, username);
        } else {
            var errorText = "Это не похоже на корректный электронный адрес. Пожалуйста, попробуйте еще раз.\n/email";
            sendMessage(chatId, errorText);
        }
    }

//    public void processEmail(Long chatId, String email) {
//        emailTimer.cancel();
//        if (EMAIL_PATTERN.matcher(email).matches()) {
//            var confirmationText = "Ваш электронный адрес успешно сохранен: " + email;
//            sendMessage(chatId, confirmationText);
//        } else {
//            var errorText = "Это не похоже на корректный электронный адрес. Пожалуйста, попробуйте еще раз.\n/email";
//            sendMessage(chatId, errorText);
//        }
//    }


    private void saveUser(String email, String username) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        userRepository.save(user);
    }

    private void sendMessage(Long chatId, String text) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(text);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            // Обработка ошибок при отправке сообщения в Telegram
            e.printStackTrace();
        }
    }
}
