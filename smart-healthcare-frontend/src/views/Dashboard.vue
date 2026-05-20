<template>
  <div class="dashboard">
    <h2 class="page-title">📊 工作台</h2>

    <!-- ==================== 概览卡片 ==================== -->
    <el-row :gutter="20" class="overview-cards">
      <el-col v-for="card in overviewCards" :key="card.label" :span="6">
        <el-card shadow="hover" class="stat-card" :class="card.colorClass">
          <div class="stat-icon">{{ card.icon }}</div>
          <div class="stat-info">
            <div class="stat-value">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ==================== 管理员视图 ==================== -->
    <template v-if="userStore.roleId === 1">
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="12">
          <el-card>
            <template #header><span class="card-title">📈 用户增长趋势（近7天）</span></template>
            <v-chart :option="userTrendOption" style="height:320px" autoresize />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header><span class="card-title">🏥 今日各科室就诊量</span></template>
            <v-chart :option="deptVisitOption" style="height:320px" autoresize />
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="12">
          <el-card>
            <template #header><span class="card-title">✅ 接口调用成功率（24h）</span></template>
            <v-chart :option="apiSuccessOption" style="height:280px" autoresize />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header><span class="card-title">⚡ 快捷操作</span></template>
            <el-row :gutter="12">
              <el-col :span="12" v-for="btn in adminQuickBtns" :key="btn.label" style="margin-bottom:12px">
                <el-button :type="btn.type" style="width:100%;height:80px;font-size:15px" @click="$router.push(btn.path)">
                  <div>{{ btn.icon }}</div>
                  <div style="margin-top:4px">{{ btn.label }}</div>
                </el-button>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ==================== 医生视图 ==================== -->
    <template v-if="userStore.roleId === 2">
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="8">
          <el-card shadow="hover" class="big-stat-card card-warning" @click="$router.push('/doctor/consultation')">
            <div class="big-stat-value">{{ doctorData.waitingCount }}</div>
            <div class="big-stat-label">待诊患者</div>
            <div class="big-stat-hint">点击查看排队队列 →</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="big-stat-card card-success">
            <div class="big-stat-value">{{ doctorData.treatedCount }}</div>
            <div class="big-stat-label">今日已诊</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover" class="big-stat-card card-primary">
            <div class="big-stat-value">{{ doctorData.todayPrescriptions }}</div>
            <div class="big-stat-label">今日处方</div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="12">
          <el-card>
            <template #header><span class="card-title">📅 本月排班</span></template>
            <el-calendar v-model="calendarDate">
              <template #date-cell="{ data }">
                <div class="calendar-cell" :class="{ 'has-schedule': isScheduledDate(data.day) }">
                  {{ data.day.split('-').slice(2).join('-') }}
                  <span v-if="isScheduledDate(data.day)" class="schedule-dot"></span>
                </div>
              </template>
            </el-calendar>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header><span class="card-title">⚡ 快捷操作</span></template>
            <el-row :gutter="12">
              <el-col :span="12" v-for="btn in doctorQuickBtns" :key="btn.label" style="margin-bottom:12px">
                <el-button :type="btn.type" style="width:100%;height:80px;font-size:15px" @click="$router.push(btn.path)">
                  <div>{{ btn.icon }}</div>
                  <div style="margin-top:4px">{{ btn.label }}</div>
                </el-button>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ==================== 药房视图 ==================== -->
    <template v-if="userStore.roleId === 3">
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="6" v-for="card in pharmacyCards" :key="card.label">
          <el-card shadow="hover" class="big-stat-card" :class="card.colorClass" @click="card.path && $router.push(card.path)">
            <div class="big-stat-value">{{ card.value }}</div>
            <div class="big-stat-label">{{ card.label }}</div>
            <div v-if="card.path" class="big-stat-hint">点击查看详情 →</div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="24">
          <el-card>
            <template #header><span class="card-title">🚨 库存预警滚动列表</span></template>
            <el-table :data="pharmacyAlerts" stripe size="small" max-height="320">
              <el-table-column prop="drugName" label="药品名称" width="200" />
              <el-table-column prop="stockCount" label="当前库存" width="100" />
              <el-table-column prop="safeThreshold" label="安全阈值" width="100" />
              <el-table-column prop="alertType" label="预警类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.alertLevel === 'danger' ? 'danger' : 'warning'" size="small">{{ row.alertType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="expiryDate" label="有效期至" width="120" />
            </el-table>
            <el-empty v-if="!pharmacyAlerts.length" description="暂无预警，库存状态良好 ✅" />
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="24">
          <el-card>
            <template #header><span class="card-title">⚡ 快捷操作</span></template>
            <el-row :gutter="12">
              <el-col :span="6" v-for="btn in pharmacyQuickBtns" :key="btn.label">
                <el-button :type="btn.type" style="width:100%;height:80px;font-size:15px" @click="$router.push(btn.path)">
                  <div>{{ btn.icon }}</div>
                  <div style="margin-top:4px">{{ btn.label }}</div>
                </el-button>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ==================== 医务科视图 ==================== -->
    <template v-if="userStore.roleId === 4">
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="6" v-for="card in medicalCards" :key="card.label">
          <el-card shadow="hover" class="big-stat-card" :class="card.colorClass">
            <div class="big-stat-value">{{ card.value }}</div>
            <div class="big-stat-label">{{ card.label }}</div>
          </el-card>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top:20px">
        <el-col :span="24">
          <el-card>
            <template #header><span class="card-title">⚡ 快捷操作</span></template>
            <el-row :gutter="12">
              <el-col :span="6" v-for="btn in medicalQuickBtns" :key="btn.label">
                <el-button :type="btn.type" style="width:100%;height:80px;font-size:15px" @click="$router.push(btn.path)">
                  <div>{{ btn.icon }}</div>
                  <div style="margin-top:4px">{{ btn.label }}</div>
                </el-button>
              </el-col>
            </el-row>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- ==================== 患者视图 ==================== -->
    <template v-if="userStore.roleId === 5">
      <el-card style="margin-top:20px">
        <template #header><span style="font-weight:bold">快捷导航</span></template>
        <el-row :gutter="16">
          <el-col :span="6"><el-button type="primary" style="width:100%;margin:4px 0;height:60px" @click="$router.push('/patient/appointment')">🏥 预约挂号</el-button></el-col>
          <el-col :span="6"><el-button type="success" style="width:100%;margin:4px 0;height:60px" @click="$router.push('/patient/records')">📋 我的病历</el-button></el-col>
          <el-col :span="6"><el-button type="warning" style="width:100%;margin:4px 0;height:60px" @click="$router.push('/patient/exam')">🔬 体检预约</el-button></el-col>
          <el-col :span="6"><el-button type="info" style="width:100%;margin:4px 0;height:60px" @click="$router.push('/ai/triage')">🤖 AI 智能导诊</el-button></el-col>
        </el-row>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/store/user'
import {
  getAdminOverview, getUserTrend, getDeptVisitToday, getApiSuccessRate,
  getDoctorOverview, getDoctorScheduleCalendar,
  getPharmacyOverview, getPharmacyAlerts,
  getMedicalOverview
} from '@/api/dashboard'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, BarChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const userStore = useUserStore()
const calendarDate = ref(new Date())

// ==================== 数据状态 ====================
const adminOverview = ref({})
const userTrendData = ref([])
const deptVisitData = ref([])
const apiSuccessData = ref({})
const doctorData = ref({ waitingCount: 0, treatedCount: 0, todayPrescriptions: 0 })
const scheduleDates = ref([])
const pharmacyOverview = ref({})
const pharmacyAlerts = ref([])
const medicalOverview = ref({})

// ==================== 概览卡片（通用） ====================
const overviewCards = computed(() => {
  const r = userStore.roleId
  const d = new Date().toISOString().slice(0, 10)
  if (r === 1) return [
    { icon: '👥', value: adminOverview.value.totalUsers || 0, label: '系统总用户', colorClass: 'card-blue' },
    { icon: '👨‍⚕️', value: adminOverview.value.totalDoctors || 0, label: '医生总数', colorClass: 'card-green' },
    { icon: '📋', value: adminOverview.value.todayAppointments || 0, label: '今日就诊量', colorClass: 'card-orange' },
    { icon: '⚠️', value: adminOverview.value.errorLogs || 0, label: '24h异常日志', colorClass: 'card-red' },
  ]
  if (r === 2) return [
    { icon: '👤', value: userStore.userInfo?.realName || '', label: userStore.roleName, colorClass: 'card-blue' },
    { icon: '📅', value: d, label: '今日日期', colorClass: 'card-green' },
    { icon: '⏳', value: doctorData.value.waitingCount, label: '待诊排队', colorClass: 'card-orange' },
    { icon: '✅', value: doctorData.value.treatedCount, label: '今日已诊', colorClass: 'card-teal' },
  ]
  if (r === 3) return [
    { icon: '💊', value: pharmacyOverview.value.pendingAuditCount || 0, label: '待审处方', colorClass: 'card-orange' },
    { icon: '📦', value: pharmacyOverview.value.todayDispensedCount || 0, label: '今日发药', colorClass: 'card-blue' },
    { icon: '⚠️', value: pharmacyOverview.value.lowStockCount || 0, label: '低库存预警', colorClass: 'card-red' },
    { icon: '⏰', value: pharmacyOverview.value.nearExpiryCount || 0, label: '临期药品', colorClass: 'card-yellow' },
  ]
  if (r === 4) return [
    { icon: '🏥', value: userStore.userInfo?.realName || '', label: '医务管理员', colorClass: 'card-blue' },
    { icon: '📅', value: d, label: '今日日期', colorClass: 'card-green' },
    { icon: '📋', value: medicalOverview.value.todaySchedules || 0, label: '今日排班', colorClass: 'card-orange' },
    { icon: '👥', value: medicalOverview.value.todayAppointments || 0, label: '今日就诊', colorClass: 'card-teal' },
  ]
  if (r === 5) return [
    { icon: '🏥', value: '智慧医疗', label: '管理系统', colorClass: 'card-blue' },
    { icon: '👤', value: userStore.userInfo?.realName || '', label: '患者', colorClass: 'card-green' },
    { icon: '📅', value: d, label: '今日日期', colorClass: 'card-orange' },
    { icon: '🤖', value: 'AI导诊', label: '智能就医', colorClass: 'card-teal' },
  ]
  return []
})

// ==================== 管理员图表配置 ====================
const userTrendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: userTrendData.value.map(i => i.date) },
  yAxis: { type: 'value', name: '用户数' },
  series: [{
    name: '累计用户', type: 'line', smooth: true,
    data: userTrendData.value.map(i => i.count),
    areaStyle: { color: 'rgba(64,158,255,0.15)' },
    itemStyle: { color: '#409eff' }
  }]
}))

const deptVisitOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  xAxis: { type: 'category', data: deptVisitData.value.map(i => i.deptName), axisLabel: { rotate: 30 } },
  yAxis: { type: 'value', name: '就诊人次' },
  series: [{
    name: '就诊量', type: 'bar',
    data: deptVisitData.value.map(i => i.count),
    itemStyle: { borderRadius: [6, 6, 0, 0], color: '#67c23a' }
  }]
}))

const apiSuccessOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    name: '接口调用', type: 'pie', radius: ['50%', '75%'],
    label: { show: true, formatter: '{b}\n{d}%' },
    data: [
      { value: apiSuccessData.value.success || 0, name: '成功', itemStyle: { color: '#67c23a' } },
      { value: apiSuccessData.value.fail || 0, name: '失败', itemStyle: { color: '#f56c6c' } }
    ]
  }]
}))

// ==================== 快捷按钮 ====================
const adminQuickBtns = [
  { icon: '👥', label: '用户管理', type: 'primary', path: '/admin/users' },
  { icon: '📋', label: '日志监控', type: 'warning', path: '/admin/logs' },
  { icon: '📅', label: '排班管理', type: 'success', path: '/medical/schedule' },
  { icon: '🏥', label: '科室管理', type: 'info', path: '/medical/departments' },
]
const doctorQuickBtns = [
  { icon: '🩺', label: '接诊工作台', type: 'primary', path: '/doctor/consultation' },
  { icon: '📋', label: '今日待诊', type: 'warning', path: '/doctor/today' },
  { icon: '📝', label: '电子病历', type: 'primary', path: '/doctor/record' },
  { icon: '💊', label: '开具处方', type: 'success', path: '/doctor/prescription' },
]
const pharmacyQuickBtns = [
  { icon: '📋', label: '处方审核', type: 'warning', path: '/pharmacy/prescriptions' },
  { icon: '💊', label: '药品管理', type: 'primary', path: '/pharmacy/drugs' },
  { icon: '⚠️', label: '库存预警', type: 'danger', path: '/pharmacy/stock' },
  { icon: '📦', label: '入库操作', type: 'success', path: '/pharmacy/drugs' },
]
const medicalQuickBtns = [
  { icon: '📅', label: '排班管理', type: 'primary', path: '/medical/schedule' },
  { icon: '🏥', label: '科室管理', type: 'success', path: '/medical/departments' },
]

