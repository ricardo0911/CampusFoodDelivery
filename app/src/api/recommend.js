import { get, post } from '@/utils/request'

export const getHomeRecommend = () => get('/customer/recommend/home', {}, { showLoading: false })
export const getSearchRecommend = (keyword) => get('/customer/recommend/search', { keyword })
export const getCartRecommend = () => get('/customer/recommend/cart', {}, { showLoading: false })
export const getDishRecommend = (dishId) => get(`/customer/recommend/detail/${dishId}`)

export default { getHomeRecommend, getSearchRecommend, getCartRecommend, getDishRecommend }
