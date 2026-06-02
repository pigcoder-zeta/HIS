import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由 Meta 中定义 allowedRoles，用于角色访问控制
 * allowedRoles: 角色编码数组，如 ['ROLE_SYSTEM_ADMIN', 'ROLE_DOCTOR']
 * 不定义 allowedRoles 表示所有已登录用户均可访问
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: () => import('@/components/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台', icon: 'HomeFilled' },
      },
      // ========== 医生路由 ==========
      {
        path: 'doctor/consultation',
        name: 'DoctorConsultation',
        component: () => import('@/views/doctor/Consultation.vue'),
        meta: { title: '接诊工作台', icon: 'Monitor', parent: 'doctor', allowedRoles: ['ROLE_DOCTOR'] },
      },
      {
        path: 'doctor/today',
        name: 'DoctorToday',
        component: () => import('@/views/doctor/TodayPatients.vue'),
        meta: { title: '今日待诊', icon: 'UserFilled', parent: 'doctor', allowedRoles: ['ROLE_DOCTOR'] },
      },
      {
        path: 'doctor/record',
        name: 'DoctorRecord',
        component: () => import('@/views/doctor/MedicalRecord.vue'),
        meta: { title: '病历管理', icon: 'Document', parent: 'doctor', allowedRoles: ['ROLE_DOCTOR'] },
      },
      {
        path: 'doctor/prescription',
        name: 'DoctorPrescription',
        component: () => import('@/views/doctor/Prescription.vue'),
        meta: { title: '开具处方', icon: 'Edit', parent: 'doctor', allowedRoles: ['ROLE_DOCTOR'] },
      },
      // ========== 患者路由 ==========
      {
        path: 'patient/appointment',
        name: 'PatientAppointment',
        component: () => import('@/views/patient/Appointment.vue'),
        meta: { title: '预约挂号', icon: 'Calendar', parent: 'patient', allowedRoles: ['ROLE_PATIENT'] },
      },
      {
        path: 'patient/records',
        name: 'PatientRecords',
        component: () => import('@/views/patient/MyRecords.vue'),
        meta: { title: '我的病历', icon: 'Notebook', parent: 'patient', allowedRoles: ['ROLE_PATIENT'] },
      },
      {
        path: 'patient/exam',
        name: 'PatientExam',
        component: () => import('@/views/patient/Exam.vue'),
        meta: { title: '体检预约', icon: 'FirstAidKit', parent: 'patient', allowedRoles: ['ROLE_PATIENT'] },
      },
      // ========== 药房路由 ==========
      {
        path: 'pharmacy/prescriptions',
        name: 'PharmacyPrescriptions',
        component: () => import('@/views/pharmacy/PrescriptionAudit.vue'),
        meta: { title: '处方审核', icon: 'Checked', parent: 'pharmacy', allowedRoles: ['ROLE_PHARMACIST'] },
      },
      {
        path: 'pharmacy/drugs',
        name: 'PharmacyDrugs',
        component: () => import('@/views/pharmacy/DrugManage.vue'),
        meta: { title: '药品管理', icon: 'Goods', parent: 'pharmacy', allowedRoles: ['ROLE_PHARMACIST'] },
      },
      {
        path: 'pharmacy/stock',
        name: 'PharmacyStock',
        component: () => import('@/views/pharmacy/StockAlert.vue'),
        meta: { title: '库存预警', icon: 'WarningFilled', parent: 'pharmacy', allowedRoles: ['ROLE_PHARMACIST'] },
      },
      // ========== 管理员路由 ==========
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', icon: 'User', parent: 'admin', allowedRoles: ['ROLE_SYSTEM_ADMIN'] },
      },
      {
        path: 'admin/logs',
        name: 'AdminLogs',
        component: () => import('@/views/admin/LogMonitor.vue'),
        meta: { title: '日志监控', icon: 'Monitor', parent: 'admin', allowedRoles: ['ROLE_SYSTEM_ADMIN'] },
      },
      // ========== 医务科路由 ==========
      {
        path: 'medical/schedule',
        name: 'MedicalSchedule',
        component: () => import('@/views/medical/ScheduleManage.vue'),
        meta: { title: '排班管理', icon: 'Timer', parent: 'medical', allowedRoles: ['ROLE_MEDICAL_ADMIN', 'ROLE_SYSTEM_ADMIN'] },
      },
      {
        path: 'medical/departments',
        name: 'MedicalDepartments',
        component: () => import('@/views/medical/DepartmentManage.vue'),
        meta: { title: '科室管理', icon: 'OfficeBuilding', parent: 'medical', allowedRoles: ['ROLE_MEDICAL_ADMIN', 'ROLE_SYSTEM_ADMIN'] },
      },
      // ========== AI 导诊（所有人可访问） ==========
      {
        path: 'ai/triage',
        name: 'AITriage',
        component: () => import('@/views/AITriage.vue'),
        meta: { title: 'AI 智能导诊', icon: 'Cpu' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// roleCode -> numeric roleId
const roleCodeToId = {
  'ROLE_SYSTEM_ADMIN': 1,
  'ROLE_DOCTOR': 2,
  'ROLE_PHARMACIST': 3,
  'ROLE_MEDICAL_ADMIN': 4,
  'ROLE_PATIENT': 5,
}

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  let userInfo = null
  try {
    const raw = localStorage.getItem('user')
    if (raw && raw !== 'undefined') userInfo = JSON.parse(raw)
  } catch {}

  // 公开页面直接放行
  if (to.meta.public) {
    if (to.path === '/login' && token) return next('/')
    return next()
  }

  // 未登录 → 登录页
  if (!token) return next('/login')

  // 角色权限检查（系统管理员可访问所有页面）
  if (to.meta.allowedRoles) {
    const userRole = userInfo?.roleCode
    if (userRole !== 'ROLE_SYSTEM_ADMIN' && !to.meta.allowedRoles.includes(userRole)) {
      // 无权限 → 返回工作台
      return next('/dashboard')
    }
  }

  next()
})

export default router
