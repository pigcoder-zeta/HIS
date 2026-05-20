import request from '@/utils/request'

export const aiTriage = (data) => request.post('/ai/triage', data)

/** SSE 流式导诊 - 返回原始 Response 供调用方读取流 */
export const aiTriageStream = (data) => {
  const token = localStorage.getItem('token')
  return fetch('/api/v1/ai/triage/stream', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`,
    },
    body: JSON.stringify(data),
  })
}

export const generateMedicalRecord = (notes) => request.post('/ai/medical-record/generate', { notes })

