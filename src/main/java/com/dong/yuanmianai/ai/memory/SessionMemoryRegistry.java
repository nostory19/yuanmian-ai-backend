package com.dong.yuanmianai.ai.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按会话隔离的记忆注册器
 */
@Component
public class SessionMemoryRegistry {

    private final Map<String, ChatMemory> memoryMap = new ConcurrentHashMap<>();

    public ChatMemory getOrCreate(String sessionKey, int maxMessages) {
        return memoryMap.computeIfAbsent(sessionKey, k -> MessageWindowChatMemory.withMaxMessages(maxMessages));
    }

    public void clear(String sessionKey) {
        memoryMap.remove(sessionKey);
    }
}
