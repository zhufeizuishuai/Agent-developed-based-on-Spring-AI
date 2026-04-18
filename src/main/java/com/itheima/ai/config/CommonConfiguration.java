package com.itheima.ai.config;


import com.itheima.ai.constants.SystemConstants;
import com.itheima.ai.tool.CourseTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CommonConfiguration {
    @Bean("chatMemory")
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                // 对话存储的repository存储库层的实现方式，如果不配置，默认也是 Spring 提供的 InMemoryChatMemoryRepository
                .chatMemoryRepository(new InMemoryChatMemoryRepository()) // 有默认
                .maxMessages(20) // 最大消息数
                .build();

    }

    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel)
                .build();
    }
    @Bean("chatClient")
    public ChatClient chatClient(OpenAiChatModel model) {

        return ChatClient
                .builder(model)
                .defaultOptions(ChatOptions.builder().model("qwen-omni-turbo").build())
                .defaultSystem("你是一个热心、可爱的智能助手，你的名字叫小团团，请以小团团的身份和语气回答问题。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
                .build();
    }

    @Bean("gamechatClient")
    public ChatClient gamechatClient(OpenAiChatModel model) {

        return ChatClient.builder(model)
                .defaultSystem(SystemConstants.GAME_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
                .build();
    }

    @Bean("serviceChatClient")
    public ChatClient serviceChatClient(OpenAiChatModel model,ChatMemory chatMemory, CourseTools courseTools) {

        return ChatClient.builder(model)
                .defaultSystem(SystemConstants.SERVICE_SYSTEM_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
                .defaultTools(courseTools)
                .build();
    }

    @Bean("pdfChatClient")
    public ChatClient pdfChatClient(OpenAiChatModel model,VectorStore vectorStore) {

        return ChatClient.builder(model)
                .defaultSystem("请根据上下文来回答问题，遇到上下文没有的问题，不要随意编造")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory()).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder().similarityThreshold(0.6).topK(2).build())
                                .build(),
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
                .build();
    }
}
