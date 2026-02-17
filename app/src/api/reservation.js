import { get, post, del } from '@/utils/request'

export const getTimeslots = (shopId) => get(`/customer/reservation/timeslots/${shopId}`)
export const getAllTimeslots = (shopId) => get(`/customer/reservation/timeslots/${shopId}/all`)
export const createReservation = (data) => post('/customer/reservation', data)
export const getReservationByOrder = (orderId) => get(`/customer/reservation/order/${orderId}`)
export const cancelReservation = (orderId) => del(`/customer/reservation/order/${orderId}`)

export default { getTimeslots, getAllTimeslots, createReservation, getReservationByOrder, cancelReservation }
