import { get, post, put, del } from '@/utils/request'

export const getCartList = () => get('/customer/cart/list')
export const addToCart = (data) => post('/customer/cart/add', data)
export const updateCartItem = (id, data) => put(`/customer/cart/update/${id}`, data)
export const deleteCartItem = (id) => del(`/customer/cart/delete/${id}`)
export const clearCart = () => del('/customer/cart/clear')

export default { getCartList, addToCart, updateCartItem, deleteCartItem, clearCart }
