import { useUserStore } from '@/store/user'

/**
 * 权限检查 composable
 * 提供 hasPermission / hasRole / hasAnyRole 方法
 */
export function usePermission() {
  const userStore = useUserStore()

  /**
   * 检查是否拥有指定权限
   * @param {string|string[]} perms - 权限标识，如 'sys:user:add' 或 ['sys:user:add', 'sys:user:edit']
   */
  function hasPermission(perms) {
    if (!perms || perms.length === 0) return true
    const userPerms = userStore.permissions || []
    if (Array.isArray(perms)) {
      return perms.some(p => userPerms.includes(p))
    }
    return userPerms.includes(perms)
  }

  /**
   * 检查是否拥有指定角色
   * @param {string|string[]} roles - 角色编码，如 'ROLE_SYSTEM_ADMIN'
   */
  function hasRole(roles) {
    if (!roles || roles.length === 0) return true
    const userRole = userStore.roleCode
    if (Array.isArray(roles)) {
      return roles.includes(userRole)
    }
    return roles === userRole
  }

  /**
   * 检查是否拥有任意一个指定角色
   */
  function hasAnyRole(roles) {
    if (!roles || roles.length === 0) return true
    return roles.includes(userStore.roleCode)
  }

  return { hasPermission, hasRole, hasAnyRole }
}
