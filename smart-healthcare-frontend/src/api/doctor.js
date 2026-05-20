import request from '@/utils/request'

export const getTodayAppointments = (doctorId) => request.get('/doctor/appointment/today', { params: { doctorId } })
export const callNext = (id) => request.put(`/doctor/appointment/${id}/call`)
export const completeAppointment = (id) => request.put(`/doctor/appointment/${id}/complete`)
export const saveRecord = (doctorId, data) => request.post('/doctor/record', data, { params: { doctorId } })
export const signRecord = (id, doctorId) => request.put(`/doctor/record/${id}/sign`, null, { params: { doctorId } })
export const createPrescription = (doctorId, data) => request.post('/doctor/prescription', data, { params: { doctorId } })
export const getPatientRecords = (patientId) => request.get(`/doctor/record/patient/${patientId}`)
