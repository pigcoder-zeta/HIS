<template>
  <div>
    <h2 class="page-title">⚠️ 库存预警</h2>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span style="font-weight:bold;color:#f56c6c">📉 低库存药品</span>
              <el-tag type="danger">{{ lowStock.length }}种</el-tag>
            </div>
          </template>
          <el-table :data="lowStock" border stripe size="small" empty-text="✅ 无低库存药品" max-height="400">
            <el-table-column prop="drugName" label="药品名称" width="160" />
            <el-table-column prop="drugCode" label="编码" width="110" />
            <el-table-column prop="stockCount" label="当前库存" width="90">
              <template #default="{ row }">
                <span :style="{ color: row.stockCount === 0 ? '#f56c6c' : '#e6a23c', fontWeight: 'bold' }">{{ row.stockCount }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="safeThreshold" label="安全阈值" width="90" />
            <el-table-column prop="specification" label="规格" width="110" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button size="small" type="primary" @click="$router.push('/pharmacy/drugs')">补货</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span style="font-weight:bold;color:#e6a23c">⏰ 临期药品（90天内）</span>
              <el-tag type="warning">{{ nearExpiry.length }}种</el-tag>
            </div>
          </template>
          <el-table :data="nearExpiry" border stripe size="small" empty-text="✅ 无临期药品" max-height="400">
            <el-table-column prop="drugName" label="药品名称" width="160" />
            <el-table-column prop="drugCode" label="编码" width="110" />
            <el-table-column prop="expiryDate" label="有效期至" width="110">
              <template #default="{ row }">
                <span :style="{ color: daysLeft(row.expiryDate) <= 30 ? '#f56c6c' : '#e6a23c', fontWeight: 'bold' }">
                  {{ row.expiryDate }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="剩余天数" width="90">
              <template #default="{ row }">
                <el-tag :type="daysLeft(row.expiryDate) <= 30 ? 'danger' : 'warning'" size="small">
                  {{ daysLeft(row.expiryDate) }}天
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="stockCount" label="库存" width="70" />
            <el-table-column label="操作" width="80">
              <template #default="{ row }">
                <el-button size="small" type="warning" @click="$router.push('/pharmacy/drugs')">处理</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLowStockDrugs, getNearExpiryDrugs } from '@/api/pharmacy'

const lowStock = ref([])
const nearExpiry = ref([])

function daysLeft(dateStr) {
  if (!dateStr) return 0
  const diff = new Date(dateStr) - new Date()
  return Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)))
}

onMounted(async () => {
  try { lowStock.value = await getLowStockDrugs() } catch {}
  try { nearExpiry.value = await getNearExpiryDrugs() } catch {}
})
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
