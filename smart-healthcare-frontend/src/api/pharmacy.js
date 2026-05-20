import request from '@/utils/request'

export const getPendingPrescriptions = () => request.get('/pharmacy/prescription/pending')
export const auditPrescription = (id, pharmacistId, approved, opinion) => request.put(`/pharmacy/prescription/${id}/audit`, null, { params: { pharmacistId, approved, opinion } })
export const dispensePrescription = (id, pharmacistId) => request.put(`/pharmacy/prescription/${id}/dispense`, null, { params: { pharmacistId } })
export const getDrugPage = (params) => request.get('/pharmacy/drug/page', { params })
export const addDrug = (data) => request.post('/pharmacy/drug', data)
export const updateDrug = (data) => request.put('/pharmacy/drug', data)
export const stockIn = (data) => request.post('/pharmacy/drug/stock-in', null, { params: data })
export const stockOut = (data) => request.post('/pharmacy/drug/stock-out', null, { params: data })
export const getLowStockDrugs = () => request.get('/pharmacy/drug/low-stock')
export const getNearExpiryDrugs = () => request.get('/pharmacy/drug/near-expiry')
export const getDrugTransactions = (drugId) => request.get(`/pharmacy/drug/${drugId}/transactions`)
