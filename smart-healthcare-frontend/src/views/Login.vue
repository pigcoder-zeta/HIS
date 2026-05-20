<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>🏥 智慧医疗管理系统</h1>
        <p>Smart Healthcare Management System</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>没有账号？</span>
        <el-button link type="primary" @click="showRegister = true">立即注册</el-button>
      </div>
    </div>

    <el-dialog v-model="showRegister" title="患者注册" width="420px" destroy-on-close>
      <el-form ref="regFormRef" :model="regForm" :rules="regRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="regForm.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="regForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="regForm.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="regForm.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" :loading="regLoading" @click="handleRegister">注册</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { register as registerApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const regFormRef = ref()
const loading = ref(false)
const regLoading = ref(false)
const showRegister = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const regForm = reactive({ username: '', password: '', realName: '', phone: '' })
const regRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/')
  } catch {
    // error handled in interceptor
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  const valid = await regFormRef.value.validate().catch(() => false)
  if (!valid) return
  regLoading.value = true
  try {
    await registerApi(regForm)
    ElMessage.success('注册成功，请登录')
    showRegister.value = false
  } catch {
    // handled
  } finally {
    regLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.login-header {
  text-align: center;
  margin-bottom: 32px;
}
.login-header h1 { font-size: 24px; color: #333; margin-bottom: 8px; }
.login-header p { font-size: 13px; color: #999; }
.login-footer {
  text-align: center;
  font-size: 13px;
  color: #999;
}
</style>
