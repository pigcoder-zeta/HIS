<template>
  <div>
    <h2 class="page-title">💊 处方审核</h2>
    <el-card>
      <el-table :data="list" border stripe v-loading="loading" empty-text="暂无待审核处方">
        <el-table-column prop="id" label="处方号" width="80" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="diagnosis" label="诊断" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" size="small" @click="audit(row, true)">通过</el-button>
            <el-button v-if="row.status === 0" type="danger" size="small" @click="audit(row, false)">驳回</el-button>
            <el-button v-if="row.status === 1" type="primary" size="small" @click="dispense(row)">发药</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getPendingPrescriptions, auditPrescription, dispensePrescription } from '@/api/pharmacy'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const list = ref([])
const loading = ref(false)
const statusMap = { 0: '待审核', 1: '已审核', 2: '已发药', 3: '已驳回' }
const statusTag = (s) => ({ 0:'warning',1:'primary',2:'success',3:'danger' }[s]||'info')

async function load() {
  loading.value = true
  try { list.value = await getPendingPrescriptions() } catch {} finally { loading.value = false }
}

async function audit(row, approved) {
  await auditPrescription(row.id, userStore.userId, approved, approved ? '审核通过' : '审核驳回')
  ElMessage.success(approved ? '已通过' : '已驳回')
  load()
}

async function dispense(row) {
  await dispensePrescription(row.id, userStore.userId)
  ElMessage.success('发药成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
