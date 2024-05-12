package com.example.Accesscontrolbot.bot;

import com.example.Accesscontrolbot.model.ChatDescr;
import com.example.Accesscontrolbot.model.User;
import com.example.Accesscontrolbot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.example.Accesscontrolbot.model.DomainsList;
import com.example.Accesscontrolbot.repository.DomainsListRepository;
import com.example.Accesscontrolbot.repository.ChatDescrRepository;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Component
public class EmailCommandHandler {
    private final AdminBot bot;
    private final UserRepository userRepository;
    private final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,6})$");
    private Timer emailTimer;

    private final DomainsListRepository domainsListRepository;
    private final ChatDescrRepository chatDescrRepository;

    @Autowired
    public EmailCommandHandler(AdminBot bot, UserRepository userRepository, DomainsListRepository domainsListRepository, ChatDescrRepository chatDescrRepository) {
        this.bot = bot;
        this.userRepository = userRepository;
        this.domainsListRepository = domainsListRepository;
        this.chatDescrRepository = chatDescrRepository;
    }




    private Map<Long, Timer> emailSessions = new HashMap<>();
    private final Map<Long, Boolean> userIsWaitingForEmail = new ConcurrentHashMap<>();

    public void handleEmailCommand(Long chatId) {
        var text = "Пожалуйста, введите ваш корпоративный электронный адрес в формате 'example@domain.com'";
        sendMessage(chatId, text);

        emailTimer = new Timer();
        emailTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                var timeoutMessage = "Время для ввода электронной почты истекло.\nПопробуйте заново /email";
                sendMessage(chatId, timeoutMessage);
                userIsWaitingForEmail.put(chatId, false);
                emailSessions.remove(chatId);
            }
        }, 60000); // 60000 миллисекунд = 1 минута
        emailSessions.put(chatId, emailTimer);
    }

    public void processEmail(Long chatId, String email, String username) {
        Timer sessionTimer = emailSessions.remove(chatId); // Удаление сессии при обработке email
        if(sessionTimer != null) {
            sessionTimer.cancel();
        }
        if (EMAIL_PATTERN.matcher(email).matches()) {
            // Validate domain before saving
//            saveUser(chatId, email, username);
            // Find chat_id from ChatDescr using userId
            String domain = email.substring(email.indexOf("@") + 1);  // Extract domain

            // Find chat_id from ChatDescr using userId
            String userId = chatId.toString();
            Optional<ChatDescr> chatDescrOptional = chatDescrRepository.findByUserId(userId);

            if (chatDescrOptional.isPresent()) {
                ChatDescr chatDescr = chatDescrOptional.get();
                Long chatIdFromDescr = Long.parseLong(chatDescr.getChatId());

                // Find allowed domains from DomainsList using chatId
                Optional<DomainsList> domainsListOptional = domainsListRepository.findByChatId(chatIdFromDescr);
                if (domainsListOptional.isPresent()) {
                    DomainsList domainsList = domainsListOptional.get();
                    String allowedDomainsString = domainsList.getListOfDomains();

                    // Validate domain
                    if (allowedDomainsString.contains(domain)) {
                        saveUser(chatId, email, username);
                    } else {
                        sendMessage(chatId, "Введенный домен не входит в список разрешенных для вашего чата. Пожалуйста, попробуйте еще раз.\n/email");
                    }
                } else {
                    sendMessage(chatId, "Список разрешенных доменов не найден для этого чата.");
                }
//                // For Debugging/Logging: Print user and chat IDs
//                System.out.println("User ID: " + userId);
//                System.out.println("Chat ID: " + chatIdFromDescr);
            } else {
                sendMessage(chatId, "Информация о чате не найдена.");
            }
        } else {
            var errorText = "Это не похоже на корректный электронный адрес. Пожалуйста, попробуйте еще раз.\n/email";
            sendMessage(chatId, errorText);
        }
    }





    private void saveUser(Long chatId, String email, String username) {
        // Проверяем, существует ли уже пользователь с этой электронной почтой
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent() && !existingUser.get().getUsername().equals(username)) {
            sendMessage(chatId, "Этот электронный адрес уже зарегистрирован в системе. Пожалуйста, используйте другой адрес.");
        } else {
            try {
                User user = userRepository.findByUsername(username).orElse(new User());
                user.setEmail(email);
                user.setUsername(username);
                userRepository.save(user);
                sendMessage(chatId, "Ваш электронный адрес успешно сохранен: " + email);
            } catch (DataIntegrityViolationException e) {
                sendMessage(chatId, "Произошла ошибка при сохранении вашего электронного адреса. Возможно, он уже используется.");
            }
        }
    }




    private void sendMessage(Long chatId, String text) {
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