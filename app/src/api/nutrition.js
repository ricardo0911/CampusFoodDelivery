import { get, post } from '@/utils/request'

/**
 * 营养分析相关 API
 */

/**
 * 获取今日营养报告
 */
export const getTodayNutrition = () => {
  return get('/customer/nutrition/today', {}, { showLoading: false })
}

/**
 * 获取周营养报告
 */
export const getWeeklyNutrition = () => {
  return get('/customer/nutrition/weekly')
}

export default {
  getTodayNutrition,
  getWeeklyNutrition
}