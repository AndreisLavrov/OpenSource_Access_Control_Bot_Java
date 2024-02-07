package com.example.Accesscontrolbot.bot;

import com.example.Accesscontrolbot.model.User;
import com.example.Accesscontrolbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;


import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


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

    @Autowired
    @Lazy
    private UserStateService userStateService;
    private final Map<Long, Integer> verificationCodes = new ConcurrentHashMap<>();



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

            SecureRandom random = new SecureRandom();
            int verificationCode = random.nextInt(900000) + 100000;
            LOG.info("Отправка кода верификации на email: " + userEmail);


            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("No-Reply");
            message.setText("Это ваш уникальный код верификации: " + verificationCode +
                    "\nПожалуйста, передайте этот код боту для завершения верификации.");

            mailSender.send(message);

            verificationCodes.put(chatId, verificationCode);
            userStateService.setUserState(chatId, UserStateService.UserState.AWAITING_VERIFICATION_CODE);


            LOG.info("Код верификации успешно отправлен на email: " + userEmail);

            // Возвращаем сообщение об успешной отправке
            String responseMessage = "На вашу почту " + userEmail + " был отправлен код верификации";
            sendResponse(chatId, responseMessage);
            sendResponse(chatId, "Пожалуйста, введите ваш код верификации.");
        } else {
            LOG.error("Пользователь с ID " + userId + " не найден.");
            // Пользователь не найден
            String responseMessage = "Пользователь не найден.";
            sendResponse(chatId, responseMessage);
        }
    }

    public void verifyCode(Long chatId, int userCode) {
        // Retrieve the expected code for the given chatId
        Integer expectedCode = verificationCodes.get(chatId);

        // Check if the provided code matches the expected code
        if (expectedCode != null && expectedCode.equals(userCode)) {
            // Verification successful
            sendResponse(chatId, "Верификация успешна!");
            // You might want to remove the code after successful verification
            verificationCodes.remove(chatId);
        } else {
            // Verification failed
            sendResponse(chatId, "Неверный код верификации.");
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
