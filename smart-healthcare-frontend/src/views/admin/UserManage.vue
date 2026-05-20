<template>
  <div>
    <h2 class="page-title">👥 用户管理</h2>
    <el-card>
      <div style="margin-bottom:16px">
        <el-button type="primary" @click="showCreate = true">创建用户</el-button>
      </div>
      <el-table :data="page.records" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag>{{ roleMap[row.roleId] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" min-width="250">
          <template #default="{ row }">
            <el-button size="small" @click="editUser(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="resetPwd(row)">重置密码</el-button>
            <el-button size="small" :type="row.status===1 ? 'danger' : 'success'" @click="toggle(row)">{{ row.status===1?'禁用':'启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top:16px;justify-content:flex-end"
        v-model:current-page="current" :page-size="size" :total="page.total"
        layout="total, prev, pager, next" @current-change="load"
      />
    </el-card>

    <el-dialog v-model="showCreate" :title="editing?.id ? '编辑用户' : '创建用户'" width="450px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item v-if="!editing?.id" label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="真实姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleId" style="width:100%">
            <el-option v-for="(v,k) in roleMap" :key="k" :label="v" :value="Number(k)" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserPage, createUser, updateUser, resetPassword, toggleUserStatus, getRoles } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const roleMap = { 1: '系统管理员', 2: '医生', 3: '药师', 4: '医务科', 5: '患者' }
const page = ref({ records: [], total: 0 })
const current = ref(1)
const size = ref(10)
const loading = ref(false)
const showCreate = ref(false)
const editing = ref({})

const form = reactive({ username: '', password: '', realName: '', phone: '', roleId: null })

async function load() {
  loading.value = true
  try { page.value = await getUserPage({ current: current.value, size: size.value }) } catch {} finally { loading.value = false }
}

function editUser(row) { Object.assign(form, row); editing.value = row; showCreate.value = true }
async function saveUser() {
  try {
    if (editing.value?.id) { await updateUser({ ...form, id: editing.value.id }) } else { await createUser(form) }
    ElMessage.success('保存成功')
    showCreate.value = false
    Object.assign(form, { username: '', password: '', realName: '', phone: '', roleId: null })
    editing.value = {}
    load()
  } catch {}
}

async function resetPwd(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', { inputType: 'password' })
    await resetPassword(row.id, value)
    ElMessage.success('密码重置成功')
  } catch {}
}

async function toggle(row) {
  const newStatus = row.status === 1 ? 0 : 1
  await toggleUserStatus(row.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
  load()
}

onMounted(load)
</script>

<style scoped>
.page-title { margin-bottom: 20px; font-size: 22px; color: #333; }
</style>
