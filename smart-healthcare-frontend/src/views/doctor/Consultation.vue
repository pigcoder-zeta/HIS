<template>
  <div class="consultation-page">
    <h2 class="page-title">🩺 接诊工作台</h2>
    <div class="consultation-layout">
      <!-- 左侧：待诊患者队列 -->
      <div class="left-panel">
        <el-card class="queue-card">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span style="font-weight:bold">📋 待诊队列</span>
              <el-tag type="warning">{{ queue.filter(p => p.status === 1).length }}人等待</el-tag>
            </div>
          </template>
          <div class="queue-list">
            <div
              v-for="p in queue"
              :key="p.id"
              class="queue-item"
              :class="{ active: currentPatient?.id === p.id, called: p.status === 2 }"
              @click="selectPatient(p)"
            >
              <div class="queue-number">{{ p.queueNumber || '--' }}</div>
              <div class="queue-info">
                <div class="queue-name">{{ p.patientName }}</div>
                <div class="queue-phone">{{ p.patientPhone }}</div>
                <div class="queue-dept">{{ p.deptName || p.departmentName }}</div>
              </div>
              <div class="queue-status">
                <el-tag :type="statusColors[p.status]" size="small">{{ statusTexts[p.status] }}</el-tag>
              </div>
            </div>
            <el-empty v-if="!queue.length" description="今日暂无待诊患者" :image-size="80" />
          </div>
        </el-card>

        <el-card style="margin-top:12px" v-if="currentPatient">
          <template #header><span style="font-weight:bold">👤 患者信息</span></template>
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="姓名">{{ currentPatient.patientName }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ currentPatient.patientPhone || '--' }}</el-descriptions-item>
            <el-descriptions-item label="科室">{{ currentPatient.deptName || currentPatient.departmentName }}</el-descriptions-item>
            <el-descriptions-item label="号序">{{ currentPatient.queueNumber || '--' }}</el-descriptions-item>
          </el-descriptions>
          <div style="margin-top:12px;display:flex;gap:8px">
            <el-button
              v-if="currentPatient.status === 1"
              type="primary"
              @click="callPatient(currentPatient)"
              style="flex:1"
            >📢 呼叫</el-button>
            <el-button
              v-if="currentPatient.status === 2"
              type="success"
              @click="completePatient(currentPatient)"
              style="flex:1"
            >✅ 完成就诊</el-button>
          </div>
        </el-card>
      </div>

      <!-- 右侧：SOAP 病历 + 处方 -->
      <div class="right-panel">
        <el-tabs v-model="activeTab" type="border-card">
          <!-- Tab1: SOAP 电子病历 -->
          <el-tab-pane label="📝 SOAP 病历" name="record">
            <div v-if="!currentPatient" class="empty-hint">
              <div class="empty-icon">👈</div>
              <p>请从左侧队列中选择一位待诊患者</p>
            </div>
            <el-form v-else :model="recordForm" label-width="90px" size="default">
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="S 主诉" required>
                    <el-input v-model="recordForm.chiefComplaint" type="textarea" :rows="2" placeholder="患者主要不适..." />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="S 现病史">
                    <el-input v-model="recordForm.presentIllness" type="textarea" :rows="2" placeholder="起病情况..." />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="O 体格检查">
                    <el-input v-model="recordForm.physicalExam" type="textarea" :rows="2" placeholder="查体发现..." />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="A 初步诊断" required>
                    <el-input v-model="recordForm.diagnosis" type="textarea" :rows="2" placeholder="诊断结论..." />
                  </el-form-item>
                </el-col>
              </el-row>
              <el-form-item label="P 治疗计划">
                <el-input v-model="recordForm.advice" type="textarea" :rows="2" placeholder="治疗方案、检查建议..." />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="saving" @click="saveRecord">💾 保存病历</el-button>
                <el-button v-if="recordId" type="success" :loading="signing" @click="signRecord">✍️ 签名归档</el-button>
                <el-button type="warning" :loading="aiGenerating" @click="aiAssistRecord" :disabled="!recordForm.chiefComplaint">🤖 AI 辅助</el-button>
                <el-button @click="resetRecordForm">清空</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- Tab2: 开具处方 -->
          <el-tab-pane label="💊 开具处方" name="prescription">
            <div v-if="!currentPatient || !recordId" class="empty-hint">
              <div class="empty-icon">📝</div>
              <p>{{ !currentPatient ? '请先选择患者' : '请先保存病历后再开具处方' }}</p>
            </div>
            <div v-else>
              <!-- 药品搜索 -->
              <div class="drug-search-bar">
                <el-input
                  v-model="drugKeyword"
                  placeholder="搜索药品名称或编码..."
                  prefix-icon="Search"
                  @keydown.enter="searchDrugs"
                  style="width:280px"
                />
                <el-button type="primary" @click="searchDrugs">搜索</el-button>
              </div>
              <!-- 已选药品列表 -->
              <el-table :data="prescriptionItems" border stripe size="small" style="margin-top:12px">
                <el-table-column prop="drugName" label="药品名称" width="180" />
                <el-table-column prop="specification" label="规格" width="120" />
                <el-table-column prop="unitPrice" label="单价(元)" width="100" />
                <el-table-column label="数量" width="140">
                  <template #default="{ row, $index }">
                    <el-input-number v-model="row.quantity" :min="1" :max="row.stockCount" size="small" controls-position="right" />
                  </template>
                </el-table-column>
                <el-table-column label="用量" width="140">
                  <template #default="{ row }">
                    <el-input v-model="row.dosage" size="small" placeholder="如：1片" />
                  </template>
                </el-table-column>
                <el-table-column label="用法" width="160">
                  <template #default="{ row }">
                    <el-input v-model="row.usageMethod" size="small" placeholder="如：口服 tid" />
                  </template>
                </el-table-column>
                <el-table-column label="小计(元)" width="100">
                  <template #default="{ row }">{{ ((row.unitPrice || 0) * (row.quantity || 0)).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column label="操作" width="80">
                  <template #default="{ $index }">
                    <el-button type="danger" size="small" @click="prescriptionItems.splice($index, 1)">移除</el-button>
                  </template>
                </el-table-column>
              </el-table>

              <!-- 药品搜索结果 -->
              <el-dialog v-model="drugSearchVisible" title="药品库检索" width="700px">
                <el-table :data="drugSearchResults" stripe size="small" max-height="400" @row-click="addDrugToPrescription" style="cursor:pointer">
                  <el-table-column prop="drugName" label="药品名称" width="200" />
                  <el-table-column prop="genericName" label="通用名" width="160" />
                  <el-table-column prop="specification" label="规格" width="120" />
                  <el-table-column prop="unitPrice" label="单价(元)" width="100" />
                  <el-table-column prop="stockCount" label="库存" width="80">
                    <template #default="{ row }">
                      <el-tag :type="row.stockCount <= row.safeThreshold ? 'danger' : 'success'" size="small">{{ row.stockCount }}</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </el-dialog>

              <div style="margin-top:16px;display:flex;justify-content:space-between;align-items:center">
                <span style="font-size:16px;font-weight:bold">
                  合计：¥{{ totalAmount }}
                </span>
                <el-button type="primary" size="large" :loading="prescribing" @click="submitPrescription" :disabled="!prescriptionItems.length">
                  📋 提交处方
                </el-button>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getTodayAppointments, callNext, completeAppointment, saveRecord as saveRecordApi, signRecord as signRecordApi, createPrescription } from '@/api/doctor'
