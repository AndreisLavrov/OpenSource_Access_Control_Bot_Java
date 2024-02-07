package com.example.Accesscontrolbot.bot;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserStateService {
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    enum UserState {
        AWAITING_VERIFICATION_CODE,
    }

    public void setUserState(Long userId, UserState state) {
        userStates.put(userId, state);
    }

    public UserState getUserState(Long userId) {
        return userStates.get(userId);
    }

    public void clearUserState(Long userId) {
        userStates.remove(userId);
    }
}
