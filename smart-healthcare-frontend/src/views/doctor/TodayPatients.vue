<template>
  <div>
    <h2 class="page-title">📋 今日待诊列表</h2>
    <el-card>
      <el-table :data="list" border stripe v-loading="loading" empty-text="暂无待诊患者">
        <el-table-column prop="id" label="号序" width="80" />
        <el-table-column prop="patientName" label="患者姓名" width="120" />
        <el-table-column prop="patientPhone" label="联系电话" width="140" />
        <el-table-column prop="departmentName" label="科室" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="挂号时间" width="180" />
        <el-table-column label="操作" min-width="200">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="primary" size="small" @click="handleCall(row)">呼叫</el-button>
            <el-button v-if="row.status === 2" type="success" size="small" @click="handleComplete(row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getTodayAppointments, callNext, completeAppointment } from '@/api/doctor'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const list = ref([])
const loading = ref(false)

function statusType(s) { return { 0: 'info', 1: 'warning', 2: 'primary', 3: 'success' }[s] || 'info' }
function statusText(s) { return { 0: '已取消', 1: '待就诊', 2: '就诊中', 3: '已完成' }[s] || '未知' }

async function load() {
  loading.value = true
  try { list.value = await getTodayAppointments(userStore.userId) } catch {} finally { loading.value = false }
}

async function handleCall(row) {
  await callNext(row.id)
  ElMessage.success('已呼叫')
  load()
}

async function handleComplete(row) {
  await completeAppointment(row.id)
  ElMessage.success('就诊完成')
  load()
}

onMounted(load)
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
