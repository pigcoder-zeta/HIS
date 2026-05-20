import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
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
      },
      // 医生
      {
        path: 'doctor/today',
        name: 'DoctorToday',
        component: () => import('@/views/doctor/TodayPatients.vue'),
      },
      {
        path: 'doctor/record',
        name: 'DoctorRecord',
        component: () => import('@/views/doctor/MedicalRecord.vue'),
      },
      {
        path: 'doctor/prescription',
        name: 'DoctorPrescription',
        component: () => import('@/views/doctor/Prescription.vue'),
      },
      // 患者
      {
        path: 'patient/appointment',
        name: 'PatientAppointment',
        component: () => import('@/views/patient/Appointment.vue'),
      },
      {
        path: 'patient/records',
        name: 'PatientRecords',
        component: () => import('@/views/patient/MyRecords.vue'),
      },
      {
        path: 'patient/exam',
        name: 'PatientExam',
        component: () => import('@/views/patient/Exam.vue'),
      },
      // 药房
      {
        path: 'pharmacy/prescriptions',
        name: 'PharmacyPrescriptions',
        component: () => import('@/views/pharmacy/PrescriptionAudit.vue'),
      },
      {
        path: 'pharmacy/drugs',
        name: 'PharmacyDrugs',
        component: () => import('@/views/pharmacy/DrugManage.vue'),
      },
      {
        path: 'pharmacy/stock',
        name: 'PharmacyStock',
        component: () => import('@/views/pharmacy/StockAlert.vue'),
      },
      // 管理员
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
      },
      {
        path: 'admin/logs',
        name: 'AdminLogs',
        component: () => import('@/views/admin/LogMonitor.vue'),
      },
      // 医务科
      {
        path: 'medical/schedule',
        name: 'MedicalSchedule',
        component: () => import('@/views/medical/ScheduleManage.vue'),
      },
      {
        path: 'medical/departments',
        name: 'MedicalDepartments',
        component: () => import('@/views/medical/DepartmentManage.vue'),
      },
      // AI 导诊
      {
        path: 'ai/triage',
        name: 'AITriage',
        component: () => import('@/views/AITriage.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router
