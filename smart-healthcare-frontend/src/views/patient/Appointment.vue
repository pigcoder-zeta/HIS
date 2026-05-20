<template>
  <div>
    <h2 class="page-title">🏥 预约挂号</h2>
    <el-card>
      <el-form :model="form" label-width="100px" style="max-width:500px">
        <el-form-item label="选择排班" required>
          <el-select v-model="form.scheduleId" placeholder="请选择排班" style="width:100%">
            <el-option v-for="s in schedules" :key="s.id" :label="`${s.doctorName} - ${s.deptName} - ${s.scheduleDate} ${slotLabel(s.timeSlot)}`" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="booking" @click="book">确认预约</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top:20px">
      <template #header><span style="font-weight:bold">我的预约列表</span></template>
      <el-table :data="appointments" border stripe v-loading="loading" empty-text="暂无预约">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="doctorName" label="医生" width="120" />
        <el-table-column prop="departmentName" label="科室" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预约时间" width="180" />
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="danger" size="small" @click="cancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getSchedules } from '@/api/medical'
import { bookAppointment, getAppointments, cancelAppointment } from '@/api/patient'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const schedules = ref([])
const appointments = ref([])
const form = reactive({ scheduleId: null })
const booking = ref(false)
const loading = ref(false)

function statusType(s) { return { 0:'info',1:'warning',2:'primary',3:'success' }[s]||'info' }
function statusText(s) { return { 0:'已取消',1:'待就诊',2:'就诊中',3:'已完成' }[s]||'未知' }
function slotLabel(ts) { return { 1: '上午', 2: '下午', 3: '夜诊' }[ts] || '' }

async function loadSchedules() {
  const today = new Date().toISOString().slice(0, 10)
  const end = new Date(Date.now() + 7*86400000).toISOString().slice(0,10)
  try { schedules.value = await getSchedules({ deptId: 0, startDate: today, endDate: end }) } catch { schedules.value = [] }
}

async function loadAppointments() {
  loading.value = true
  try { appointments.value = await getAppointments(userStore.userId) } catch {} finally { loading.value = false }
}

async function book() {
  if (!form.scheduleId) { ElMessage.warning('请选择排班'); return }
  booking.value = true
  try {
    await bookAppointment({ patientId: userStore.userId, scheduleId: form.scheduleId })
    ElMessage.success('预约成功')
    loadAppointments()
  } catch {} finally { booking.value = false }
}

async function cancel(row) {
  await cancelAppointment(row.id, userStore.userId)
  ElMessage.success('已取消')
  loadAppointments()
}

onMounted(() => { loadSchedules(); loadAppointments() })
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
