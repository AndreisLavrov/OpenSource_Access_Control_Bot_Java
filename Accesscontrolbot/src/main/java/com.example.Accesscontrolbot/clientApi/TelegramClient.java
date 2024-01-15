package com.example.Accesscontrolbot.clientApi;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TelegramClient {

    private static final String BASE_URL = "http://127.0.0.1:8000"; // URL вашего API

    public static void getChatMembers(long chatId) { // Используем тип long для chatId
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/chat_members/" + chatId))
                .GET() // GET request
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println)
                .join(); // Wait for the response
    }

    public static void main(String[] args) {
        long chatId = -1002097459947L; // Используем тип long и суффикс L для литерала
        getChatMembers(chatId);
    }
}

////
//package com.example.Accesscontrolbot.clientApi;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//
//@SpringBootApplication
//public class TelegramClient  {
//
//    public static void main(String[] args) {
//        SpringApplication.run(TelegramClient.class, args);
//    }
//}
//
//@RestController
//class ChatController {
//
//    private static final String BASE_URL = "http://127.0.0.1:8000";
//
//    @PostMapping("/chat_id")
//    public ResponseEntity<String> receiveChatId(@RequestBody String chatId) {
//        System.out.println("Полученный chatId: " + chatId);
//        getChatMembers(Long.parseLong(chatId.trim()));
//        return ResponseEntity.ok("Chat ID принят: " + chatId);
//    }
//
//    public static void getChatMembers(long chatId) {
//        HttpClient client = HttpClient.newHttpClient();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(URI.create(BASE_URL + "/chat_members/" + chatId))
//                .GET()
//                .build();
//
//        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
//                .thenApply(HttpResponse::body)
//                .thenAccept(System.out::println)
//                .join();
//    }
//}
