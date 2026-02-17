import { get, put, post, del } from '@/utils/request'

export const getUserInfo = () => get('/customer/user/info')
export const updateUserInfo = (data) => put('/customer/user/update', data)
export const getAddressList = () => get('/customer/user/address')
export const addAddress = (data) => post('/customer/user/address/add', data)
export const updateAddress = (id, data) => put(`/customer/user/address/${id}`, data)
export const deleteAddress = (id) => del(`/customer/user/address/${id}`)

export default { getUserInfo, updateUserInfo, getAddressList, addAddress, updateAddress, deleteAddress }
