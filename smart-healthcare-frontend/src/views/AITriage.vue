<template>
  <div class="ai-triage-page">
    <h2 class="page-title">🤖 AI 智能导诊</h2>
    <div class="chat-container">
      <!-- 顶部提示条 -->
      <div class="chat-header">
        <div class="ai-avatar-lg">🤖</div>
        <div class="ai-info">
          <div class="ai-name">AI 智能导诊助手</div>
          <div class="ai-desc">请描述您的症状，我将为您推荐最合适的就诊科室</div>
        </div>
      </div>

      <!-- 对话消息区 -->
      <div class="chat-messages" ref="messagesRef">
        <!-- 欢迎消息 -->
        <div v-if="messages.length === 0" class="welcome-area">
          <div class="welcome-icon">🏥</div>
          <div class="welcome-text">您好！我是智能导诊助手</div>
          <div class="welcome-sub">请描述您的症状，例如"头痛、发烧、咳嗽三天"</div>
          <div class="quick-symptoms">
            <el-tag
              v-for="s in commonSymptoms"
              :key="s"
              class="quick-tag"
              @click="sendQuick(s)"
              effect="plain"
            >{{ s }}</el-tag>
          </div>
        </div>

        <!-- 对话气泡 -->
        <div
          v-for="(msg, idx) in messages"
          :key="idx"
          class="message-row"
          :class="msg.role === 'user' ? 'msg-right' : 'msg-left'"
        >
          <!-- AI 头像 -->
          <div v-if="msg.role === 'ai'" class="msg-avatar">🤖</div>
          <div class="msg-bubble-wrapper">
            <div class="msg-bubble" :class="msg.role === 'user' ? 'bubble-user' : 'bubble-ai'">
              <!-- 加载动画 -->
              <span v-if="msg.loading" class="typing-dots">
                <span></span><span></span><span></span>
              </span>
              <!-- AI 结构化结果 -->
              <div v-else-if="msg.role === 'ai' && msg.result" class="triage-result">
                <div class="result-item">
                  <span class="result-label">🏥 建议科室：</span>
                  <el-tag v-for="d in msg.result.departments" :key="d" type="primary" style="margin-right:6px">{{ d }}</el-tag>
                </div>
                <div class="result-item" style="margin-top:8px">
                  <span class="result-label">💡 就诊建议：</span>
                  <span>{{ msg.result.advice }}</span>
                </div>
                <el-alert type="warning" :closable="false" show-icon title="以上结果由AI生成，仅供参考，请及时就医！" style="margin-top:10px" />
              </div>
              <!-- 普通 AI 文本 -->
              <span v-else>{{ msg.content }}</span>
            </div>
          </div>
          <!-- 用户头像 -->
          <div v-if="msg.role === 'user'" class="msg-avatar msg-avatar-user">👤</div>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="chat-input-area">
        <div class="input-row">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="请描述您的症状..."
            @keydown.enter.exact.prevent="sendMessage"
            :disabled="streaming"
          />
          <div class="input-actions">
            <el-button
              type="primary"
              :icon="streaming ? 'Loading' : 'Promotion'"
              @click="sendMessage"
              :loading="streaming"
              size="large"
              circle
            />
            <el-button text @click="clearChat" :disabled="messages.length === 0">清空对话</el-button>
          </div>
        </div>
        <div class="input-hint">
          <span>💡 支持自然语言描述，按 Enter 发送 · 流式响应</span>
          <el-switch v-model="useStream" size="small" active-text="流式" inactive-text="同步" style="margin-left:12px" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { aiTriage, aiTriageStream } from '@/api/ai'
import { ElMessage } from 'element-plus'

const inputText = ref('')
const messages = ref([])
const streaming = ref(false)
const useStream = ref(true)
const messagesRef = ref(null)

const commonSymptoms = [
  '头痛', '发热咳嗽', '腹痛腹泻', '胸痛胸闷', '关节疼痛',
  '头晕恶心', '皮疹瘙痒', '呼吸困难', '腰背酸痛', '失眠乏力',
]

