package com.itheima.ai.repository;

import java.util.List;

public interface ChatHistoryRepository {
    //保存会话历史
    void save(String type,String chatId);
    //获取会话Id列表
    List<String> getChatIds(String type);

}
