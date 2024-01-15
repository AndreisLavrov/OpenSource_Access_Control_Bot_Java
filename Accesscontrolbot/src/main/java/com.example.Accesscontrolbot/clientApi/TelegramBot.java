package com.example.Accesscontrolbot.clientApi;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;



public class TelegramBot {
    private final Client client;
    private final String apiId;
    private final String apiHash;
    private final String phoneNumber;

    public TelegramBot(String apiId, String apiHash, String phoneNumber) {
        this.apiId = apiId;
        this.apiHash = apiHash;
        this.phoneNumber = phoneNumber;
        this.client = Client.create(new UpdateHandler(), null, null);
    }

    public void start() {
        client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
    }

    private class UpdateHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
            switch (object.getConstructor()) {
                case TdApi.UpdateNewChat.CONSTRUCTOR:
                    onNewChat((TdApi.UpdateNewChat) object);
                    break;
                case TdApi.UpdateChatMember.CONSTRUCTOR:
                    onChatMemberUpdated((TdApi.UpdateChatMember) object);
                    break;
            }
        }

        private void onNewChat(TdApi.UpdateNewChat update) {
            TdApi.Chat chat = update.chat;
            System.out.println("Бот был добавлен в чат с ID: " + chat.id);
            getChatMembers(chat.id);
        }

        private void onChatMemberUpdated(TdApi.UpdateChatMember update) {
            if (update.newChatMember.status.getConstructor() == TdApi.ChatMemberStatusMember.CONSTRUCTOR) {
                System.out.println("Новый участник добавлен в чат: " + update.newChatMember.memberId);
            }
        }

        private void getChatMembers(long chatId) {
            client.send(new TdApi.GetSupergroupMembers((int) chatId, null, 0, 200), response -> {
                if (response.getConstructor() == TdApi.ChatMembers.CONSTRUCTOR) {
                    for (TdApi.ChatMember member : ((TdApi.ChatMembers) response).members) {
                        System.out.println("Участник чата: ID " + member.memberId);
                    }
                }
            });
        }
    }

    private class AuthorizationRequestHandler implements Client.ResultHandler {
        @Override
        public void onResult(TdApi.Object object) {
        }
    }

    public static void main(String[] args) {
        String apiId = "28735038";
        String apiHash = "2d057b2b9b1dbe212af5395b79e004de";
        String phoneNumber = "79867824778";

        TelegramBot listener = new TelegramBot(apiId, apiHash, phoneNumber);
        listener.start();
    }
}