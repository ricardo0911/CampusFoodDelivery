import { get, post } from '@/utils/request'

export const getOrderList = (params) => get('/customer/order/list', params)
export const getOrderDetail = (id) => get(`/customer/order/${id}`)
export const createOrder = (data) => post('/customer/order/create', data)
export const payOrder = (id) => post(`/customer/order/${id}/pay`)
export const cancelOrder = (id) => post(`/customer/order/${id}/cancel`)
export const confirmOrder = (id) => post(`/customer/order/${id}/confirm`)

export default { getOrderList, getOrderDetail, createOrder, payOrder, cancelOrder, confirmOrder }
