<template>
  <div>
    <h2 class="page-title">📝 电子病历</h2>
    <el-card>
      <el-form :model="form" label-width="100px" style="max-width:800px">
        <el-form-item label="患者ID" required>
          <el-input-number v-model="form.patientId" :min="1" />
          <el-button style="margin-left:12px" type="primary" :loading="loadingHistory" @click="loadHistory">查看历史病历</el-button>
        </el-form-item>
        <el-form-item label="主诉" required>
          <el-input v-model="form.chiefComplaint" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="现病史" required>
          <el-input v-model="form.presentIllness" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="体格检查">
          <el-input v-model="form.physicalExam" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="诊断" required>
          <el-input v-model="form.diagnosis" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="医嘱">
          <el-input v-model="form.advice" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存病历</el-button>
          <el-button v-if="currentId" type="success" :loading="signing" @click="sign">签名归档</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="historyVisible" title="历史病历" width="700px">
      <el-timeline>
        <el-timeline-item v-for="r in historyList" :key="r.id" :timestamp="r.createTime" placement="top">
          <el-card>
            <p><b>主诉：</b>{{ r.chiefComplaint }}</p>
            <p><b>诊断：</b>{{ r.diagnosis }}</p>
            <p><b>医嘱：</b>{{ r.advice || '无' }}</p>
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
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const form = reactive({ patientId: null, chiefComplaint: '', presentIllness: '', physicalExam: '', diagnosis: '', advice: '' })
const saving = ref(false)
const signing = ref(false)
const currentId = ref(null)
const historyVisible = ref(false)
const historyList = ref([])
const loadingHistory = ref(false)

async function save() {
  saving.value = true
  try {
    const r = await saveRecord(userStore.userId, form)
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
  loadingHistory.value = true
  try {
    historyList.value = await getPatientRecords(form.patientId)
    historyVisible.value = true
  } catch {} finally { loadingHistory.value = false }
}
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
