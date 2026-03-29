package com.dong.yuanmianai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.dong.yuanmianai.common.BaseResponse;
import com.dong.yuanmianai.common.ErrorCode;
import com.dong.yuanmianai.common.ResultUtils;
import com.dong.yuanmianai.exception.ThrowUtils;
import com.dong.yuanmianai.model.dto.assistant.AiAssistantChatRequest;
import com.dong.yuanmianai.model.dto.assistant.AiAssistantHistoryRequest;
import com.dong.yuanmianai.model.dto.assistant.AiAssistantSessionQueryRequest;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.vo.assistant.AiAssistantMessageVO;
import com.dong.yuanmianai.model.vo.assistant.AiAssistantChatVO;
import com.dong.yuanmianai.model.vo.assistant.AiAssistantSessionVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dong.yuanmianai.service.AiAssistantConversationService;
import com.dong.yuanmianai.service.AgentProxyService;
import com.dong.yuanmianai.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AI 助手接口（后端网关层）
 */
@RestController
@RequestMapping("/ai_assistant")
public class AiAssistantController {

    @Resource
    private AgentProxyService agentProxyService;

    @Resource
    private UserService userService;

    @Resource
    private AiAssistantConversationService conversationService;

    /**
     * 流式对话（SSE）：{@code Flux<ServerSentEvent<String>>}，与常见 Flux 写 SSE 的方式一致。
     * 拉取 Agent 须按 SSE 帧解码（见 {@link com.dong.yuanmianai.service.impl.AgentProxyServiceImpl}），勿对上游使用 {@code bodyToFlux(String.class)}，否则易整包缓冲。
     */
    @PostMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SaCheckPermission("ai:chat")
    public ResponseEntity<Flux<ServerSentEvent<String>>> chatStream(@RequestBody AiAssistantChatRequest chatRequest,
                                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(chatRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        chatRequest.setUserId(String.valueOf(loginUser.getId()));
        if (chatRequest.getSessionId() == null || chatRequest.getSessionId().isBlank()) {
            chatRequest.setSessionId("sess_" + UUID.randomUUID());
        }
        String sid = conversationService.ensureSession(chatRequest.getSessionId(), loginUser, chatRequest.getMessage());
        chatRequest.setSessionId(sid);
        conversationService.saveUserMessage(sid, loginUser, chatRequest.getMessage());

        List<String> assistantParts = new ArrayList<>();
        final String metaPrefix = "__AGENT_META__";
        Flux<ServerSentEvent<String>> flux = agentProxyService.proxyChatStream(chatRequest)
                .doOnNext(data -> {
                    if (data != null && !data.contains("[DONE]")
                            && !data.startsWith(metaPrefix)) {
                        assistantParts.add(data);
                    }
                })
                .doOnComplete(() -> conversationService.saveAssistantMessage(sid, loginUser, String.join("", assistantParts)))
                .map(data -> ServerSentEvent.<String>builder().data(data).build())
                .onErrorResume(e -> Flux.just(
                        ServerSentEvent.<String>builder().event("error").data("Agent 请求失败: " + e.getMessage()).build()
                ));
        // 避免缓存/代理把 SSE 攒成一整包；Chrome「Preview」仍可能结束时才刷新，要以 Timing/EventStream 为准
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .cacheControl(CacheControl.noStore())
                .header("X-Accel-Buffering", "no")
                .header("Connection", "keep-alive")
                .body(flux);
    }

    /**
     * 非流式对话
     */
    @PostMapping("/chat")
    @SaCheckPermission("ai:chat")
    public BaseResponse<AiAssistantChatVO> chat(@RequestBody AiAssistantChatRequest chatRequest,
                                                HttpServletRequest request) {
        ThrowUtils.throwIf(chatRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        chatRequest.setUserId(String.valueOf(loginUser.getId()));
        String sid = conversationService.ensureSession(chatRequest.getSessionId(), loginUser, chatRequest.getMessage());
        chatRequest.setSessionId(sid);
        conversationService.saveUserMessage(sid, loginUser, chatRequest.getMessage());
        AiAssistantChatVO resp = agentProxyService.proxyChat(chatRequest);
        conversationService.saveAssistantMessage(sid, loginUser, resp.getAnswer());
        resp.setSessionId(sid);
        return ResultUtils.success(resp);
    }

    /**
     * 获取会话列表
     */
    @PostMapping("/session_list")
    @SaCheckPermission("ai:session")
    public BaseResponse<Page<AiAssistantSessionVO>> sessionList(@RequestBody(required = false) AiAssistantSessionQueryRequest queryRequest,
                                                                HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(conversationService.listSessions(queryRequest, loginUser));
    }

    /**
     * 获取会话历史
     */
    @PostMapping("/get_history")
    @SaCheckPermission("ai:history")
    public BaseResponse<List<AiAssistantMessageVO>> getHistory(@RequestBody AiAssistantHistoryRequest historyRequest,
                                                               HttpServletRequest request) {
        ThrowUtils.throwIf(historyRequest == null || historyRequest.getSessionId() == null || historyRequest.getSessionId().isBlank(),
                ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(conversationService.getHistory(historyRequest.getSessionId(), loginUser));
    }
}
