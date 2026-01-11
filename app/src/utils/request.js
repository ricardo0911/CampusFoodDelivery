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
                // 处理 HTTP 状态码错误 (404, 500 等)
                if (res.statusCode >= 400) {
                    console.log('API 错误:', res.statusCode, options.url)
                    reject({ code: res.statusCode, message: '网络请求失败' })
                    return
                }

                // 处理业务逻辑
                if (res.data && res.data.code === 200) {
                    resolve(res.data)
                } else if (res.data && res.data.code === 401) {
                    // 未授权，静默处理，让页面使用模拟数据
                    reject(res.data)
                } else {
                    // 其他错误，只有在有 message 时才显示 toast
                    if (res.data && res.data.message) {
                        uni.showToast({ title: res.data.message, icon: 'none' })
                    }
                    reject(res.data || { code: -1, message: '未知错误' })
                }
            },
            fail: (err) => {
                console.log('网络错误:', err)
                reject({ code: -1, message: '网络错误' })
            }
        })
    })
}

export const get = (url, data) => request({ url, method: 'GET', data })
export const post = (url, data) => request({ url, method: 'POST', data })
export const put = (url, data) => request({ url, method: 'PUT', data })
export const del = (url, data) => request({ url, method: 'DELETE', data })

export default { get, post, put, del }
