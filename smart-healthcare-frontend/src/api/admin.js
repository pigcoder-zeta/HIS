import request from '@/utils/request'

export const getUserPage = (params) => request.get('/admin/user/page', { params })
export const createUser = (data) => request.post('/admin/user', data)
export const updateUser = (data) => request.put('/admin/user', data)
export const resetPassword = (id, newPassword) => request.put(`/admin/user/${id}/reset-password`, null, { params: { newPassword } })
export const toggleUserStatus = (id, status) => request.put(`/admin/user/${id}/toggle-status`, null, { params: { status } })
export const getRoles = () => request.get('/admin/role')
export const getLogPage = (params) => request.get('/admin/log/page', { params })