// 自动滚动到底部
async function scrollToBottom() {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// 发送快捷症状
function sendQuick(text) {
  inputText.value = text
  sendMessage()
}

// 发送消息
async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || streaming.value) return
  inputText.value = ''

  // 添加用户消息
  messages.value.push({ role: 'user', content: text })
  await scrollToBottom()

  // 添加 AI 加载消息
  const aiMsgIdx = messages.value.length
  messages.value.push({ role: 'ai', content: '', loading: true })
  await scrollToBottom()

  if (useStream.value) {
    await sendStreamMessage(text, aiMsgIdx)
  } else {
    await sendSyncMessage(text, aiMsgIdx)
  }
}

// 同步导诊
async function sendSyncMessage(text, aiMsgIdx) {
  try {
    const result = await aiTriage({ symptomDescription: text })
    messages.value[aiMsgIdx] = {
      role: 'ai',
      content: `根据您的描述，建议就诊：${result.departments?.join('、') || '未知科室'}`,
      result,
      loading: false,
    }
  } catch {
    messages.value[aiMsgIdx] = { role: 'ai', content: '抱歉，AI导诊服务暂时不可用，请稍后再试。', loading: false }
  }
  await scrollToBottom()
}

// SSE 流式导诊
async function sendStreamMessage(text, aiMsgIdx) {
  streaming.value = true
  let fullContent = ''
  let reader = null
  // 安全超时：30秒后强制结束流式状态
  const safetyTimer = setTimeout(() => {
    streaming.value = false
    if (reader) {
      try { reader.cancel() } catch {}
    }
  }, 30000)

  try {
    const response = await aiTriageStream({ symptomDescription: text })
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }
    reader = response.body.getReader()
    const decoder = new TextDecoder()
    let lineBuffer = ''  // 缓冲区：处理TCP分片导致的跨包断行

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      const chunk = decoder.decode(value, { stream: true })
      // 拼接到缓冲区，按完整行切割
      lineBuffer += chunk
      const lines = lineBuffer.split('\n')
      // 最后一段可能是不完整的行，保留在缓冲区
      lineBuffer = lines.pop() || ''
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim()
          if (data === '[DONE]') continue
          fullContent += data
          // 流式过程中只显示加载状态，不显示原始JSON碎片
          messages.value[aiMsgIdx] = {
            role: 'ai',
            content: '',
            loading: true,
          }
        }
      }
    }
    // 处理流结束后缓冲区中剩余的完整行
    for (const line of lineBuffer.split('\n')) {
      if (line.startsWith('data:')) {
        const data = line.substring(5).trim()
        if (data !== '[DONE]') {
          fullContent += data
        }
      }
    }

    // 尝试解析结构化结果
    const parsed = tryParseAIResponse(fullContent)
    if (parsed) {
      messages.value[aiMsgIdx] = {
        role: 'ai',
        content: parsed.departments?.join('、') || '未知科室',
        result: parsed,
        loading: false,
      }
    } else if (fullContent) {
      // 解析失败但有内容，显示原始文本作为降级
      messages.value[aiMsgIdx] = {
        role: 'ai',
        content: fullContent.substring(0, 500),
        loading: false,
      }
    } else {
      messages.value[aiMsgIdx] = { role: 'ai', content: '抱歉，AI导诊服务暂时不可用，请稍后再试。', loading: false }
    }
  } catch (err) {
    console.error('流式导诊失败:', err)
    if (!fullContent) {
      messages.value[aiMsgIdx] = { role: 'ai', content: '抱歉，AI导诊服务暂时不可用，请稍后再试。', loading: false }
    }
  } finally {
    clearTimeout(safetyTimer)
    streaming.value = false
    try { await scrollToBottom() } catch {}
  }
}

// 尝试从AI响应中提取结构化JSON（处理markdown代码块包裹等格式问题）
function tryParseAIResponse(raw) {
  if (!raw) return null
  let text = raw.trim()
  
  // 1. 去除 markdown 代码块包裹
  if (text.startsWith('```')) {
    const lines = text.split('\n')
    if (lines.length >= 3) {
      lines.shift() // 移除首行 ```json
      if (lines[lines.length - 1].trim() === '```') lines.pop()
      text = lines.join('\n').trim()
    }
  }
  
  // 2. 提取 JSON 对象 { ... }
  const start = text.indexOf('{')
  const end = text.lastIndexOf('}')
  if (start >= 0 && end > start) {
    text = text.substring(start, end + 1)
  }
  
  // 3. 尝试解析
  try {
    const json = JSON.parse(text)
    if (json.departments) return json
  } catch (e) {
    console.warn('AI响应JSON解析失败:', e, '\n原始内容:', text.substring(0, 200))
  }

  // 4. 降级：如果无法解析，至少尝试提取信息
  return null
}