// 药房统计卡片
const pharmacyCards = computed(() => [
  { label: '待审处方', value: pharmacyOverview.value.pendingAuditCount || 0, colorClass: 'card-orange', path: '/pharmacy/prescriptions' },
  { label: '今日发药', value: pharmacyOverview.value.todayDispensedCount || 0, colorClass: 'card-blue' },
  { label: '低库存预警', value: pharmacyOverview.value.lowStockCount || 0, colorClass: 'card-red', path: '/pharmacy/stock' },
  { label: '临期药品', value: pharmacyOverview.value.nearExpiryCount || 0, colorClass: 'card-yellow', path: '/pharmacy/stock' },
])

const medicalCards = computed(() => [
  { label: '科室总数', value: medicalOverview.value.totalDepts || 0, colorClass: 'card-blue' },
  { label: '医生总数', value: medicalOverview.value.totalDoctors || 0, colorClass: 'card-green' },
  { label: '今日排班', value: medicalOverview.value.todaySchedules || 0, colorClass: 'card-orange' },
  { label: '今日就诊', value: medicalOverview.value.todayAppointments || 0, colorClass: 'card-teal' },
])

// ==================== 日历辅助 ====================
function isScheduledDate(dateStr) {
  return scheduleDates.value.includes(dateStr)
}

