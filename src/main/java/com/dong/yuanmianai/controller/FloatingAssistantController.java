package com.dong.yuanmianai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
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
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * 全局悬浮助手：仅转发至 Agent 的 /assistant（单 LLM），不落库，与 /ai_assistant（/agent 面试图）隔离。
 */
@RestController
@RequestMapping("/floating_assistant")
public class FloatingAssistantController {

    @Resource
    private AgentProxyService agentProxyService;

    @Resource
    private UserService userService;

    // 测试agent的chat-stream接口，可以看见agnet还返回了一些其他元信息，所以前端只能等到所有内容生成后才显示，
    // TODO 修改对应的agent的显示
    @PostMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SaCheckPermission("ai:chat")
    public ResponseEntity<Flux<ServerSentEvent<String>>> chatStream(@RequestBody AiAssistantChatRequest chatRequest,
                                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(chatRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        chatRequest.setUserId(String.valueOf(loginUser.getId()));
        if (chatRequest.getSessionId() == null || chatRequest.getSessionId().isBlank()) {
            chatRequest.setSessionId("float_sess_" + UUID.randomUUID());
        }

        Flux<ServerSentEvent<String>> flux = agentProxyService.proxyFloatingAssistantStream(chatRequest)
                .map(data -> ServerSentEvent.<String>builder().data(data).build())
                .onErrorResume(e -> Flux.just(
                        ServerSentEvent.<String>builder().event("error").data("助手请求失败: " + e.getMessage()).build()
                ));

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .cacheControl(CacheControl.noStore())
                .header("X-Accel-Buffering", "no")
                .header("Connection", "keep-alive")
                .body(flux);
    }

    @PostMapping("/chat")
    @SaCheckPermission("ai:chat")
    public BaseResponse<AiAssistantChatVO> chat(@RequestBody AiAssistantChatRequest chatRequest,
                                               HttpServletRequest request) {
        ThrowUtils.throwIf(chatRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        chatRequest.setUserId(String.valueOf(loginUser.getId()));
        if (chatRequest.getSessionId() == null || chatRequest.getSessionId().isBlank()) {
            chatRequest.setSessionId("float_sess_" + UUID.randomUUID());
        }
        AiAssistantChatVO resp = agentProxyService.proxyFloatingAssistantChat(chatRequest);
        return ResultUtils.success(resp);
    }
}
