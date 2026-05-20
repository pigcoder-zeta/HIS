import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getStoredValue('token', ''))
  const userInfo = ref(getStoredJSON('user', null))
  const permissions = ref(getStoredJSON('permissions', []))

function getStoredValue(key, fallback) {
  try {
    const val = localStorage.getItem(key)
    return val && val !== 'undefined' ? val : fallback
  } catch { return fallback }
}
function getStoredJSON(key, fallback) {
  try {
    const val = localStorage.getItem(key)
    return val && val !== 'undefined' ? JSON.parse(val) : fallback
  } catch { return fallback }
}

  const isLoggedIn = computed(() => !!token.value)
  const roleCode = computed(() => userInfo.value?.roleCode)
  const userId = computed(() => userInfo.value?.id)

  // roleCode -> numeric roleId 映射（兼容旧组件）
  const roleCodeToId = {
    'ROLE_SYSTEM_ADMIN': 1,
    'ROLE_DOCTOR': 2,
    'ROLE_PHARMACIST': 3,
    'ROLE_MEDICAL_ADMIN': 4,
    'ROLE_PATIENT': 5,
  }
  const roleId = computed(() => roleCodeToId[roleCode.value] || 0)
  const roleName = computed(() => userInfo.value?.roleName || '未知')

  async function login(credentials) {
    const data = await loginApi(credentials)
    token.value = data.token
    // 后端返回扁平结构，适配为前端期望格式
    const user = {
      id: data.userId,
      username: data.username,
      realName: data.realName,
      roleCode: data.roleCode,
      roleName: data.roleName,
      avatar: data.avatar,
    }
    userInfo.value = user
    // 权限列表（如后端返回）
    const perms = data.permissions || generateDefaultPermissions(data.roleCode)
    permissions.value = perms
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(user))
    localStorage.setItem('permissions', JSON.stringify(perms))
    return data
  }

  function logout() {
    if (userId.value) {
      logoutApi(userId.value).catch(() => {})
    }
    token.value = ''
    userInfo.value = null
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('permissions')
  }

  /**
   * 根据角色生成默认权限（后端未返回权限列表时的降级方案）
   */
  function generateDefaultPermissions(roleCode) {
    const permMap = {
      'ROLE_SYSTEM_ADMIN': ['*:*:*'],
      'ROLE_DOCTOR': ['doctor:*:*', 'patient:record:*', 'ai:triage:*'],
      'ROLE_PHARMACIST': ['pharmacy:*:*', 'drug:*:*'],
      'ROLE_MEDICAL_ADMIN': ['medical:*:*', 'schedule:*:*', 'department:*:*'],
      'ROLE_PATIENT': ['patient:appointment:*', 'patient:record:*', 'patient:exam:*', 'ai:triage:*'],
    }
    return permMap[roleCode] || []
  }

  return { token, userInfo, permissions, isLoggedIn, roleCode, roleId, userId, roleName, login, logout }
})
