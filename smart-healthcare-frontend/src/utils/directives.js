import { useUserStore } from '@/store/user'

/**
 * Vue 自定义指令：v-hasPermi
 * 用于按钮级权限控制
 *
 * 用法：
 *   <el-button v-hasPermi="'sys:user:add'">新增</el-button>
 *   <el-button v-hasPermi="['sys:user:add', 'sys:user:edit']">操作</el-button>
 */
export default {
  install(app) {
    app.directive('hasPermi', {
      mounted(el, binding) {
        const userStore = useUserStore()
        const perms = binding.value
        if (!perms || perms.length === 0) return

        const userPerms = userStore.permissions || []
        let has = false

        if (Array.isArray(perms)) {
          has = perms.some(p => userPerms.includes(p))
        } else {
          has = userPerms.includes(perms)
        }

        if (!has) {
          el.parentNode?.removeChild(el)
        }
      },
    })

    /**
     * v-hasRole 指令：基于角色的权限控制
     * 用法：<div v-hasRole="'ROLE_DOCTOR'">...</div>
     */
    app.directive('hasRole', {
      mounted(el, binding) {
        const userStore = useUserStore()
        const roles = binding.value
        if (!roles || roles.length === 0) return

        let has = false
        if (Array.isArray(roles)) {
          has = roles.includes(userStore.roleCode)
        } else {
          has = roles === userStore.roleCode
        }

        if (!has) {
          el.parentNode?.removeChild(el)
        }
      },
    })
  },
}
