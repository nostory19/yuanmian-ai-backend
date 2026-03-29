import request from '@/request'
import { getAccessToken } from '@/utils/auth'

export async function chat(body: API.AiAssistantChatRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseAiAssistantChatVO>('/ai_assistant/chat', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/**
 * 通过 fetch 调用 SSE（POST）
 */
export async function chatStream(
  body: API.AiAssistantChatRequest,
  onMessage: (chunk: string) => void,
  options?: { signal?: AbortSignal }
) {
  const accessToken = getAccessToken()
  const response = await fetch('http://localhost:8101/api/ai_assistant/chat-stream', {
    method: 'POST',
    cache: 'no-store',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
    },
    body: JSON.stringify(body),
    signal: options?.signal,
  })
  if (!response.ok || !response.body) {
    throw new Error(`流式请求失败: ${response.status}`)
  }
  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) {
      break
    }
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() ?? ''
    for (const line of lines) {
      if (line.startsWith('data:')) {
        const payload = line.slice(5).trim()
        if (payload !== '') {
          onMessage(payload)
        }
      }
    }
  }
}
