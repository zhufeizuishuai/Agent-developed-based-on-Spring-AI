package com.itheima.ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class HeimaAiApplicationTests {
    @Autowired
    private OpenAiEmbeddingModel openAiEmbeddingModel;
    @Autowired
    private VectorStore vectorStore;
    @Test
    void contextLoads() {
        float[] floats = openAiEmbeddingModel.embed("我爱Java");
        System.out.println(Arrays.toString(floats));
    }
     @Test
     void testVectorStore(){
        //1.创建pdf读取器
         Resource resource = new FileSystemResource("中二知识笔记.pdf");
         PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(
                 resource,
                 PdfDocumentReaderConfig.builder()
                         .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                         .withPagesPerDocument(1)
                         .build());

         //2.读取pdf文件
         List<Document> documents = pagePdfDocumentReader.read();
         //3.写入向量数据库
        vectorStore.add(documents);
         //4.搜索
         SearchRequest searchRequest = SearchRequest.builder()
                 .query("论语中的教育目的是什么")
                 .topK(1)
                 .similarityThreshold(0.5)
                 .filterExpression("file_name == '中二知识笔记.pdf'")
                 .build();
         List<Document> search = vectorStore.similaritySearch(searchRequest);
         for (Document document : search) {
             System.out.println(document.getId());
                 System.out.println(document.getScore());
                 System.out.println(document.getText());
         }
    }

}
