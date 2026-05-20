<template>
  <el-container class="layout">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span class="logo-icon">🏥</span>
        <span class="logo-text">智慧医疗</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#ffffffb3"
        active-text-color="#fff"
        class="side-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
        </el-menu-item>

        <template v-if="userStore.roleId === 2">
          <el-sub-menu index="doctor">
            <template #title><el-icon><UserFilled /></el-icon><span>医生工作台</span></template>
            <el-menu-item index="/doctor/today">今日待诊</el-menu-item>
            <el-menu-item index="/doctor/record">病历管理</el-menu-item>
            <el-menu-item index="/doctor/prescription">开具处方</el-menu-item>
          </el-sub-menu>
        </template>

        <template v-if="userStore.roleId === 5">
          <el-sub-menu index="patient">
            <template #title><el-icon><User /></el-icon><span>患者服务</span></template>
            <el-menu-item index="/patient/appointment">预约挂号</el-menu-item>
            <el-menu-item index="/patient/records">我的病历</el-menu-item>
            <el-menu-item index="/patient/exam">体检预约</el-menu-item>
          </el-sub-menu>
        </template>

        <template v-if="userStore.roleId === 3">
          <el-sub-menu index="pharmacy">
            <template #title><el-icon><Goods /></el-icon><span>药房工作台</span></template>
            <el-menu-item index="/pharmacy/prescriptions">处方审核</el-menu-item>
            <el-menu-item index="/pharmacy/drugs">药品管理</el-menu-item>
            <el-menu-item index="/pharmacy/stock">库存预警</el-menu-item>
          </el-sub-menu>
        </template>

        <template v-if="userStore.roleId === 4">
          <el-sub-menu index="medical">
            <template #title><el-icon><OfficeBuilding /></el-icon><span>医务管理</span></template>
            <el-menu-item index="/medical/schedule">排班管理</el-menu-item>
            <el-menu-item index="/medical/departments">科室管理</el-menu-item>
          </el-sub-menu>
        </template>

        <template v-if="userStore.roleId === 1">
          <el-sub-menu index="admin">
            <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
            <el-menu-item index="/admin/users">用户管理</el-menu-item>
            <el-menu-item index="/admin/logs">日志监控</el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/medical/schedule">排班管理</el-menu-item>
          <el-menu-item index="/medical/departments">科室管理</el-menu-item>
        </template>

        <el-menu-item index="/ai/triage">
          <el-icon><Cpu /></el-icon>
          <span>AI 智能导诊</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <span class="welcome">欢迎，{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
          <el-tag size="small" type="primary">{{ userStore.roleName }}</el-tag>
        </div>
        <div class="header-right">
          <el-button text @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/doctor')) return path
  if (path.startsWith('/patient')) return path
  if (path.startsWith('/pharmacy')) return path
  if (path.startsWith('/admin')) return path
  if (path.startsWith('/medical')) return path
  return path
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.sidebar {
  background: #001529;
  overflow-y: auto;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.logo-icon { font-size: 24px; }
.logo-text { color: #fff; font-size: 18px; font-weight: bold; }
.side-menu { border-right: none; }
.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  padding: 0 24px;
  height: 60px;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.welcome { font-size: 14px; color: #333; }
.header-right { display: flex; align-items: center; gap: 12px; }
.main-content { background: #f5f7fa; padding: 20px; overflow-y: auto; }

:deep(.el-sub-menu .el-menu-item) { padding-left: 56px !important; }
</style>
