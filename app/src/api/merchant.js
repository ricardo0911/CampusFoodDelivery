import { get, post, put, del } from '@/utils/request'

/**
 * 商家端 API
 */

// 店铺管理
export const getShopInfo = () => get('/merchant/shop/info')
export const updateShopInfo = (data) => put('/merchant/shop/update', data)

// 菜品管理
export const getDishList = (params) => get('/merchant/dish/list', params)
export const addDish = (data) => post('/merchant/dish/add', data)
export const updateDish = (data) => put('/merchant/dish/update', data)
export const deleteDish = (id) => del(`/merchant/dish/delete/${id}`)

// 订单管理
export const getMerchantOrderList = (params) => get('/merchant/order/list', params)
export const getMerchantOrderDetail = (id) => get(`/merchant/order/${id}`)
export const acceptOrder = (id) => post(`/merchant/order/${id}/accept`)
export const rejectOrder = (id, reason) => post(`/merchant/order/${id}/reject`, { reason })

// 数据统计
export const getTodayStats = () => get('/merchant/stats/today', {}, { showLoading: false })

export default {
  getShopInfo, updateShopInfo,
  getDishList, addDish, updateDish, deleteDish,
  getMerchantOrderList, getMerchantOrderDetail, acceptOrder, rejectOrder,
  getTodayStats
}
