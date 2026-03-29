<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { chat, chatStream } from '@/api/aiAssistantController'

const AGENT_META_PREFIX = '__AGENT_META__'

type AgentStreamMeta = {
  _meta?: boolean
  next_action?: string
  score?: number
  question?: string
  weakness?: string
  follow_up_question?: string
  report?: string
  agent_trace?: string[]
}

function parseAgentMetaChunk(chunk: string): AgentStreamMeta | null {
  if (!chunk.startsWith(AGENT_META_PREFIX)) {
    return null
  }
  try {
    const o = JSON.parse(chunk.slice(AGENT_META_PREFIX.length)) as AgentStreamMeta
    return o && o._meta ? o : null
  } catch {
    return null
  }
}

function applyInterviewMeta(meta: AgentStreamMeta) {
  if (meta.next_action != null && meta.next_action !== '') {
    interviewNextAction.value = meta.next_action
  }
  if (meta.score != null) {
    interviewScore.value = meta.score
  }
  if (meta.report) {
    interviewReport.value = meta.report
  }
}

function appendOutputChunk(chunk: string) {
  output.value += chunk
}

function appendQuestionChunk(chunk: string) {
  currentQuestion.value += chunk
}

const activeTab = ref('assistant')

const input = ref('')
const output = ref('')
const sessionId = ref(`sess_${Date.now()}`)
/** 普通对话按钮 loading */
const chatLoading = ref(false)
/** 流式对话按钮 loading（首包到达后会关掉，避免一直转圈） */
const streamLoading = ref(false)
/** 流式是否仍在进行（用于「停止流式」是否可点） */
const streamActive = ref(false)
const nextAction = ref('')
const score = ref<number | null>(null)
const question = ref('')
const weakness = ref('')
const followUpQuestion = ref('')
const report = ref('')
const agentTrace = ref<string[]>([])
let abortController: AbortController | null = null

// 面试模式
const interviewSessionId = ref(`interview_${Date.now()}`)
const interviewStarted = ref(false)
const interviewLoading = ref(false)
const currentQuestion = ref('')
const answerInput = ref('')
const interviewLog = ref<string[]>([])
const interviewReport = ref('')
const interviewScore = ref<number | null>(null)
const interviewNextAction = ref('')
let interviewAbortController: AbortController | null = null

const doChat = async () => {
  if (!input.value.trim()) {
    message.warning('请输入问题')
    return
  }
  chatLoading.value = true
  try {
    const res = await chat({
      sessionId: sessionId.value,
      message: input.value,
    })
    if (res.data.code === 0) {
      output.value = res.data.data?.answer ?? ''
      nextAction.value = res.data.data?.nextAction ?? ''
      score.value = res.data.data?.score ?? null
      question.value = res.data.data?.question ?? ''
      weakness.value = res.data.data?.weakness ?? ''
      followUpQuestion.value = res.data.data?.followUpQuestion ?? ''
      report.value = res.data.data?.report ?? ''
      agentTrace.value = res.data.data?.agentTrace ?? []
    } else {
      message.error(res.data.message || '请求失败')
    }
  } catch (e: any) {
    message.error(e?.message || '请求失败')
  } finally {
    chatLoading.value = false
  }
}

const doChatStream = async () => {
  if (!input.value.trim()) {
    message.warning('请输入问题')
    return
  }
  streamActive.value = true
  streamLoading.value = true
  output.value = ''
  abortController = new AbortController()
  let firstChunk = false
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
        if (chunk.startsWith(AGENT_META_PREFIX)) {
          return
        }
        if (chunk && !firstChunk) {
          firstChunk = true
          streamLoading.value = false
        }
        appendOutputChunk(chunk)
      },
      { signal: abortController.signal }
    )
  } catch (e: any) {
    if (e?.name !== 'AbortError') {
      message.error(e?.message || '流式请求失败')
    }
  } finally {
    streamActive.value = false
    streamLoading.value = false
    abortController = null
  }
}

const stopStream = () => {
  abortController?.abort()
  streamActive.value = false
  streamLoading.value = false
}

const startInterview = async () => {
  interviewAbortController?.abort()
  interviewAbortController = new AbortController()
  interviewLoading.value = true
  interviewStarted.value = false
  currentQuestion.value = ''
  interviewLog.value = []
  interviewReport.value = ''
  interviewScore.value = null
  interviewNextAction.value = ''
  let firstChunk = false
  try {
    await chatStream(
      {
        sessionId: interviewSessionId.value,
        message: '我准备开始技术面试，请先给我第一道题目。',
      },
      (chunk) => {
        if (chunk === '[DONE]') {
          return
        }
        const meta = parseAgentMetaChunk(chunk)
        if (meta) {
          applyInterviewMeta(meta)
          return
        }
        if (chunk && !firstChunk) {
          firstChunk = true
          interviewLoading.value = false
        }
        appendQuestionChunk(chunk)
      },
      { signal: interviewAbortController.signal }
    )
    interviewStarted.value = true
    if (currentQuestion.value.trim()) {
      interviewLog.value.push(`面试官：${currentQuestion.value.trim()}`)
    }
  } catch (e: any) {
    if (e?.name !== 'AbortError') {
      message.error(e?.message || '开始面试失败')
    }
  } finally {
    interviewLoading.value = false
    interviewAbortController = null
  }
}

