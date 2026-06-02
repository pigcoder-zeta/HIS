<template>
  <div>
    <h2 class="page-title">📅 排班管理</h2>
    <el-card>
      <el-form inline>
        <el-form-item label="科室">
          <el-select v-model="deptId" placeholder="选择科室" @change="loadSchedules">
            <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="到" @change="loadSchedules" />
        </el-form-item>
        <el-form-item><el-button type="primary" @click="showCreate=true">创建排班</el-button></el-form-item>
      </el-form>
      <el-table :data="schedules" border stripe v-loading="loading" :empty-text="deptId ? '暂无排班数据' : '请选择科室和日期'">
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="deptName" label="科室" width="120" />
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column label="时段" width="80">
          <template #default="{ row }"><el-tag size="small">{{ row.timeSlot === 1 ? '上午' : '下午' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="totalQuota" label="号源" width="80" />
        <el-table-column label="已约" width="80">
          <template #default="{ row }">{{ (row.totalQuota || 0) - (row.leftQuota || 0) }}</template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="cancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreate" title="创建排班" width="450px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="科室"><el-select v-model="form.deptId" style="width:100%" @change="loadDoctors(form.deptId)"><el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id" /></el-select></el-form-item>
        <el-form-item label="医生"><el-select v-model="form.doctorId" style="width:100%"><el-option v-for="d in doctors" :key="d.id" :label="d.realName" :value="d.id" /></el-select></el-form-item>
        <el-form-item label="日期"><el-date-picker v-model="form.date" type="date" style="width:100%" /></el-form-item>
        <el-form-item label="时段"><el-radio-group v-model="form.period"><el-radio value="am">上午</el-radio><el-radio value="pm">下午</el-radio></el-radio-group></el-form-item>
        <el-form-item label="号源"><el-input-number v-model="form.maxPatients" :min="1" :max="100" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="doCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getDepartments, getDoctors, getSchedules, createSchedule, cancelSchedule } from '@/api/medical'
import { ElMessage } from 'element-plus'

const departments = ref([])
const doctors = ref([])
const schedules = ref([])
const deptId = ref(null)
const dateRange = ref([])
const loading = ref(false)
const showCreate = ref(false)
const form = reactive({ deptId: null, doctorId: null, date: '', period: 'am', maxPatients: 30 })

async function loadDepts() { try { departments.value = await getDepartments() } catch {} }
async function loadDoctors(dept) { try { doctors.value = await getDoctors(dept) } catch {} }

async function loadSchedules() {
  if (!deptId.value || !dateRange.value?.length) return
  loading.value = true
  try {
    const fmt = (d) => { const dt = new Date(d); return dt.getFullYear()+'-'+String(dt.getMonth()+1).padStart(2,'0')+'-'+String(dt.getDate()).padStart(2,'0') }
    schedules.value = await getSchedules({
      deptId: deptId.value,
      startDate: fmt(dateRange.value[0]),
      endDate: fmt(dateRange.value[1]),
    })
  } catch {} finally { loading.value = false }
}

async function doCreate() {
  try {
    const params = {
      doctorId: form.doctorId,
      deptId: form.deptId,
      scheduleDate: typeof form.date === 'string' ? form.date : form.date.toISOString().slice(0, 10),
      timeSlot: form.period === 'am' ? 1 : 2,
      totalQuota: form.maxPatients,
    }
    await createSchedule(params)
    ElMessage.success('排班创建成功')
    showCreate.value = false
    // 自动设置顶部筛选条件，让刚创建的排班显示出来
    deptId.value = form.deptId
    const d = new Date(params.scheduleDate)
    const startOfWeek = new Date(d); startOfWeek.setDate(d.getDate() - d.getDay() + 1)
    const endOfWeek = new Date(d); endOfWeek.setDate(d.getDate() - d.getDay() + 7)
    dateRange.value = [startOfWeek, endOfWeek]
    loadSchedules()
  } catch {}
}

async function cancel(row) {
  try {
    await cancelSchedule(row.id)
    ElMessage.success('已取消')
    // 等待后端处理完成，重新加载排班列表
    setTimeout(() => loadSchedules(), 100)
  } catch (e) {
    ElMessage.error('取消排班失败')
    console.error('取消排班失败', e)
  }
}

onMounted(loadDepts)
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
