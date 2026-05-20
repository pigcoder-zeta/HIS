<template>
  <div>
    <h2 class="page-title">📝 电子病历</h2>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-weight:bold">SOAP 病历书写</span>
          <el-button type="warning" :loading="aiGenerating" @click="aiAssist" :disabled="!form.chiefComplaint">
            🤖 AI 辅助生成病历
          </el-button>
        </div>
      </template>
      <el-row :gutter="20">
        <!-- 左侧：病历表单 -->
        <el-col :span="14">
          <el-form :model="form" label-width="100px">
            <el-form-item label="患者ID" required>
              <el-input-number v-model="form.patientId" :min="1" style="width:180px" />
              <el-button style="margin-left:12px" type="primary" :loading="loadingHistory" @click="loadHistory">查看历史病历</el-button>
            </el-form-item>
            <el-form-item label="S 主诉" required>
              <el-input v-model="form.chiefComplaint" type="textarea" :rows="2" placeholder="患者主要不适及持续时间..." />
            </el-form-item>
            <el-form-item label="S 现病史">
              <el-input v-model="form.presentIllness" type="textarea" :rows="3" placeholder="起病情况、主要症状演变、诊疗经过..." />
            </el-form-item>
            <el-form-item label="O 体格检查">
              <el-input v-model="form.physicalExam" type="textarea" :rows="2" placeholder="生命体征、系统查体阳性及阴性发现..." />
            </el-form-item>
            <el-form-item label="A 诊断" required>
              <el-input v-model="form.diagnosis" type="textarea" :rows="2" placeholder="主要诊断、次要诊断（ICD编码可选）..." />
            </el-form-item>
            <el-form-item label="P 治疗计划">
              <el-input v-model="form.advice" type="textarea" :rows="2" placeholder="进一步检查、用药、手术治疗、健康教育..." />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="save">💾 保存病历</el-button>
              <el-button v-if="currentId" type="success" :loading="signing" @click="sign">✍️ 签名归档</el-button>
              <el-button @click="resetForm">清空重置</el-button>
            </el-form-item>
          </el-form>
        </el-col>
        <!-- 右侧：AI 联想输入区 -->
        <el-col :span="10">
          <el-card shadow="never" class="ai-panel">
            <template #header><span style="font-weight:bold">🤖 AI 草稿润色</span></template>
            <div v-if="!aiNotes" class="ai-placeholder">
              <div class="ai-hint-icon">✏️</div>
              <p>在此输入零散笔记或口述草稿</p>
              <p style="font-size:12px;color:#bbb">AI 将自动润色为结构化 SOAP 病历</p>
            </div>
            <el-input
              v-model="aiNotes"
              type="textarea"
              :rows="10"
              placeholder="例如：患者男性 45岁，因反复上腹痛1周就诊。疼痛呈烧灼样，空腹加重，进食后缓解，伴有反酸、嗳气。查体上腹部轻压痛，无反跳痛..."
            />
            <el-button
              type="warning"
              style="width:100%;margin-top:12px"
              :loading="aiGenerating"
              @click="aiAssist"
            >🤖 生成结构化病历</el-button>
            <div v-if="aiResult" style="margin-top:12px">
              <el-alert type="success" :closable="false" title="AI 已生成结构化内容，已自动填充到左侧表单" />
              <el-button style="margin-top:8px" type="info" size="small" @click="aiResult = null; aiNotes = ''">清除 AI 结果</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 历史病历弹窗 -->
    <el-dialog v-model="historyVisible" title="📋 患者历史病历" width="700px">
      <el-timeline>
        <el-timeline-item v-for="r in historyList" :key="r.id" :timestamp="r.createTime" placement="top">
          <el-card>
            <p><b>S 主诉：</b>{{ r.chiefComplaint }}</p>
            <p><b>O 查体：</b>{{ r.physicalExamination || '无' }}</p>
            <p><b>A 诊断：</b>{{ r.diagnosis }}</p>
            <p><b>P 治疗：</b>{{ r.treatmentPlan || r.advice || '无' }}</p>
            <el-tag :type="r.status === 1 ? 'success' : 'info'" size="small">{{ r.status === 1 ? '已归档' : '草稿' }}</el-tag>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="!historyList.length" description="暂无历史病历" />
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useUserStore } from '@/store/user'
import { saveRecord, signRecord, getPatientRecords } from '@/api/doctor'
import { generateMedicalRecord } from '@/api/ai'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const form = reactive({ patientId: null, chiefComplaint: '', presentIllness: '', physicalExam: '', diagnosis: '', advice: '' })
const saving = ref(false)
const signing = ref(false)
const currentId = ref(null)
const historyVisible = ref(false)
const historyList = ref([])
const loadingHistory = ref(false)

// AI 辅助
const aiNotes = ref('')
const aiGenerating = ref(false)
const aiResult = ref(null)

async function save() {
  saving.value = true
  try {
    const body = {
      patientId: form.patientId,
      chiefComplaint: form.chiefComplaint,
      presentIllness: form.presentIllness,
      physicalExamination: form.physicalExam,
      auxiliaryExam: '',
      diagnosis: form.diagnosis,
      treatmentPlan: form.advice,
    }
    const r = await saveRecord(userStore.userId, body)
    currentId.value = r.id
    ElMessage.success('保存成功')
  } catch {} finally { saving.value = false }
}

async function sign() {
  signing.value = true
  try {
    await signRecord(currentId.value, userStore.userId)
    ElMessage.success('已签名归档')
  } catch {} finally { signing.value = false }
}

async function loadHistory() {
  if (!form.patientId) { ElMessage.warning('请先输入患者ID'); return }
  loadingHistory.value = true
  try {
    historyList.value = await getPatientRecords(form.patientId)
    historyVisible.value = true
  } catch {} finally { loadingHistory.value = false }
}

function resetForm() {
  Object.assign(form, { patientId: null, chiefComplaint: '', presentIllness: '', physicalExam: '', diagnosis: '', advice: '' })
  currentId.value = null
  aiNotes.value = ''
  aiResult.value = null
}

/** AI 辅助生成结构化病历 */
async function aiAssist() {
  const notes = aiNotes.value.trim() || form.chiefComplaint
  if (!notes) { ElMessage.warning('请先输入症状描述或草稿笔记'); return }
  aiGenerating.value = true
  try {
    const result = await generateMedicalRecord(notes)
    aiResult.value = result
    // 自动填充到表单（仅填充空字段，保留已填内容）
    if (!form.chiefComplaint) form.chiefComplaint = result.chiefComplaint || ''
    if (!form.presentIllness) form.presentIllness = result.presentIllness || ''
    if (!form.physicalExam) form.physicalExam = result.physicalExamination || ''
    if (!form.diagnosis) form.diagnosis = result.diagnosis || ''
    if (!form.advice) form.advice = result.treatmentPlan || ''
    ElMessage.success('AI 已生成结构化病历并填充到表单')
  } catch {} finally { aiGenerating.value = false }
}
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }

.ai-panel { background: #fafbfc; border: 1px dashed #d9d9d9; }
.ai-placeholder { text-align: center; padding: 30px 0; color: #999; }
.ai-hint-icon { font-size: 40px; margin-bottom: 12px; }
</style>