const submitAnswer = async () => {
  if (!interviewStarted.value) {
    message.warning('请先点击开始答题')
    return
  }
  if (!answerInput.value.trim()) {
    message.warning('请输入你的答案')
    return
  }
  interviewAbortController?.abort()
  interviewAbortController = new AbortController()
  interviewLoading.value = true
  const candidateAnswer = answerInput.value.trim()
  interviewLog.value.push(`你：${candidateAnswer}`)
  answerInput.value = ''
  currentQuestion.value = ''
  let firstChunk = false
  try {
    await chatStream(
      {
        sessionId: interviewSessionId.value,
        message: candidateAnswer,
      },
      (chunk) => {
        if (chunk === '[DONE]') {
          return
        }
        const meta = parseAgentMetaChunk(chunk)
        if (meta) {
          applyInterviewMeta(meta)
          return
        }
        if (chunk && !firstChunk) {
          firstChunk = true
          interviewLoading.value = false
        }
        appendQuestionChunk(chunk)
      },
      { signal: interviewAbortController.signal }
    )
    const q = currentQuestion.value.trim()
    if (q) {
      interviewLog.value.push(`面试官：${q}`)
    }
  } catch (e: any) {
    if (e?.name !== 'AbortError') {
      message.error(e?.message || '提交答案失败')
    }
  } finally {
    interviewLoading.value = false
    interviewAbortController = null
  }
}

const resetInterview = () => {
  interviewAbortController?.abort()
  interviewAbortController = null
  interviewSessionId.value = `interview_${Date.now()}`
  interviewStarted.value = false
  interviewLoading.value = false
  currentQuestion.value = ''
  answerInput.value = ''
  interviewLog.value = []
  interviewReport.value = ''
  interviewScore.value = null
  interviewNextAction.value = ''
}

onUnmounted(() => {
  interviewAbortController?.abort()
})
</script>

<template>
  <div class="assistant-page">
    <a-card title="AI 助手（后端网关 + Agent）">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="assistant" tab="AI 对话">
          <a-space direction="vertical" style="width: 100%">
            <a-input v-model:value="sessionId" placeholder="会话 ID" />
            <a-textarea v-model:value="input" :rows="5" placeholder="输入你的问题..." />
            <a-space>
              <a-button type="primary" :loading="chatLoading" @click="doChat">普通对话</a-button>
              <a-button type="primary" ghost :loading="streamLoading" @click="doChatStream">流式对话</a-button>
              <a-button danger :disabled="!streamActive" @click="stopStream">停止流式</a-button>
            </a-space>
            <a-card size="small" title="输出结果">
              <pre class="result">{{ output || '暂无输出' }}</pre>
            </a-card>
            <a-card size="small" title="多 Agent 调试信息">
              <a-descriptions :column="1" bordered size="small">
                <a-descriptions-item label="next_action">{{ nextAction || '-' }}</a-descriptions-item>
                <a-descriptions-item label="score">{{ score ?? '-' }}</a-descriptions-item>
                <a-descriptions-item label="question">{{ question || '-' }}</a-descriptions-item>
                <a-descriptions-item label="weakness">{{ weakness || '-' }}</a-descriptions-item>
                <a-descriptions-item label="follow_up_question">{{ followUpQuestion || '-' }}</a-descriptions-item>
                <a-descriptions-item label="report">{{ report || '-' }}</a-descriptions-item>
                <a-descriptions-item label="agent_trace">{{ agentTrace.join(' -> ') || '-' }}</a-descriptions-item>
              </a-descriptions>
            </a-card>
          </a-space>
        </a-tab-pane>
        <a-tab-pane key="interview" tab="模拟面试">
          <a-space direction="vertical" style="width: 100%">
            <a-input v-model:value="interviewSessionId" placeholder="面试会话 ID" />
            <a-space>
              <a-button type="primary" :loading="interviewLoading" @click="startInterview">开始答题</a-button>
              <a-button @click="resetInterview">重置面试</a-button>
            </a-space>
            <a-card size="small" title="当前题目">
              <pre class="result">{{ currentQuestion || '点击开始答题后将获取第一题' }}</pre>
            </a-card>
            <a-textarea
              v-model:value="answerInput"
              :rows="4"
              :disabled="!interviewStarted || interviewLoading"
              placeholder="输入你的答案..."
            />
            <a-space>
              <a-button
                type="primary"
                :loading="interviewLoading"
                :disabled="!interviewStarted"
                @click="submitAnswer"
              >
                提交答案
              </a-button>
            </a-space>
            <a-card size="small" title="面试状态">
              <a-descriptions :column="1" bordered size="small">
                <a-descriptions-item label="next_action">{{ interviewNextAction || '-' }}</a-descriptions-item>
                <a-descriptions-item label="score">{{ interviewScore ?? '-' }}</a-descriptions-item>
              </a-descriptions>
            </a-card>
            <a-card size="small" title="面试记录">
              <pre class="result">{{ interviewLog.join('\n\n') || '暂无记录' }}</pre>
            </a-card>
            <a-card size="small" title="最终报告">
              <pre class="result">{{ interviewReport || '暂无报告' }}</pre>
            </a-card>
          </a-space>
        </a-tab-pane>
      </a-tabs>
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
