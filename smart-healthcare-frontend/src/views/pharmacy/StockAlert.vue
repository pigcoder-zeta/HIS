<template>
  <div>
    <h2 class="page-title">⚠️ 库存预警</h2>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header><span style="font-weight:bold;color:#f56c6c">低库存药品</span></template>
          <el-table :data="lowStock" border stripe empty-text="无低库存药品">
            <el-table-column prop="name" label="药品名" />
            <el-table-column prop="stock" label="库存" width="80">
              <template #default="{ row }"><span style="color:#f56c6c;font-weight:bold">{{ row.stock }}</span></template>
            </el-table-column>
            <el-table-column prop="spec" label="规格" width="100" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span style="font-weight:bold;color:#e6a23c">临期药品</span></template>
          <el-table :data="nearExpiry" border stripe empty-text="无临期药品">
            <el-table-column prop="name" label="药品名" />
            <el-table-column prop="expireDate" label="有效期" width="120">
              <template #default="{ row }"><span style="color:#e6a23c;font-weight:bold">{{ row.expireDate }}</span></template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="80" />
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

onMounted(async () => {
  try { lowStock.value = await getLowStockDrugs() } catch {}
  try { nearExpiry.value = await getNearExpiryDrugs() } catch {}
})
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
