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

        <!-- 动态菜单：根据路由 meta 自动生成 -->
        <template v-for="group in menuGroups" :key="group.key">
          <el-sub-menu :index="group.key">
            <template #title>
              <el-icon><component :is="group.icon" /></el-icon>
              <span>{{ group.label }}</span>
            </template>
            <el-menu-item
              v-for="item in group.children"
              :key="item.path"
              :index="item.path"
            >{{ item.meta.title }}</el-menu-item>
          </el-sub-menu>
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

/**
 * 根据角色和路由表动态生成菜单分组
 */
const menuGroupConfig = {
  'ROLE_DOCTOR': {
    key: 'doctor', label: '医生工作台', icon: 'UserFilled',
    prefix: 'doctor',
  },
  'ROLE_PATIENT': {
    key: 'patient', label: '患者服务', icon: 'User',
    prefix: 'patient',
  },
  'ROLE_PHARMACIST': {
    key: 'pharmacy', label: '药房工作台', icon: 'Goods',
    prefix: 'pharmacy',
  },
  'ROLE_MEDICAL_ADMIN': {
    key: 'medical', label: '医务管理', icon: 'OfficeBuilding',
    prefix: 'medical',
  },
  'ROLE_SYSTEM_ADMIN': {
    key: 'admin', label: '系统管理', icon: 'Setting',
    prefix: 'admin',
    extraGroups: [
      { key: 'medical', label: '医务管理', icon: 'OfficeBuilding', prefix: 'medical' },
      { key: 'doctor', label: '医生工作台', icon: 'UserFilled', prefix: 'doctor' },
      { key: 'patient', label: '患者服务', icon: 'User', prefix: 'patient' },
      { key: 'pharmacy', label: '药房工作台', icon: 'Goods', prefix: 'pharmacy' },
    ],
  },
}

const menuGroups = computed(() => {
  const roleCode = userStore.roleCode
  const config = menuGroupConfig[roleCode]
  if (!config) return []

  const groups = [config]
  if (config.extraGroups) {
    groups.push(...config.extraGroups)
  }

  return groups.map(g => {
    const children = router.getRoutes().filter(r => {
      return r.path.startsWith('/' + g.prefix) && r.path !== '/' + g.prefix && r.meta.title
    })
    return { key: g.key, label: g.label, icon: g.icon, children }
  })
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