// ==================== 数据加载 ====================
async function loadAdminData() {
  try { adminOverview.value = await getAdminOverview() } catch {}
  try { userTrendData.value = await getUserTrend() } catch {}
  try { deptVisitData.value = await getDeptVisitToday() } catch {}
  try { apiSuccessData.value = await getApiSuccessRate() } catch {}
}

async function loadDoctorData() {
  try { doctorData.value = await getDoctorOverview(userStore.userId) } catch {}
  try {
    const month = new Date().toISOString().slice(0, 7)
    const schedules = await getDoctorScheduleCalendar(userStore.userId, month)
    scheduleDates.value = schedules.map(s => s.date)
  } catch {}
}

async function loadPharmacyData() {
  try { pharmacyOverview.value = await getPharmacyOverview() } catch {}
  try { pharmacyAlerts.value = await getPharmacyAlerts() } catch {}
}

async function loadMedicalData() {
  try { medicalOverview.value = await getMedicalOverview() } catch {}
}

// ==================== 生命周期 ====================
onMounted(() => {
  const r = userStore.roleId
  if (r === 1) loadAdminData()
  else if (r === 2) loadDoctorData()
  else if (r === 3) loadPharmacyData()
  else if (r === 4) loadMedicalData()
})
</script>

<style scoped>
.dashboard { padding-bottom: 30px; }
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }

/* 概览卡片 */
.stat-card {
  display: flex; align-items: center; gap: 16px;
  padding: 20px; cursor: default;
}
.stat-icon { font-size: 40px; }
.stat-info { flex: 1; }
.stat-value { font-size: 26px; font-weight: bold; color: #333; }
.stat-label { font-size: 13px; color: #999; margin-top: 4px; }

/* 大统计卡片 */
.big-stat-card { text-align: center; padding: 24px 0; cursor: default; }
.big-stat-value { font-size: 40px; font-weight: bold; }
.big-stat-label { font-size: 15px; color: #666; margin-top: 8px; }
.big-stat-hint { font-size: 12px; color: #999; margin-top: 8px; }

/* 卡片颜色 */
.card-blue .stat-value, .card-blue .big-stat-value { color: #409eff; }
.card-green .stat-value, .card-green .big-stat-value { color: #67c23a; }
.card-orange .stat-value, .card-orange .big-stat-value { color: #e6a23c; }
.card-red .stat-value, .card-red .big-stat-value { color: #f56c6c; }
.card-teal .stat-value, .card-teal .big-stat-value { color: #20a39e; }
.card-yellow .stat-value, .card-yellow .big-stat-value { color: #f0c040; }
.card-warning { border-left: 4px solid #e6a23c; }
.card-success { border-left: 4px solid #67c23a; }
.card-primary { border-left: 4px solid #409eff; }

/* 图表标题 */
.card-title { font-weight: bold; font-size: 15px; }

/* 日历样式 */
.calendar-cell { position: relative; text-align: center; }
.has-schedule { color: #409eff; font-weight: bold; }
.schedule-dot {
  display: block; width: 6px; height: 6px;
  background: #67c23a; border-radius: 50%;
  margin: 2px auto 0;
}
</style>
