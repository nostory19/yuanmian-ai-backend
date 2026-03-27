package com.dong.yuanmianai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dong.yuanmianai.mapper.AiAssistantChatMessageMapper;
import com.dong.yuanmianai.mapper.AiAssistantSessionMapper;
import com.dong.yuanmianai.model.dto.assistant.AiAssistantSessionQueryRequest;
import com.dong.yuanmianai.model.entity.AiAssistantChatMessage;
import com.dong.yuanmianai.model.entity.AiAssistantSession;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.vo.assistant.AiAssistantMessageVO;
import com.dong.yuanmianai.model.vo.assistant.AiAssistantSessionVO;
import com.dong.yuanmianai.service.AiAssistantConversationService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AiAssistantConversationServiceImpl implements AiAssistantConversationService {

    @Resource
    private AiAssistantSessionMapper sessionMapper;

    @Resource
    private AiAssistantChatMessageMapper messageMapper;

    @Override
    public String ensureSession(String sessionId, User user, String firstQuestion) {
        String sid = StringUtils.isBlank(sessionId) ? "sess_" + UUID.randomUUID() : sessionId;
        AiAssistantSession exist = sessionMapper.selectOne(new LambdaQueryWrapper<AiAssistantSession>()
                .eq(AiAssistantSession::getSessionId, sid)
                .eq(AiAssistantSession::getUserId, user.getId())
                .last("limit 1"));
        if (exist != null) {
            return sid;
        }
        AiAssistantSession session = new AiAssistantSession();
        session.setSessionId(sid);
        session.setUserId(user.getId());
        session.setUserName(StringUtils.defaultIfBlank(user.getUserName(), "用户" + user.getId()));
        session.setTitle(buildTitle(firstQuestion));
        session.setCreatedTime(new Date());
        session.setModifyTime(new Date());
        sessionMapper.insert(session);
        return sid;
    }

    @Override
    public void saveUserMessage(String sessionId, User user, String content) {
        saveMessage(sessionId, user, "user", content);
    }

    @Override
    public void saveAssistantMessage(String sessionId, User user, String content) {
        saveMessage(sessionId, user, "assistant", content);
    }

    @Override
    public Page<AiAssistantSessionVO> listSessions(AiAssistantSessionQueryRequest request, User user) {
        long current = request == null ? 1 : request.getCurrent();
        long pageSize = request == null ? 10 : request.getPageSize();
        Page<AiAssistantSession> page = sessionMapper.selectPage(
                new Page<>(current, pageSize),
                new LambdaQueryWrapper<AiAssistantSession>()
                        .eq(AiAssistantSession::getUserId, user.getId())
                        .orderByDesc(AiAssistantSession::getModifyTime)
        );
        Page<AiAssistantSessionVO> voPage = new Page<>(current, pageSize, page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(item -> {
            AiAssistantSessionVO vo = new AiAssistantSessionVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<AiAssistantMessageVO> getHistory(String sessionId, User user) {
        List<AiAssistantChatMessage> list = messageMapper.selectList(
                new LambdaQueryWrapper<AiAssistantChatMessage>()
                        .eq(AiAssistantChatMessage::getSessionId, sessionId)
                        .eq(AiAssistantChatMessage::getUserId, user.getId())
                        .orderByAsc(AiAssistantChatMessage::getCreateTime)
        );
        return list.stream().map(item -> {
            AiAssistantMessageVO vo = new AiAssistantMessageVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    private void saveMessage(String sessionId, User user, String role, String content) {
        if (StringUtils.isBlank(content)) {
            return;
        }
        AiAssistantChatMessage msg = new AiAssistantChatMessage();
        msg.setMsgId("msg_" + UUID.randomUUID());
        msg.setSessionId(sessionId);
        msg.setUserId(user.getId());
        msg.setUserName(StringUtils.defaultIfBlank(user.getUserName(), "用户" + user.getId()));
        msg.setRole(role);
        msg.setContent(content);
        msg.setTitle(buildTitle(content));
        msg.setCreateTime(new Date());
        msg.setModifyTime(new Date());
        messageMapper.insert(msg);
        sessionMapper.update(
                null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AiAssistantSession>()
                        .eq(AiAssistantSession::getSessionId, sessionId)
                        .eq(AiAssistantSession::getUserId, user.getId())
                        .set(AiAssistantSession::getModifyTime, new Date())
        );
    }

    private String buildTitle(String text) {
        if (StringUtils.isBlank(text)) {
            return "新会话";
        }
        String t = text.trim();
        return t.length() > 20 ? t.substring(0, 20) + "..." : t;
    }
}
