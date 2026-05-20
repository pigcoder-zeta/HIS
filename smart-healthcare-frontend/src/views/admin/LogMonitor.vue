<template>
  <div>
    <h2 class="page-title">📋 系统日志</h2>
    <el-card>
      <el-table :data="page.records" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="action" label="操作" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.result === 1 ? 'success' : 'danger'">{{ row.result === 1 ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
      <el-pagination
        style="margin-top:16px;justify-content:flex-end"
        v-model:current-page="current" :page-size="size" :total="page.total"
        layout="total, prev, pager, next" @current-change="load"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLogPage } from '@/api/admin'

const page = ref({ records: [], total: 0 })
const current = ref(1)
const size = ref(20)
const loading = ref(false)

async function load() {
  loading.value = true
  try { page.value = await getLogPage({ current: current.value, size: size.value }) } catch {} finally { loading.value = false }
}

onMounted(load)
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
