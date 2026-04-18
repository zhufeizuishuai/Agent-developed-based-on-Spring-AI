package com.itheima.ai.controller;


import com.itheima.ai.repository.InMemoryChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RestController
@RequestMapping("/ai")
public class gameController {
    private final ChatClient gameChatClient;
    private final InMemoryChatHistoryRepository chatHistoryRepository;
    public gameController(@Qualifier("gamechatClient") ChatClient gameChatClient,
                          InMemoryChatHistoryRepository chatHistoryRepository) {
        this.gameChatClient = gameChatClient;
        this.chatHistoryRepository = chatHistoryRepository;
    }
    @RequestMapping(value = "/game",produces = "text/html;charset=utf-8")
    public Flux<String> chat(String prompt, String chatId){
        //保存会话 ID
        chatHistoryRepository.save("game",chatId);
        return gameChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId.trim()))
                .stream()
                .content();
    }
}
