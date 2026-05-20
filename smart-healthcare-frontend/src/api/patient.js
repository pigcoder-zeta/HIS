import request from '@/utils/request'

export const bookAppointment = (data) => request.post('/patient/appointment/book', data)
export const cancelAppointment = (id, patientId) => request.put(`/patient/appointment/${id}/cancel`, null, { params: { patientId } })
export const getAppointments = (patientId) => request.get('/patient/appointment/list', { params: { patientId } })
export const getRecords = (patientId) => request.get('/patient/record/list', { params: { patientId } })
export const getRecordDetail = (id) => request.get(`/patient/record/${id}`)
export const getExamPackages = () => request.get('/patient/exam/packages')
export const bookExam = (patientId, packageId, date) => request.post('/patient/exam/book', null, { params: { patientId, packageId, date } })
export const getExams = (patientId) => request.get('/patient/exam/list', { params: { patientId } })
export const cancelExam = (id, patientId) => request.put(`/patient/exam/${id}/cancel`, null, { params: { patientId } })
