import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
    const userType = ref(localStorage.getItem('userType') || '')

    function setLogin(data, type) {
        token.value = data.token
        userInfo.value = data.user || data.merchant
        userType.value = type
        localStorage.setItem('token', data.token)
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        localStorage.setItem('userType', type)
    }

    function logout() {
        token.value = ''
        userInfo.value = {}
        userType.value = ''
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('userType')
    }

    return { token, userInfo, userType, setLogin, logout }
})
