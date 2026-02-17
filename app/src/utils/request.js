export const BASE_URL = 'http://localhost:8080/api'

// 请求锁，防止重复请求（计数器模式）
const requestLock = new Map()

/**
 * 显示loading
 * @param {String} loadingKey - loading标识
 * @param {String} text - 提示文字
 */
const showLoading = (loadingKey, text = '加载中...') => {
  const count = requestLock.get(loadingKey) || 0
  if (count === 0) {
    uni.showLoading({ title: text, mask: true })
  }
  requestLock.set(loadingKey, count + 1)
}

/**
 * 隐藏loading
 * @param {String} loadingKey - loading标识
 */
const hideLoading = (loadingKey) => {
  const count = requestLock.get(loadingKey) || 0
  if (count <= 1) {
    uni.hideLoading()
    requestLock.delete(loadingKey)
  } else {
    requestLock.set(loadingKey, count - 1)
  }
}

/**
 * 处理401未授权
 */
let isRedirecting = false
const handleUnauthorized = () => {
  if (isRedirecting) return

  // 清除登录状态
  uni.removeStorageSync('token')
  uni.removeStorageSync('userInfo')

  // 跳转登录页
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const currentRoute = currentPage?.route || ''

  if (!currentRoute.includes('login')) {
    isRedirecting = true
    uni.showToast({
      title: '登录已过期，请重新登录',
      icon: 'none',
      duration: 1500
    })

    setTimeout(() => {
      isRedirecting = false
      uni.navigateTo({
        url: '/pages/login/login'
      })
    }, 1500)
  }
}

/**
 * 显示错误提示
 * @param {Object} error - 错误信息
 */
const showError = (error) => {
  const message = error?.message || '请求失败'
  // 避免重复显示相同的错误
  const lastError = uni.getStorageSync('lastError')
  if (lastError === message && Date.now() - (uni.getStorageSync('lastErrorTime') || 0) < 2000) {
    return
  }

  uni.setStorageSync('lastError', message)
  uni.setStorageSync('lastErrorTime', Date.now())

  uni.showToast({
    title: message,
    icon: 'none',
    duration: 2000
  })
}

const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    const loadingKey = options.url + JSON.stringify(options.data || {})

    // 显示loading
    if (options.showLoading !== false) {
      showLoading(loadingKey, options.loadingText || '加载中...')
    }

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Authorization': token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      },
      success: (res) => {
        // 隐藏loading
        hideLoading(loadingKey)

        // 处理 HTTP 状态码错误 (404, 500 等)
        if (res.statusCode >= 400) {
          console.log('API 错误:', res.statusCode, options.url)
          const error = { code: res.statusCode, message: '网络请求失败' }
          showError(error)
          reject(error)
          return
        }

        // 处理业务逻辑
        if (res.data && res.data.code === 200) {
          resolve(res.data)
        } else if (res.data && res.data.code === 401) {
          // 未授权
          handleUnauthorized()
          reject({ code: 401, message: '登录已过期' })
        } else if (res.data && res.data.code === 403) {
          // 无权限
          showError({ message: res.data.message || '无权限访问' })
          reject(res.data)
        } else if (res.data && res.data.code === 404) {
          // 资源不存在
          showError({ message: res.data.message || '请求的资源不存在' })
          reject(res.data)
        } else if (res.data && res.data.code === 500) {
          // 服务器错误
          showError({ message: '服务器繁忙，请稍后重试' })
          reject(res.data)
        } else {
          // 其他错误
          if (res.data && res.data.message) {
            showError(res.data)
          }
          reject(res.data || { code: -1, message: '未知错误' })
        }
      },
      fail: (err) => {
        // 隐藏loading
        hideLoading(loadingKey)

        console.log('网络错误:', err)

        // 网络错误处理
        if (err.errMsg && err.errMsg.includes('request:fail')) {
          showError({ message: '网络连接失败，请检查网络' })
          reject({ code: -1, message: '网络连接失败', type: 'network' })
        } else {
          showError({ message: '请求失败' })
          reject({ code: -1, message: '请求失败' })
        }
      }
    })
  })
}

export const get = (url, data, options = {}) => request({ url, method: 'GET', data, ...options })
export const post = (url, data, options = {}) => request({ url, method: 'POST', data, ...options })
export const put = (url, data, options = {}) => request({ url, method: 'PUT', data, ...options })
export const del = (url, data, options = {}) => request({ url, method: 'DELETE', data, ...options })

export default { get, post, put, del }
