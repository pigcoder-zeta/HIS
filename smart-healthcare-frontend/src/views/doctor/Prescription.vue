<template>
  <div>
    <h2 class="page-title">💊 开具处方</h2>
    <el-card>
      <el-form :model="form" label-width="100px" style="max-width:800px">
        <el-form-item label="患者ID" required>
          <el-input-number v-model="form.patientId" :min="1" />
        </el-form-item>
        <el-form-item label="诊断" required>
          <el-input v-model="form.diagnosis" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="处方药品">
          <div v-for="(item, idx) in form.items" :key="idx" style="margin-bottom:8px;display:flex;gap:8px;align-items:center">
            <el-input v-model="item.drugName" placeholder="药品名" style="width:180px" />
            <el-input-number v-model="item.quantity" :min="1" placeholder="数量" style="width:100px" />
            <el-input v-model="item.unit" placeholder="单位" style="width:80px" />
            <el-input v-model="item.usage" placeholder="用法" style="width:200px" />
            <el-button type="danger" circle size="small" :icon="Delete" @click="form.items.splice(idx,1)" />
          </div>
          <el-button type="primary" @click="form.items.push({ drugName:'',quantity:1,unit:'盒',usage:'' })">+ 添加药品</el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="submitting" @click="submit">开具处方</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useUserStore } from '@/store/user'
import { createPrescription } from '@/api/doctor'
import { ElMessage } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'

const userStore = useUserStore()
const form = reactive({ patientId: null, diagnosis: '', items: [{ drugName: '', quantity: 1, unit: '盒', usage: '' }] })
const submitting = ref(false)

async function submit() {
  submitting.value = true
  try {
    await createPrescription(userStore.userId, form)
    ElMessage.success('处方已开具')
    form.patientId = null
    form.diagnosis = ''
    form.items = [{ drugName: '', quantity: 1, unit: '盒', usage: '' }]
  } catch {} finally { submitting.value = false }
}
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
