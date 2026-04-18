package com.itheima.ai.controller;

import com.itheima.ai.repository.InMemoryChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.itheima.ai.entity.vo.Result;
import com.itheima.ai.repository.FileRepository;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/pdf")
public class PdfController {

    private final FileRepository fileRepository;
    private final VectorStore vectorStore;
    private final ChatClient pdfChatClient;
    private final InMemoryChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "/chat",produces = "text/html;charset=utf-8")
    public Flux<String> chat(String prompt, String chatId){
        //1.找到会话文件
        Resource file = fileRepository.getFile(chatId);
        if (file == null) {
            return Flux.just("请先上传PDF文件！");
        }
        //保存会话ID
        chatHistoryRepository.save("pdf",chatId);
        return pdfChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId.trim()))
                .advisors(a -> a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION,"file_name == '"+file.getFilename()+"'"))
                .stream()
                .content();
    }
    /**
     * PDF文件上传接口
     * @param chatId 会话ID
     * @param file 上传的PDF文件
     * @return 响应结果
     */
    @RequestMapping("/upload/{chatId}")
    public Result uploadPdf(@PathVariable String chatId, @RequestParam("file") MultipartFile file) {
        try {
            // 1. 校验文件是否为PDF格式
            if (!Objects.equals(file.getContentType(), "application/pdf")) {
                return Result.fail("只能上传PDF文件！");
            }
            // 2. 保存文件
            boolean success = fileRepository.save(chatId, file.getResource());
            if (!success) {
                return Result.fail("保存文件失败！");
            }
            // 3. 写入向量库
            this.writeToVectorStore(file.getResource());
            return Result.ok();
        } catch (Exception e) {
            log.error("Failed to upload PDF.", e);
            return Result.fail("上传文件失败！");
        }
    }

    /**
     * 根据会话ID下载PDF文件
     * @param chatId 会话ID
     * @return 文件响应体
     * @throws IOException 流操作异常
     */
    @GetMapping("/file/{chatId}")
    public ResponseEntity<Resource> download(@PathVariable("chatId") String chatId) throws IOException {
        // 1. 读取文件
        Resource resource = fileRepository.getFile(chatId);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        // 2. 文件名编码，写入响应头
        String filename = URLEncoder.encode(Objects.requireNonNull(resource.getFilename()), StandardCharsets.UTF_8);
        // 3. 返回文件
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    /**
     * 将PDF内容写入向量库
     * @param resource PDF文件资源
     */
    private void writeToVectorStore(Resource resource) {
        // 1. 创建PDF的读取器
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1) // 每1页PDF作为一个Document
                        .build()
        );
        // 2. 读取PDF文档，拆分为Document
        List<Document> documents = reader.read();
        // 3. 写入向量库
        vectorStore.add(documents);
    }
}