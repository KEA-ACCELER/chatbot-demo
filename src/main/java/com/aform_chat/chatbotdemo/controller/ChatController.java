package com.aform_chat.chatbotdemo.controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

import com.aform_chat.chatbotdemo.model.ChatMessage;

import org.springframework.web.client.RestTemplate;

@Controller
public class ChatController {

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Value("${chatbot.server.url}")
    private String chatbotServerUrl;

    @MessageMapping("/chat/sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        messagingTemplate.convertAndSend("/topic/public", chatMessage);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HashMap<String, String> body = new HashMap<String, String>();
        body.put("msg", chatMessage.getContent());

        HttpEntity<HashMap<String, String>> request = new HttpEntity<>(body, headers);
        // Post Request
        HttpEntity<String> response = restTemplate.postForEntity(chatbotServerUrl, request, String.class);

        ChatMessage responseMessage = ChatMessage.builder()
                .type(ChatMessage.MessageType.CHAT)
                .sender("Simsimi")
                .content(response.getBody())
                .build();
        return responseMessage;
    }

    @MessageMapping("/chat/addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

}