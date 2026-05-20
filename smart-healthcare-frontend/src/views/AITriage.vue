<template>
  <div>
    <h2 class="page-title">🤖 AI 智能导诊</h2>
    <el-row :gutter="20">
      <el-col :span="14">
        <el-card>
          <template #header><span style="font-weight:bold">症状描述</span></template>
          <el-input
            v-model="symptom"
            type="textarea"
            :rows="6"
            placeholder="请描述您的症状，例如：头痛、发烧、咳嗽三天..."
          />
          <div style="margin-top:16px;display:flex;gap:12px">
            <el-button type="primary" size="large" :loading="loading" @click="doTriage">开始导诊</el-button>
            <el-button size="large" @click="symptom = ''; result = null">清空</el-button>
          </div>
        </el-card>

        <el-card v-if="result" style="margin-top:16px">
          <template #header><span style="font-weight:bold">导诊结果</span></template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="建议科室">
              <el-tag type="primary" size="large">{{ result.department }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="紧急程度">
              <el-tag :type="urgencyType(result.urgency)" size="large">{{ result.urgency }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="可能疾病">{{ result.possibleDisease || '待进一步检查' }}</el-descriptions-item>
            <el-descriptions-item label="建议">
              <div v-html="result.advice"></div>
            </el-descriptions-item>
          </el-descriptions>
          <div style="margin-top:12px">
            <el-alert type="warning" :closable="false" show-icon title="以上结果由AI生成，仅供参考，请及时就医！" />
          </div>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card>
          <template #header><span style="font-weight:bold">常见症状参考</span></template>
          <div class="symptom-tags">
            <el-tag v-for="s in commonSymptoms" :key="s" class="tag-item" @click="symptom = s" style="cursor:pointer">
              {{ s }}
            </el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { aiTriage } from '@/api/ai'

const symptom = ref('')
const result = ref(null)
const loading = ref(false)

const commonSymptoms = [
  '头痛', '发热', '咳嗽', '腹痛', '胸痛', '关节痛',
  '头晕', '恶心呕吐', '腹泻', '皮疹', '呼吸困难',
  '腰痛', '喉咙痛', '鼻塞流涕', '乏力', '失眠',
]

function urgencyType(u) {
  return { '低': 'info', '中': 'warning', '高': 'danger' }[u] || 'info'
}

async function doTriage() {
  if (!symptom.value.trim()) return
  loading.value = true
  try {
    result.value = await aiTriage({ symptomDescription: symptom.value })
  } catch {} finally { loading.value = false }
}
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
.symptom-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.tag-item { padding: 8px 16px; font-size: 14px; }
.tag-item:hover { opacity: 0.8; }
</style>
