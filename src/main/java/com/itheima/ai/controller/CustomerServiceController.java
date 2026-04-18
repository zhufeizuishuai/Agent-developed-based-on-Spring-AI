package com.itheima.ai.controller;

import com.itheima.ai.repository.InMemoryChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

//@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class CustomerServiceController {

    private final ChatClient serviceChatClient;
    private final InMemoryChatHistoryRepository chatHistoryRepository;

    public  CustomerServiceController(@Qualifier("serviceChatClient") ChatClient serviceChatClient,
                          InMemoryChatHistoryRepository chatHistoryRepository) {
        this.serviceChatClient = serviceChatClient;
        this.chatHistoryRepository = chatHistoryRepository;
    }
    @RequestMapping(value = "/service",produces = "text/html;charset=utf-8")
    public Flux<String> service(String prompt,String chatId){
        //保存会话ID
        chatHistoryRepository.save("service",chatId);
        return serviceChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId.trim()))
                .stream()
                .content();
    }
}
