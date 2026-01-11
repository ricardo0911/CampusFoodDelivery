const BASE_URL = 'http://localhost:8080/api'

const request = (options) => {
    return new Promise((resolve, reject) => {
        const token = uni.getStorageSync('token')
        uni.request({
            url: BASE_URL + options.url,
            method: options.method || 'GET',
            data: options.data,
            header: {
                'Authorization': token ? `Bearer ${token}` : '',
                'Content-Type': 'application/json'
            },
            success: (res) => {
                if (res.data.code === 200) {
                    resolve(res.data)
                } else if (res.data.code === 401) {
                    // uni.removeStorageSync('token')
                    // uni.navigateTo({ url: '/pages/login/login' })
                    reject(res.data)
                } else {
                    uni.showToast({ title: res.data.message, icon: 'none' })
                    reject(res.data)
                }
            },
            fail: (err) => {
                uni.showToast({ title: '网络错误', icon: 'none' })
                reject(err)
            }
        })
    })
}

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
export const put = (url, data) => request({ url, method: 'PUT', data })
export const del = (url, data) => request({ url, method: 'DELETE', data })

export default { get, post, put, del }
