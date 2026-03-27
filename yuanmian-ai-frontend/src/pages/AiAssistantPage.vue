<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { chat, chatStream } from '@/api/aiAssistantController'

const input = ref('')
const output = ref('')
const sessionId = ref(`sess_${Date.now()}`)
const loading = ref(false)
let abortController: AbortController | null = null

const doChat = async () => {
  if (!input.value.trim()) {
    message.warning('请输入问题')
    return
  }
  loading.value = true
  try {
    const res = await chat({
      sessionId: sessionId.value,
      message: input.value,
    })
    if (res.data.code === 0) {
      output.value = res.data.data?.answer ?? ''
    } else {
      message.error(res.data.message || '请求失败')
    }
  } catch (e: any) {
    message.error(e?.message || '请求失败')
  } finally {
    loading.value = false
  }
}

const doChatStream = async () => {
  if (!input.value.trim()) {
    message.warning('请输入问题')
    return
  }
  loading.value = true
  output.value = ''
  abortController = new AbortController()
  try {
    await chatStream(
      {
        sessionId: sessionId.value,
        message: input.value,
      },
      (chunk) => {
        if (chunk === '[DONE]') {
          return
        }
        output.value += (output.value ? '\n' : '') + chunk
      },
      { signal: abortController.signal }
    )
  } catch (e: any) {
    if (e?.name !== 'AbortError') {
      message.error(e?.message || '流式请求失败')
    }
  } finally {
    loading.value = false
    abortController = null
  }
}

const stopStream = () => {
  abortController?.abort()
}
</script>

<template>
  <div class="assistant-page">
    <a-card title="AI 助手（后端网关 + Agent）">
      <a-space direction="vertical" style="width: 100%">
        <a-input v-model:value="sessionId" placeholder="会话 ID" />
        <a-textarea v-model:value="input" :rows="5" placeholder="输入你的问题..." />
        <a-space>
          <a-button type="primary" :loading="loading" @click="doChat">普通对话</a-button>
          <a-button type="primary" ghost :loading="loading" @click="doChatStream">流式对话</a-button>
          <a-button danger :disabled="!loading" @click="stopStream">停止流式</a-button>
        </a-space>
        <a-card size="small" title="输出结果">
          <pre class="result">{{ output || '暂无输出' }}</pre>
        </a-card>
      </a-space>
    </a-card>
  </div>
</template>

<style scoped>
.assistant-page {
  max-width: 960px;
  margin: 24px auto;
}

.result {
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
}
</style>
