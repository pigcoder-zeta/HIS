import request from '@/utils/request'

export const aiTriage = (data) => request.post('/ai/triage', data)
export const generateMedicalRecord = (notes) => request.post('/ai/medical-record/generate', { notes })
