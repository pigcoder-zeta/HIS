<template>
  <div>
    <h2 class="page-title">💊 药品管理</h2>
    <el-card>
      <div style="margin-bottom:16px;display:flex;gap:12px">
        <el-input v-model="keyword" placeholder="搜索药品" style="width:240px" clearable @clear="load" @keyup.enter="load" />
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button type="success" @click="openAdd">新增药品</el-button>
      </div>
      <el-table :data="page.records" border stripe v-loading="loading">
        <el-table-column prop="drugCode" label="编码" width="100" />
        <el-table-column prop="drugName" label="药品名称" width="160" />
        <el-table-column prop="genericName" label="通用名" width="140" show-overflow-tooltip />
        <el-table-column prop="specification" label="规格" width="120" />
        <el-table-column prop="stockCount" label="库存" width="80">
          <template #default="{ row }">
            <el-tag :type="row.stockCount <= row.safeThreshold ? 'danger' : 'success'" size="small">{{ row.stockCount }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="safeThreshold" label="安全线" width="70" />
        <el-table-column prop="unitPrice" label="单价(元)" width="100" />
        <el-table-column prop="expiryDate" label="有效期至" width="110">
          <template #default="{ row }">
            <span :style="{ color: isNearExpiry(row.expiryDate) ? '#e6a23c' : '#333' }">{{ row.expiryDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="editDrug(row)">编辑</el-button>
            <el-button size="small" text type="success" @click="openStockIn(row)">入库</el-button>
            <el-button size="small" text type="warning" @click="openStockOut(row)">出库</el-button>
            <el-button size="small" text type="info" @click="viewTransactions(row)">流水</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top:16px;justify-content:flex-end"
        v-model:current-page="current" :page-size="size" :total="page.total"
        layout="total, prev, pager, next" @current-change="load"
      />
    </el-card>

    <el-dialog v-model="showAdd" :title="editingId ? '编辑药品' : '新增药品'" width="550px" destroy-on-close>
      <el-form :model="drugForm" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="编码"><el-input v-model="drugForm.drugCode" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="名称"><el-input v-model="drugForm.drugName" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="通用名"><el-input v-model="drugForm.genericName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="分类"><el-input v-model="drugForm.category" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="规格"><el-input v-model="drugForm.specification" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="单位"><el-input v-model="drugForm.unit" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="单价"><el-input-number v-model="drugForm.unitPrice" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="安全线"><el-input-number v-model="drugForm.safeThreshold" :min="0" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="厂家"><el-input v-model="drugForm.manufacturer" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="有效期"><el-date-picker v-model="drugForm.expiryDate" type="date" style="width:100%" value-format="YYYY-MM-DD" /></el-form-item></el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="saveDrug">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showStock" :title="stockForm.type === 'in' ? '药品入库' : '药品出库'" width="400px" destroy-on-close>
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="药品">{{ stockForm.drugName }}</el-form-item>
        <el-form-item label="数量"><el-input-number v-model="stockForm.quantity" :min="1" style="width:100%" /></el-form-item>
        <el-form-item v-if="stockForm.type === 'in'" label="批号"><el-input v-model="stockForm.batchNo" placeholder="请输入批号" /></el-form-item>
        <el-form-item v-if="stockForm.type === 'out'" label="备注"><el-input v-model="stockForm.remark" placeholder="出库原因" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStock = false">取消</el-button>
        <el-button type="primary" @click="doStock">确认{{ stockForm.type === 'in' ? '入库' : '出库' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showTrans" title="出入库流水" width="600px">
      <el-table :data="transactions" border stripe>
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }"><el-tag :type="row.type===1?'success':'danger'">{{ row.type===1?'入库':'出库' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="batchNo" label="批号" width="120" />
        <el-table-column prop="createTime" label="时间" width="180" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getDrugPage, addDrug, updateDrug, stockIn as stockInApi, stockOut as stockOutApi, getDrugTransactions } from '@/api/pharmacy'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const page = ref({ records: [], total: 0 })
const current = ref(1)
const size = ref(10)
const keyword = ref('')
const loading = ref(false)
const showAdd = ref(false)
const showStock = ref(false)
const showTrans = ref(false)
const editingId = ref(null)
const transactions = ref([])

const drugForm = reactive({
  drugCode: '', drugName: '', genericName: '', category: '',
  specification: '', unit: '盒', unitPrice: 0, safeThreshold: 10,
  manufacturer: '', expiryDate: '',
})

const stockForm = reactive({ drugId: null, drugName: '', quantity: 1, batchNo: '', remark: '', type: 'in' })

function isNearExpiry(date) {
  if (!date) return false
  const d = new Date(date)
  const now = new Date()
  const diff = (d - now) / (1000 * 60 * 60 * 24)
  return diff <= 90 && diff > 0
}

async function load() {
  loading.value = true
  try { page.value = await getDrugPage({ current: current.value, size: size.value, keyword: keyword.value || undefined }) } catch {} finally { loading.value = false }
}

function openAdd() {
  editingId.value = null
  Object.assign(drugForm, { drugCode: '', drugName: '', genericName: '', category: '', specification: '', unit: '盒', unitPrice: 0, safeThreshold: 10, manufacturer: '', expiryDate: '' })
  showAdd.value = true
}

function editDrug(row) {
  editingId.value = row.id
  Object.assign(drugForm, { ...row, expiryDate: row.expiryDate || '' })
  showAdd.value = true
}

async function saveDrug() {
  try {
    const body = { ...drugForm }
    if (editingId.value) {
      body.id = editingId.value
      await updateDrug(body)
    } else {
      body.status = 1
      body.stockCount = 0
      await addDrug(body)
    }
    ElMessage.success('保存成功')
    showAdd.value = false
    load()
  } catch {}
}

function openStockIn(row) {
  Object.assign(stockForm, { drugId: row.id, drugName: row.drugName, quantity: 1, batchNo: '', remark: '', type: 'in' })
  showStock.value = true
}

function openStockOut(row) {
  Object.assign(stockForm, { drugId: row.id, drugName: row.drugName, quantity: 1, batchNo: '', remark: '', type: 'out' })
  showStock.value = true
}

async function doStock() {
  try {
    if (stockForm.type === 'in') {
      await stockInApi({ drugId: stockForm.drugId, quantity: stockForm.quantity, batchNo: stockForm.batchNo || 'DEFAULT', operatorId: userStore.userId })
    } else {
      await stockOutApi({ drugId: stockForm.drugId, quantity: stockForm.quantity, operatorId: userStore.userId, remark: stockForm.remark })
    }
    ElMessage.success(stockForm.type === 'in' ? '入库成功' : '出库成功')
    showStock.value = false
    load()
  } catch {}
}

async function viewTransactions(row) {
  try { transactions.value = await getDrugTransactions(row.id); showTrans.value = true } catch {}
}

onMounted(load)
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
