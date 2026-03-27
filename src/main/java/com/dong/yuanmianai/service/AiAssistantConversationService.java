package com.dong.yuanmianai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dong.yuanmianai.model.dto.assistant.AiAssistantSessionQueryRequest;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.vo.assistant.AiAssistantMessageVO;
import com.dong.yuanmianai.model.vo.assistant.AiAssistantSessionVO;

import java.util.List;

public interface AiAssistantConversationService {

    String ensureSession(String sessionId, User user, String firstQuestion);

    void saveUserMessage(String sessionId, User user, String content);

    void saveAssistantMessage(String sessionId, User user, String content);

    Page<AiAssistantSessionVO> listSessions(AiAssistantSessionQueryRequest request, User user);

    List<AiAssistantMessageVO> getHistory(String sessionId, User user);
}
