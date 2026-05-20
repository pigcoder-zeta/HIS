<template>
  <div>
    <h2 class="page-title">🏢 科室管理</h2>
    <el-card>
      <div style="margin-bottom:16px">
        <el-button type="primary" @click="showAdd = true">新增科室</el-button>
      </div>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="name" label="科室名称" width="180" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" @click="edit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" :title="editing?.id?'编辑科室':'新增科室'" width="400px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getDepartments, addDepartment, updateDepartment } from '@/api/medical'
import { ElMessage } from 'element-plus'

const list = ref([])
const loading = ref(false)
const showAdd = ref(false)
const editing = ref({})
const form = reactive({ name: '', description: '' })

async function load() {
  loading.value = true
  try { list.value = await getDepartments() } catch {} finally { loading.value = false }
}

function edit(row) { Object.assign(form, row); editing.value = row; showAdd.value = true }

async function save() {
  try {
    if (editing.value?.id) { await updateDepartment({ ...form, id: editing.value.id }) } else { await addDepartment(form) }
    ElMessage.success('保存成功')
    showAdd.value = false
    Object.assign(form, { name: '', description: '' })
    editing.value = {}
    load()
  } catch {}
}

onMounted(load)
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
