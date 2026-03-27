# yuanmian-ai-backend

`yuanmian-ai-backend` 是 **前端 - 后端 - Agent** 三层架构中的后端服务层，负责：

- 业务接口与数据持久化（MySQL / MyBatis-Plus）
- 用户体系与权限控制
- 对独立 `yuanmian-ai-agent` 的网关转发（普通对话 + SSE 流式对话）

> 当前架构已调整为：**AI 能力全部在独立 Agent 服务中实现**，后端不再内置 LangChain4j Agent 模块。

## 架构说明

```text
Frontend (yuanmian-ai-frontend)
        |
        | HTTP / SSE
        v
Backend (yuanmian-ai-backend)
  - /api/ai_assistant/chat
  - /api/ai_assistant/chat-stream
        |
        | HTTP / SSE proxy
        v
Agent (yuanmian-ai-agent)
  - /agent/chat
  - /agent/chat-stream
```

## 主要技术栈

- Spring Boot 3
- Spring Web + WebFlux（用于 Agent 网关转发 / SSE）
- MyBatis-Plus
- MySQL
- Redis（可选）
- Elasticsearch（可选）
- Knife4j / OpenAPI

## 关键接口

### AI 网关接口（前端调用）

- `POST /api/ai_assistant/chat`
- `POST /api/ai_assistant/chat-stream`

### 后端内部转发目标（Agent）

- `POST /agent/chat`
- `POST /agent/chat-stream`

## 快速启动

### 1. 数据库准备

修改 `src/main/resources/application.yml` 中数据库配置：

```yml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/yuanmian_db
    username: root
    password: 12345
```

执行建表脚本：

```bash
mysql -u root -p < sql/create_table.sql
```

### 2. 配置 Agent 地址

在 `application.yml` 中确认：

```yml
agent:
  base-url: http://localhost:8291
  connect-timeout-ms: 3000
  read-timeout-ms: 120000
```

### 3. 启动顺序

1. 启动 `yuanmian-ai-agent`
2. 启动 `yuanmian-ai-backend`
3. 启动前端 `yuanmian-ai-frontend`

## 运行与验证

启动后端：

```bash
mvn spring-boot:run
```

构建检查：

```bash
mvn -DskipTests compile
```

接口文档：

- [http://localhost:8101/api/doc.html](http://localhost:8101/api/doc.html)

## 目录补充说明

- `src/main/java/com/dong/yuanmianai/controller/AiAssistantController.java`  
  前端 AI 对话入口，统一接入后端。
- `src/main/java/com/dong/yuanmianai/service/impl/AgentProxyServiceImpl.java`  
  代理转发到独立 Agent 服务，处理普通/流式请求。
- `src/main/java/com/dong/yuanmianai/config/AgentProperties.java`  
  Agent 地址和超时配置。

## 备注

- 后端已移除内置 `ai` 包及 `langchain4j` 依赖，避免与独立 Agent 职责重叠。
- 如需扩展 AI 能力，请优先在 `yuanmian-ai-agent` 中实现，再由后端网关转发接入。
