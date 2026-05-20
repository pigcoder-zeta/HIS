<template>
  <div>
    <h2 class="page-title">📂 我的病历</h2>
    <el-card v-loading="loading">
      <el-timeline>
        <el-timeline-item v-for="r in records" :key="r.id" :timestamp="r.createTime" placement="top">
          <el-card shadow="hover">
            <p><b>就诊医生：</b>{{ r.doctorName || '未知' }}</p>
            <p><b>主诉：</b>{{ r.chiefComplaint }}</p>
            <p><b>诊断：</b>{{ r.diagnosis }}</p>
            <p><b>医嘱：</b>{{ r.advice || '无' }}</p>
            <el-tag :type="r.status === 1 ? 'success' : 'info'" size="small">{{ r.status === 1 ? '已归档' : '草稿' }}</el-tag>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-if="!records.length" description="暂无病历记录" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getRecords } from '@/api/patient'

const userStore = useUserStore()
const records = ref([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try { records.value = await getRecords(userStore.userId) } catch {} finally { loading.value = false }
})
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