import { getDrugPage } from '@/api/pharmacy'
import { generateMedicalRecord } from '@/api/ai'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

// ==================== 患者队列 ====================
const queue = ref([])
const currentPatient = ref(null)
const loading = ref(false)

const statusColors = { 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'danger' }
const statusTexts = { 1: '待就诊', 2: '就诊中', 3: '已完成', 4: '已取消', 5: '已爽约' }

async function loadQueue() {
  loading.value = true
  try { queue.value = await getTodayAppointments(userStore.userId) } catch {} finally { loading.value = false }
}

function selectPatient(patient) {
  currentPatient.value = patient
  recordId.value = null
  resetRecordForm()
  prescriptionItems.value = []
}

async function callPatient(patient) {
  await callNext(patient.id)
  patient.status = 2
  ElMessage.success(`已呼叫 ${patient.patientName}`)
}

async function completePatient(patient) {
  await completeAppointment(patient.id)
  patient.status = 3
  ElMessage.success('就诊完成')
  currentPatient.value = null
  recordId.value = null
  resetRecordForm()
  prescriptionItems.value = []
}

// ==================== 病历表单 ====================
const activeTab = ref('record')
const recordForm = reactive({ chiefComplaint: '', presentIllness: '', physicalExam: '', diagnosis: '', advice: '' })
const recordId = ref(null)
const saving = ref(false)
const signing = ref(false)
const aiGenerating = ref(false)

function resetRecordForm() {
  Object.assign(recordForm, { chiefComplaint: '', presentIllness: '', physicalExam: '', diagnosis: '', advice: '' })
}

async function saveRecord() {
  if (!currentPatient.value) return
  saving.value = true
  try {
    const body = {
      patientId: currentPatient.value.patientId,
      chiefComplaint: recordForm.chiefComplaint,
      presentIllness: recordForm.presentIllness,
      physicalExamination: recordForm.physicalExam,
      auxiliaryExam: '',
      diagnosis: recordForm.diagnosis,
      treatmentPlan: recordForm.advice,
    }
    const r = await saveRecordApi(userStore.userId, body)
    recordId.value = r.id
    ElMessage.success('病历保存成功')
  } catch {} finally { saving.value = false }
}