// 清空对话
function clearChat() {
  messages.value = []
  inputText.value = ''
}
</script>

<style scoped>
.ai-triage-page { height: calc(100vh - 140px); display: flex; flex-direction: column; }
.page-title { margin-bottom: 12px; font-size: 22px; color: #333; flex-shrink: 0; }

/* ========== 对话容器 ========== */
.chat-container {
  flex: 1; display: flex; flex-direction: column;
  background: #fff; border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  overflow: hidden;
}

/* 顶部 AI 信息 */
.chat-header {
  display: flex; align-items: center; gap: 14px;
  padding: 16px 24px;
  background: linear-gradient(135deg, #409eff 0%, #2d6cdf 100%);
  color: #fff;
  flex-shrink: 0;
}
.ai-avatar-lg { font-size: 40px; }
.ai-name { font-size: 18px; font-weight: bold; }
.ai-desc { font-size: 13px; opacity: 0.85; margin-top: 2px; }

/* ========== 消息区域 ========== */
.chat-messages {
  flex: 1; overflow-y: auto; padding: 20px 24px;
  background: #f7f8fc;
  display: flex; flex-direction: column; gap: 16px;
}

/* 欢迎区 */
.welcome-area { text-align: center; padding: 40px 20px; }
.welcome-icon { font-size: 56px; margin-bottom: 12px; }
.welcome-text { font-size: 18px; color: #333; font-weight: bold; }
.welcome-sub { font-size: 14px; color: #999; margin: 8px 0 20px; }
.quick-symptoms { display: flex; flex-wrap: wrap; justify-content: center; gap: 10px; }
.quick-tag { cursor: pointer; padding: 8px 16px; font-size: 14px; border-radius: 20px; transition: all 0.2s; }
.quick-tag:hover { background: #ecf5ff; border-color: #409eff; color: #409eff; transform: scale(1.05); }

/* 消息行 */
.message-row { display: flex; align-items: flex-start; gap: 10px; }
.msg-right { flex-direction: row-reverse; }
.msg-left { flex-direction: row; }

.msg-avatar {
  font-size: 32px; flex-shrink: 0;
  width: 40px; height: 40px; display: flex;
  align-items: center; justify-content: center;
  border-radius: 50%; background: #e8f4fd;
}
.msg-avatar-user { background: #e6f7e6; }

.msg-bubble-wrapper { max-width: 75%; }
.msg-bubble {
  padding: 12px 16px; border-radius: 14px;
  font-size: 14px; line-height: 1.6;
  word-break: break-word;
}
.bubble-user {
  background: linear-gradient(135deg, #409eff, #2d6cdf);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.bubble-ai {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  border-bottom-left-radius: 4px;
}

/* 打字动画 */
.typing-dots { display: inline-flex; gap: 4px; padding: 4px 0; }
.typing-dots span {
  width: 8px; height: 8px; border-radius: 50%;
  background: #bbb; animation: typing 1.4s infinite ease-in-out both;
}
.typing-dots span:nth-child(1) { animation-delay: 0s; }
.typing-dots span:nth-child(2) { animation-delay: 0.2s; }
.typing-dots span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* 导诊结果 */
.triage-result { font-size: 14px; color: #333; }
.result-item { display: flex; align-items: flex-start; gap: 6px; }
.result-label { font-weight: bold; white-space: nowrap; }

/* ========== 输入区 ========== */
.chat-input-area {
  padding: 16px 24px; border-top: 1px solid #ebeef5;
  background: #fff; flex-shrink: 0;
}
.input-row { display: flex; gap: 12px; align-items: flex-end; }
.input-row .el-textarea { flex: 1; }
.input-actions { display: flex; align-items: center; gap: 8px; flex-shrink: 0; }
.input-hint { margin-top: 8px; font-size: 12px; color: #bbb; display: flex; align-items: center; }
</style>
