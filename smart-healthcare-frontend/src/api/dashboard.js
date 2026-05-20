import request from '@/utils/request'

// 管理员
export const getAdminOverview = () => request.get('/dashboard/admin/overview')
export const getUserTrend = () => request.get('/dashboard/admin/user-trend')
export const getDeptVisitToday = () => request.get('/dashboard/admin/dept-visit-today')
export const getApiSuccessRate = () => request.get('/dashboard/admin/api-success-rate')

// 医生
export const getDoctorOverview = (doctorId) => request.get('/dashboard/doctor/overview', { params: { doctorId } })
export const getDoctorScheduleCalendar = (doctorId, month) => request.get('/dashboard/doctor/schedule-calendar', { params: { doctorId, month } })

// 药房
export const getPharmacyOverview = () => request.get('/dashboard/pharmacy/overview')
export const getPharmacyAlerts = () => request.get('/dashboard/pharmacy/alerts')

// 医务科
export const getMedicalOverview = () => request.get('/dashboard/medical/overview')
