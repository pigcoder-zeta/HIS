<template>
  <div>
    <h2 class="page-title">💊 药品管理</h2>
    <el-card>
      <div style="margin-bottom:16px;display:flex;gap:12px">
        <el-input v-model="keyword" placeholder="搜索药品" style="width:240px" clearable @clear="load" @keyup.enter="load" />
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button type="success" @click="showAdd = true">新增药品</el-button>
      </div>
      <el-table :data="page.records" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="药品名称" width="180" />
        <el-table-column prop="spec" label="规格" width="120" />
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="price" label="单价" width="80" />
        <el-table-column prop="expireDate" label="有效期" width="120" />
        <el-table-column label="操作" min-width="200">
          <template #default="{ row }">
            <el-button size="small" @click="edit(row)">编辑</el-button>
            <el-button size="small" type="success" @click="stockIn(row)">入库</el-button>
            <el-button size="small" type="warning" @click="stockOut(row)">出库</el-button>
            <el-button size="small" type="info" @click="viewTrans(row)">流水</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top:16px;justify-content:flex-end"
        v-model:current-page="current" :page-size="size" :total="page.total"
        layout="total, prev, pager, next" @current-change="load"
      />
    </el-card>

    <el-dialog v-model="showAdd" :title="editingDrug?.id ? '编辑药品' : '新增药品'" width="500px" destroy-on-close>
      <el-form :model="drugForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="drugForm.name" /></el-form-item>
        <el-form-item label="规格"><el-input v-model="drugForm.spec" /></el-form-item>
        <el-form-item label="单价"><el-input-number v-model="drugForm.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="有效期"><el-date-picker v-model="drugForm.expireDate" type="date" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="saveDrug">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showStock" title="出入库" width="400px" destroy-on-close>
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="数量"><el-input-number v-model="stockForm.quantity" :min="1" /></el-form-item>
        <el-form-item v-if="stockForm.type === 'in'" label="批号"><el-input v-model="stockForm.batchNo" /></el-form-item>
        <el-form-item v-if="stockForm.type === 'out'" label="备注"><el-input v-model="stockForm.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStock = false">取消</el-button>
        <el-button type="primary" @click="doStock">确认</el-button>
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
const editingDrug = ref({})
const transactions = ref([])

const drugForm = reactive({ name: '', spec: '', price: 0, expireDate: '' })
const stockForm = reactive({ drugId: null, quantity: 0, batchNo: '', remark: '', type: 'in' })

async function load() {
  loading.value = true
  try { page.value = await getDrugPage({ current: current.value, size: size.value, keyword: keyword.value }) } catch {} finally { loading.value = false }
}

function edit(row) { Object.assign(drugForm, { ...row }); editingDrug.value = row; showAdd.value = true }
async function saveDrug() {
  try {
    if (editingDrug.value?.id) { await updateDrug({ ...drugForm, id: editingDrug.value.id }) } else { await addDrug(drugForm) }
    ElMessage.success('保存成功')
    showAdd.value = false
    Object.assign(drugForm, { name: '', spec: '', price: 0, expireDate: '' })
    editingDrug.value = {}
    load()
  } catch {}
}

function stockIn(row) { stockForm.drugId = row.id; stockForm.type = 'in'; stockForm.quantity = 0; stockForm.batchNo = ''; showStock.value = true }
function stockOut(row) { stockForm.drugId = row.id; stockForm.type = 'out'; stockForm.quantity = 0; stockForm.remark = ''; showStock.value = true }
async function doStock() {
  try {
    if (stockForm.type === 'in') {
      await stockInApi({ drugId: stockForm.drugId, quantity: stockForm.quantity, batchNo: stockForm.batchNo, operatorId: userStore.userId })
    } else {
      await stockOutApi({ drugId: stockForm.drugId, quantity: stockForm.quantity, operatorId: userStore.userId, remark: stockForm.remark })
    }
    ElMessage.success(stockForm.type === 'in' ? '入库成功' : '出库成功')
    showStock.value = false
    load()
  } catch {}
}

async function viewTrans(row) {
  try { transactions.value = await getDrugTransactions(row.id); showTrans.value = true } catch {}
}

onMounted(load)
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