async function signRecord() {
  signing.value = true
  try {
    await signRecordApi(recordId.value, userStore.userId)
    ElMessage.success('已签名归档')
  } catch {} finally { signing.value = false }
}

async function aiAssistRecord() {
  aiGenerating.value = true
  try {
    const result = await generateMedicalRecord(recordForm.chiefComplaint)
    if (!recordForm.presentIllness) recordForm.presentIllness = result.presentIllness || ''
    if (!recordForm.physicalExam) recordForm.physicalExam = result.physicalExamination || ''
    if (!recordForm.diagnosis) recordForm.diagnosis = result.diagnosis || ''
    if (!recordForm.advice) recordForm.advice = result.treatmentPlan || ''
    ElMessage.success('AI 已润色病历')
  } catch {} finally { aiGenerating.value = false }
}

// ==================== 处方 ====================
const drugKeyword = ref('')
const drugSearchVisible = ref(false)
const drugSearchResults = ref([])
const prescriptionItems = ref([])
const prescribing = ref(false)

const totalAmount = computed(() => {
  return prescriptionItems.value.reduce((sum, item) => sum + (item.unitPrice || 0) * (item.quantity || 0), 0).toFixed(2)
})

async function searchDrugs() {
  try {
    const page = await getDrugPage({ current: 1, size: 50, keyword: drugKeyword.value || undefined })
    drugSearchResults.value = page.records || []
    drugSearchVisible.value = true
  } catch {}
}

function addDrugToPrescription(row) {
  const exists = prescriptionItems.value.find(i => i.drugId === row.id)
  if (exists) {
    exists.quantity++
    return
  }
  prescriptionItems.value.push({
    drugId: row.id,
    drugName: row.drugName,
    specification: row.specification,
    unitPrice: row.unitPrice,
    stockCount: row.stockCount,
    quantity: 1,
    dosage: '',
    usageMethod: '',
  })
  drugSearchVisible.value = false
  ElMessage.success(`已添加：${row.drugName}`)
}

async function submitPrescription() {
  if (!recordId.value) { ElMessage.warning('请先保存病历'); return }
  prescribing.value = true
  try {
    const items = prescriptionItems.value.map(i => ({
      drugId: i.drugId,
      quantity: i.quantity,
      dosage: i.dosage || '',
      usageMethod: i.usageMethod || '',
    }))
    await createPrescription(userStore.userId, {
      recordId: recordId.value,
      patientId: currentPatient.value.patientId,
      items,
      note: '',
    })
    ElMessage.success('处方已生成，已发送至药房审核')
    prescriptionItems.value = []
  } catch {} finally { prescribing.value = false }
}

onMounted(loadQueue)
</script>

<style scoped>
.consultation-page { height: calc(100vh - 140px); display: flex; flex-direction: column; }
.page-title { margin-bottom: 12px; font-size: 22px; color: #333; flex-shrink: 0; }

.consultation-layout { flex: 1; display: flex; gap: 16px; overflow: hidden; }

/* 左侧面板 */
.left-panel { width: 300px; flex-shrink: 0; overflow-y: auto; }
.queue-card { height: 100%; display: flex; flex-direction: column; }
.queue-card :deep(.el-card__body) { padding: 8px; flex: 1; overflow-y: auto; }
.queue-list { max-height: 400px; overflow-y: auto; }

.queue-item {
  display: flex; align-items: center; gap: 12px;
  padding: 10px 12px; border-radius: 8px;
  cursor: pointer; transition: all 0.2s;
  border: 1px solid transparent; margin-bottom: 4px;
}
.queue-item:hover { background: #ecf5ff; }
.queue-item.active { background: #ecf5ff; border-color: #409eff; }
.queue-item.called { background: #f0f9eb; }

.queue-number {
  width: 32px; height: 32px; border-radius: 50%;
  background: #409eff; color: #fff; font-weight: bold;
  display: flex; align-items: center; justify-content: center;
  font-size: 14px; flex-shrink: 0;
}
.queue-info { flex: 1; min-width: 0; }
.queue-name { font-size: 14px; font-weight: bold; color: #333; }
.queue-phone { font-size: 12px; color: #999; }
.queue-dept { font-size: 12px; color: #666; }
.queue-status { flex-shrink: 0; }

/* 右侧面板 */
.right-panel { flex: 1; overflow: hidden; display: flex; flex-direction: column; }
.right-panel :deep(.el-tabs) { display: flex; flex-direction: column; height: 100%; }
.right-panel :deep(.el-tabs__content) { flex: 1; overflow-y: auto; padding: 16px; }
.right-panel :deep(.el-tab-pane) { height: 100%; }

.empty-hint { text-align: center; padding: 60px 0; color: #999; }
.empty-icon { font-size: 48px; margin-bottom: 12px; }

.drug-search-bar { display: flex; gap: 8px; align-items: center; }
</style>