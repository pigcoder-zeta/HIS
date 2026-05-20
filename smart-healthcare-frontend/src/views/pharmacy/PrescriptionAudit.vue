<template>
  <div>
    <h2 class="page-title">💊 处方审核与发药</h2>
    <el-card>
      <el-table :data="list" border stripe v-loading="loading" empty-text="暂无待审核处方" size="small">
        <el-table-column prop="id" label="处方号" width="80" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="doctorName" label="开方医生" width="100" />
        <el-table-column prop="diagnosis" label="诊断" min-width="140" show-overflow-tooltip />
        <el-table-column label="金额(元)" width="90">
          <template #default="{ row }">{{ row.totalAmount || '0.00' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">{{ row.createTime }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="info" @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 0" size="small" type="success" @click="audit(row, true)">通过</el-button>
            <el-button v-if="row.status === 0" size="small" type="danger" @click="audit(row, false)">驳回</el-button>
            <el-button v-if="row.status === 1" size="small" type="primary" @click="dispense(row)">发药</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 处方详情弹窗 -->
    <el-dialog v-model="detailVisible" title="📋 处方详情" width="650px">
      <el-descriptions v-if="currentRx" :column="2" border size="small">
        <el-descriptions-item label="处方号">{{ currentRx.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(currentRx.status)" size="small">{{ statusMap[currentRx.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="患者">{{ currentRx.patientName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="医生">{{ currentRx.doctorName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="诊断" :span="2">{{ currentRx.diagnosis || '--' }}</el-descriptions-item>
        <el-descriptions-item label="审核药师">{{ currentRx.pharmacistName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="审核意见">{{ currentRx.auditOpinion || '--' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">药品明细</el-divider>
      <el-table v-if="currentRx" :data="currentRx.items || []" border stripe size="small">
        <el-table-column prop="drugName" label="药品名称" width="180" />
        <el-table-column prop="dosage" label="用量" width="100" />
        <el-table-column prop="usageMethod" label="用法" width="140" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="unitPrice" label="单价(元)" width="100" />
        <el-table-column label="小计(元)" width="100">
          <template #default="{ row }">{{ ((row.unitPrice || 0) * (row.quantity || 0)).toFixed(2) }}</template>
        </el-table-column>
      </el-table>

      <div v-if="currentRx" style="margin-top:16px;text-align:right;font-size:16px;font-weight:bold">
        合计：¥{{ currentRx.totalAmount || '0.00' }}
      </div>
    </el-dialog>
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
const detailVisible = ref(false)
const currentRx = ref(null)

const statusMap = { 0: '待审核', 1: '已审核', 2: '已发药', 3: '已驳回' }
const statusTag = (s) => ({ 0:'warning',1:'primary',2:'success',3:'danger' }[s]||'info')

async function load() {
  loading.value = true
  try { list.value = await getPendingPrescriptions() } catch {} finally { loading.value = false }
}

function viewDetail(row) {
  currentRx.value = row
  detailVisible.value = true
}

async function audit(row, approved) {
  await auditPrescription(row.id, userStore.userId, approved, approved ? '审核通过' : '审核驳回')
  ElMessage.success(approved ? '已通过' : '已驳回')
  detailVisible.value = false
  load()
}

async function dispense(row) {
  await dispensePrescription(row.id, userStore.userId)
  ElMessage.success('发药成功，库存已自动扣减')
  detailVisible.value = false
  load()
}

onMounted(load)
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
