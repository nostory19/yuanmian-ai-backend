package com.dong.yuanmianai.ai.mcp;

import org.springframework.stereotype.Component;

/**
 * MCP 上下文占位，后续可对接真实 MCP Server
 */
@Component
public class McpContextProvider {

    public String buildContext(String jobRole) {
        return "MCP_CONTEXT: role=" + jobRole;
    }
}
