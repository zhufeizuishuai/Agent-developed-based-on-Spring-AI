package com.itheima.ai.controller;

import com.itheima.ai.repository.InMemoryChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;
    private final InMemoryChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "/chat",produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt,
                             @RequestParam("prompt") String chatId,
                             @RequestParam(value = "files",required = false)List<MultipartFile> files){
        //保存会话ID
        chatHistoryRepository.save("chat",chatId);
        if(files==null||files.isEmpty()) {
            return textChat(prompt,chatId);
        }
        else {
            return multiModalChat(prompt,chatId,files);
        }

    }

    private Flux<String> multiModalChat(String prompt, String chatId, List<MultipartFile> files) {
        //1.解析多媒体
        List<Media> mediaList = files.stream()
                .map(file->new Media(
                        MediaType.valueOf(file.getContentType()),
                        file.getResource())).toList();
        return chatClient.prompt()
                .user(p->p.text(prompt).media(mediaList.toArray(Media[]::new)))
                .advisors(a -> a.param(CONVERSATION_ID, chatId.trim()))
                .stream()
                .content();
    }

    private Flux<String> textChat(String prompt, String chatId) {
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId.trim()))
                .stream()
                .content();
    }
}
