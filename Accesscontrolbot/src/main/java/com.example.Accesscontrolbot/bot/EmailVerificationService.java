package com.example.Accesscontrolbot.bot;

import com.example.Accesscontrolbot.model.User;
import com.example.Accesscontrolbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailVerificationService {
    private static final Logger LOG = LoggerFactory.getLogger(EmailVerificationService.class);

    private final AdminBot bot;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private UserRepository userRepository;

    public EmailVerificationService(AdminBot bot, JavaMailSender mailSender) {
        this.bot = bot;
        this.mailSender = mailSender;
    }


    public void initiateVerification(Long userId, Long chatId) {
        LOG.info("Запуск процесса верификации для пользователя с ID: " + userId);
        Optional<User> userOptional = userRepository.findByUsername(userId.toString());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String userEmail = user.getEmail();

            // Генерация уникального кода верификации
            String verificationCode = UUID.randomUUID().toString();

            LOG.info("Отправка кода верификации на email: " + userEmail);
            // Построение и отправка email с кодом верификации
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("NoReply");
            message.setText("Это ваш уникальный код верификации: " + verificationCode +
                    "\nПожалуйста, передайте этот код боту для завершения верификации.");

            mailSender.send(message);

            LOG.info("Код верификации успешно отправлен на email: " + userEmail);

            // Возвращаем сообщение об успешной отправке
            String responseMessage = "На вашу почту " + userEmail + " был отправлен код верификации";
            sendResponse(chatId, responseMessage);
        } else {
            LOG.error("Пользователь с ID " + userId + " не найден.");
            // Пользователь не найден
            String responseMessage = "Пользователь не найден.";
            sendResponse(chatId, responseMessage);
        }
    }

    private void sendResponse(Long chatId, String text) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(text);
        try {
            bot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
