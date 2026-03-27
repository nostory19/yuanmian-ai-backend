package com.dong.yuanmianai.controller;

import com.dong.yuanmianai.common.BaseResponse;
import com.dong.yuanmianai.common.ErrorCode;
import com.dong.yuanmianai.common.ResultUtils;
import com.dong.yuanmianai.exception.ThrowUtils;
import com.dong.yuanmianai.model.dto.assistant.AiAssistantChatRequest;
import com.dong.yuanmianai.model.entity.User;
import com.dong.yuanmianai.model.vo.assistant.AiAssistantChatVO;
import com.dong.yuanmianai.service.AgentProxyService;
import com.dong.yuanmianai.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

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

    /**
     * 流式对话（SSE）
     */
    @PostMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@RequestBody AiAssistantChatRequest chatRequest,
                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(chatRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null && loginUser.getId() != null) {
            chatRequest.setUserId(String.valueOf(loginUser.getId()));
        }
        return agentProxyService.proxyChatStream(chatRequest)
                .map(data -> ServerSentEvent.<String>builder().data(data).build())
                .onErrorResume(e -> Flux.just(
                        ServerSentEvent.<String>builder().event("error").data("Agent 请求失败: " + e.getMessage()).build()
                ));
    }

    /**
     * 非流式对话
     */
    @PostMapping("/chat")
    public BaseResponse<AiAssistantChatVO> chat(@RequestBody AiAssistantChatRequest chatRequest,
                                                HttpServletRequest request) {
        ThrowUtils.throwIf(chatRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null && loginUser.getId() != null) {
            chatRequest.setUserId(String.valueOf(loginUser.getId()));
        }
        return ResultUtils.success(agentProxyService.proxyChat(chatRequest));
    }
}
