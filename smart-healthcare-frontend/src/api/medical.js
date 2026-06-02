import request from '@/utils/request'

export const getSchedules = (params) => request.get('/medical/schedule', { params })
export const createSchedule = (data) => request.post('/medical/schedule', data)
export const batchSchedule = (data) => request.post('/medical/schedule/batch', data)
export const cancelSchedule = (id) => request.put(`/medical/schedule/${id}/cancel`)
export const getDepartments = () => request.get('/medical/department')
export const addDepartment = (data) => request.post('/medical/department', data)
export const updateDepartment = (data) => request.put('/medical/department', data)
export const getDoctors = (deptId) => request.get('/medical/doctor', { params: { deptId } })
