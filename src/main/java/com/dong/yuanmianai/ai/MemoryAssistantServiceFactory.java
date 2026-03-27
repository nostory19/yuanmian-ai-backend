package com.dong.yuanmianai.ai;

import com.dong.yuanmianai.ai.guardrail.InterviewGuardrail;
import com.dong.yuanmianai.ai.memory.SessionMemoryRegistry;
import com.dong.yuanmianai.ai.tool.InterviewTools;
import com.dong.yuanmianai.ai.truncator.PromptTruncator;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一管理多个 AgentService 的工厂
 */
@Component
public class MemoryAssistantServiceFactory {

    public enum AgentType {
        ASSISTANT,
        INTERVIEWER,
        EVALUATOR,
        FOLLOW_UP,
        REPORT
    }

    @Value("${ai.openai.api-key:}")
    private String apiKey;

    @Value("${ai.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String modelName;

    @Value("${ai.memory.max-messages:20}")
    private Integer maxMessages;

    @Resource
    private SessionMemoryRegistry sessionMemoryRegistry;

    @Resource
    private InterviewTools interviewTools;

    @Resource
    private InterviewGuardrail interviewGuardrail;

    @Resource
    private PromptTruncator promptTruncator;

    private final Map<String, Object> serviceCache = new ConcurrentHashMap<>();

    public AssistantService assistant(String sessionId) {
        return (AssistantService) getService(AgentType.ASSISTANT, sessionId);
    }

    public InterviewerAgentService interviewer(String sessionId) {
        return (InterviewerAgentService) getService(AgentType.INTERVIEWER, sessionId);
    }

    public EvaluatorAgentService evaluator(String sessionId) {
        return (EvaluatorAgentService) getService(AgentType.EVALUATOR, sessionId);
    }

    public FollowUpAgentService followUp(String sessionId) {
        return (FollowUpAgentService) getService(AgentType.FOLLOW_UP, sessionId);
    }

    public ReportAgentService report(String sessionId) {
        return (ReportAgentService) getService(AgentType.REPORT, sessionId);
    }

    public Object getService(AgentType agentType, String sessionId) {
        String safeSessionId = StringUtils.defaultIfBlank(sessionId, "default");
        String cacheKey = agentType.name() + ":" + safeSessionId;
        return serviceCache.computeIfAbsent(cacheKey, key -> build(agentType, safeSessionId));
    }

    public void clearSession(String sessionId) {
        if (StringUtils.isBlank(sessionId)) {
            return;
        }
        for (AgentType type : AgentType.values()) {
            serviceCache.remove(type.name() + ":" + sessionId);
        }
        sessionMemoryRegistry.clear(sessionId);
    }

    private Object build(AgentType agentType, String sessionId) {
        ChatModel model = buildModel();
        ChatMemory chatMemory = sessionMemoryRegistry.getOrCreate(sessionId, maxMessages);
        return switch (agentType) {
            case ASSISTANT -> AiServices.builder(AssistantService.class)
                    .chatModel(model)
                    .chatMemory(chatMemory)
                    .tools(interviewTools)
                    .build();
            case INTERVIEWER -> AiServices.builder(InterviewerAgentService.class)
                    .chatModel(model)
                    .chatMemory(chatMemory)
                    .tools(interviewTools)
                    .build();
            case EVALUATOR -> AiServices.builder(EvaluatorAgentService.class)
                    .chatModel(model)
                    .chatMemory(chatMemory)
                    .tools(interviewTools)
                    .build();
            case FOLLOW_UP -> AiServices.builder(FollowUpAgentService.class)
                    .chatModel(model)
                    .chatMemory(chatMemory)
                    .tools(interviewTools)
                    .build();
            case REPORT -> AiServices.builder(ReportAgentService.class)
                    .chatModel(model)
                    .chatMemory(chatMemory)
                    .tools(interviewTools)
                    .build();
        };
    }

    private ChatModel buildModel() {
        if (StringUtils.isBlank(apiKey)) {
            throw new IllegalStateException("请先配置 ai.openai.api-key");
        }
        String sanitizedBaseUrl = interviewGuardrail.sanitize(baseUrl);
        String sanitizedModel = promptTruncator.truncate(modelName, 128);
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(sanitizedBaseUrl)
                .modelName(sanitizedModel)
                .build();
    }
}
