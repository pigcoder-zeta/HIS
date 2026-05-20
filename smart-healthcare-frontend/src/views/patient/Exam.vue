<template>
  <div>
    <h2 class="page-title">🔬 体检预约</h2>
    <el-card>
      <h3>体检套餐</h3>
      <el-row :gutter="16">
        <el-col v-for="pkg in packages" :key="pkg.id" :span="8" style="margin-bottom:16px">
          <el-card shadow="hover">
            <h4>{{ pkg.name }}</h4>
            <p style="color:#999">{{ pkg.description }}</p>
            <p style="color:#f56c6c;font-weight:bold">¥{{ pkg.price }}</p>
            <el-date-picker v-model="dates[pkg.id]" type="date" placeholder="选择日期" style="width:100%;margin:8px 0" />
            <el-button type="primary" style="width:100%" @click="bookExam(pkg.id)">立即预约</el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card style="margin-top:20px">
      <template #header><span style="font-weight:bold">我的体检预约</span></template>
      <el-table :data="exams" border stripe v-loading="loading" empty-text="暂无体检预约">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="packageName" label="套餐" width="200" />
        <el-table-column prop="examDate" label="体检日期" width="140" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '已完成' : row.status === 0 ? '已取消' : '待体检' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button v-if="row.status !== 0 && row.status !== 1" type="danger" size="small" @click="cancelExam(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getExamPackages, bookExam as bookExamApi, getExams, cancelExam as cancelExamApi } from '@/api/patient'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const packages = ref([])
const exams = ref([])
const dates = reactive({})
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    packages.value = await getExamPackages()
    exams.value = await getExams(userStore.userId)
  } catch {} finally { loading.value = false }
}

async function bookExam(packageId) {
  const date = dates[packageId]
  if (!date) { ElMessage.warning('请选择日期'); return }
  try {
    await bookExamApi(userStore.userId, packageId, date)
    ElMessage.success('体检预约成功')
    load()
  } catch {}
}

async function cancelExam(row) {
  await cancelExamApi(row.id, userStore.userId)
  ElMessage.success('已取消')
  load()
}

onMounted(load)
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
