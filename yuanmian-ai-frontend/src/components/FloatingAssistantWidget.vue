<template>
  <div class="floating-assistant">
    <Transition name="fade">
      <div v-if="expanded" class="panel" role="dialog" aria-label="悬浮助手">
        <div class="panel-header">
          <span class="panel-title">
            <RobotOutlined />
            助手
          </span>
          <div class="panel-actions">
            <a-button type="text" size="small" @click="clearChat">新对话</a-button>
            <a-button type="text" size="small" @click="expanded = false">
              <CloseOutlined />
            </a-button>
          </div>
        </div>
        <div ref="scrollRef" class="panel-messages">
          <div v-if="messages.length === 0" class="empty-hint">有问题尽管问，我会尽力简洁回答。</div>
          <div
            v-for="(m, idx) in messages"
            :key="idx"
            class="msg"
            :class="m.role === 'user' ? 'msg-user' : 'msg-assistant'"
          >
            <div class="msg-bubble">{{ m.content }}</div>
          </div>
          <div v-if="streamLoading" class="msg msg-assistant">
            <div class="msg-bubble typing">正在回复…</div>
          </div>
        </div>
        <div class="panel-input">
          <a-textarea
            v-model:value="input"
            :rows="3"
            placeholder="输入消息，Enter 发送（Shift+Enter 换行）"
            @press-enter.exact.prevent="sendStream"
          />
          <div class="input-row">
            <a-button :disabled="streamActive" @click="sendStream" type="primary" :loading="streamLoading && streamActive">
              发送
            </a-button>
            <a-button v-if="streamActive" danger @click="stopStream">停止</a-button>
          </div>
        </div>
      </div>
    </Transition>

    <button
      type="button"
      class="fab"
      :class="{ 'fab-active': expanded }"
      aria-label="打开助手"
      @click="toggle"
    >
      <RobotOutlined />
    </button>
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { CloseOutlined, RobotOutlined } from '@ant-design/icons-vue'
import { floatingChatStream } from '@/api/floatingAssistantController'
import { getAccessToken } from '@/utils/auth'

const expanded = ref(false)
const input = ref('')
const messages = ref<{ role: 'user' | 'assistant'; content: string }[]>([])
const sessionId = ref(`float_sess_${Date.now()}`)
const streamLoading = ref(false)
const streamActive = ref(false)
const scrollRef = ref<HTMLElement | null>(null)
let abortController: AbortController | null = null

function toggle() {
  expanded.value = !expanded.value
}

function clearChat() {
  messages.value = []
  input.value = ''
  sessionId.value = `float_sess_${Date.now()}`
}

async function scrollToBottom() {
  await nextTick()
  const el = scrollRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

watch(
  () => messages.value.length,
  () => {
    scrollToBottom()
  }
)

watch(
  () => messages.value[messages.value.length - 1]?.content,
  () => {
    scrollToBottom()
  }
)

function stopStream() {
  abortController?.abort()
  streamActive.value = false
  streamLoading.value = false
}

async function sendStream() {
  const text = input.value.trim()
  if (!text) {
    message.warning('请输入内容')
    return
  }
  if (!getAccessToken()) {
    message.warning('请先登录')
    return
  }
  streamActive.value = true
  streamLoading.value = true
  messages.value.push({ role: 'user', content: text })
  messages.value.push({ role: 'assistant', content: '' })
  input.value = ''
  abortController = new AbortController()
  const assistantIndex = messages.value.length - 1
  let firstChunk = false
  try {
    await floatingChatStream(
      { sessionId: sessionId.value, message: text },
      (chunk) => {
        if (chunk === '[DONE]') {
          return
        }
        if (!firstChunk && chunk) {
          firstChunk = true
          streamLoading.value = false
        }
        const cur = messages.value[assistantIndex]
        if (cur && cur.role === 'assistant') {
          cur.content += chunk
        }
      },
      { signal: abortController.signal }
    )
  } catch (e: unknown) {
    const err = e as { name?: string; message?: string }
    if (err?.name !== 'AbortError') {
      message.error(err?.message || '请求失败')
      const cur = messages.value[assistantIndex]
      if (cur && cur.role === 'assistant' && !cur.content) {
        cur.content = '（无法获取回复，请稍后重试）'
      }
    }
  } finally {
    streamActive.value = false
    streamLoading.value = false
    abortController = null
    await scrollToBottom()
  }
}

</script>

<style scoped>
.floating-assistant {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 1000;
  font-size: 14px;
}

.fab {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #fff;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.45);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.fab:hover {
  transform: scale(1.05);
  box-shadow: 0 10px 28px rgba(79, 70, 229, 0.55);
}

.fab-active {
  opacity: 0.85;
}

.panel {
  position: absolute;
  right: 0;
  bottom: 72px;
  width: min(420px, calc(100vw - 48px));
  height: min(520px, 70vh);
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(15, 23, 42, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  background: linear-gradient(90deg, #f8fafc, #eef2ff);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1e293b;
}

.panel-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.panel-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: #f8fafc;
}

.empty-hint {
  color: #64748b;
  font-size: 13px;
  text-align: center;
  padding: 24px 12px;
}

.msg {
  margin-bottom: 10px;
  display: flex;
}

.msg-user {
  justify-content: flex-end;
}

.msg-assistant {
  justify-content: flex-start;
}

.msg-bubble {
  max-width: 88%;
  padding: 10px 12px;
  border-radius: 12px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-word;
}

.msg-user .msg-bubble {
  background: linear-gradient(135deg, #4f46e5, #6366f1);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.msg-assistant .msg-bubble {
  background: #fff;
  color: #334155;
  border: 1px solid #e2e8f0;
  border-bottom-left-radius: 4px;
}

.msg-bubble.typing {
  color: #94a3b8;
  font-size: 13px;
}

.panel-input {
  padding: 10px 12px 12px;
  background: #fff;
  border-top: 1px solid #e2e8f0;
}

.input-row {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}
</style>
