import { get, post } from '@/utils/request'

/**
 * 店铺相关 API
 */

/**
 * 获取店铺列表
 * @param {Object} params - 查询参数
 * @param {String} params.keyword - 搜索关键词
 * @param {String} params.category - 分类
 * @param {Number} params.page - 页码
 * @param {Number} params.size - 每页数量
 */
export const getShopList = (params = {}) => {
  return get('/public/shop/list', params)
}

/**
 * 获取店铺详情
 * @param {Number} id - 店铺ID
 */
export const getShopDetail = (id) => {
  return get(`/public/shop/${id}`)
}

/**
 * 获取店铺菜单
 * @param {Number} id - 店铺ID
 */
export const getShopMenu = (id) => {
  return get(`/public/shop/${id}/menu`)
}

/**
 * 搜索菜品
 * @param {Object} params - 查询参数
 * @param {String} params.keyword - 搜索关键词
 */
export const searchDishes = (params = {}) => {
  return get('/public/shop/dish/search', params)
}

/**
 * 获取菜品详情
 * @param {Number} id - 菜品ID
 */
export const getDishDetail = (id) => {
  return get(`/public/shop/dish/${id}`)
}

export default {
  getShopList,
  getShopDetail,
  getShopMenu,
  searchDishes,
  getDishDetail
}
