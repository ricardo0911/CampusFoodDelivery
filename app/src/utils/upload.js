const BASE_URL = 'http://localhost:8080/api'

/**
 * 文件上传工具类
 */

/**
 * 上传图片
 * @param {String} filePath - 文件路径 (从 uni.chooseImage 获取)
 * @param {Object} options - 配置选项
 * @returns {Promise} 返回上传结果
 */
export const uploadImage = (filePath, options = {}) => {
    return uploadFile(filePath, '/public/upload/image', options)
}

/**
 * 通用文件上传
 * @param {String} filePath - 文件路径
 * @param {String} url - 上传接口路径
 * @param {Object} options - 配置选项
 * @returns {Promise} 返回上传结果
 */
export const uploadFile = (filePath, url, options = {}) => {
    const {
        onProgressUpdate,
        timeout = 30000
    } = options

    return new Promise((resolve, reject) => {
        const token = uni.getStorageSync('token')

        const uploadTask = uni.uploadFile({
            url: BASE_URL + url,
            filePath: filePath,
            name: 'file',
            header: {
                'Authorization': token ? `Bearer ${token}` : '',
            },
            success: (res) => {
                if (res.statusCode >= 400) {
                    reject({ code: res.statusCode, message: '上传失败' })
                    return
                }

                try {
                    const data = JSON.parse(res.data)
                    if (data.code === 200) {
                        resolve(data)
                    } else {
                        reject(data)
                    }
                } catch (e) {
                    reject({ code: -1, message: '解析响应失败' })
                }
            },
            fail: (err) => {
                console.log('上传失败:', err)
                reject({ code: -1, message: '网络错误，上传失败' })
            }
        })

        // 监听上传进度
        if (onProgressUpdate && typeof onProgressUpdate === 'function') {
            uploadTask.onProgressUpdate((res) => {
                onProgressUpdate(res)
            })
        }
    })
}

/**
 * 批量上传图片
 * @param {Array} filePaths - 文件路径数组
 * @param {Object} options - 配置选项
 * @returns {Promise} 返回上传结果数组
 */
export const uploadImages = async (filePaths, options = {}) => {
    const results = []
    const errors = []

    for (let i = 0; i < filePaths.length; i++) {
        try {
            const result = await uploadImage(filePaths[i], options)
            results.push(result.data)
        } catch (error) {
            errors.push({ index: i, error })
        }
    }

    return {
        success: results,
        errors: errors,
        total: filePaths.length,
        successCount: results.length,
        errorCount: errors.length
    }
}

/**
 * 选择并上传图片
 * @param {Object} options - 配置选项
 * @returns {Promise} 返回上传结果
 */
export const chooseAndUploadImage = (options = {}) => {
    const {
        count = 1,
        sourceType = ['album', 'camera'],
        success,
        fail
    } = options

    return new Promise((resolve, reject) => {
        uni.chooseImage({
            count: count,
            sourceType: sourceType,
            success: (res) => {
                const tempFilePaths = res.tempFilePaths

                if (count === 1 || tempFilePaths.length === 1) {
                    // 单张图片
                    uploadImage(tempFilePaths[0], options)
                        .then((result) => {
                            if (success) success(result)
                            resolve(result)
                        })
                        .catch((error) => {
                            if (fail) fail(error)
                            reject(error)
                        })
                } else {
                    // 多张图片
                    uploadImages(tempFilePaths, options)
                        .then((result) => {
                            if (success) success(result)
                            resolve(result)
                        })
                        .catch((error) => {
                            if (fail) fail(error)
                            reject(error)
                        })
                }
            },
            fail: (err) => {
                console.log('选择图片失败:', err)
                if (fail) fail(err)
                reject({ code: -1, message: '选择图片失败' })
            }
        })
    })
}

/**
 * 从网络 URL 下载并上传图片（用于转发图片到服务器存储）
 * @param {String} imageUrl - 网络图片URL
 * @param {Object} options - 配置选项
 * @returns {Promise} 返回上传结果
 */
export const downloadAndUpload = (imageUrl, options = {}) => {
    return new Promise((resolve, reject) => {
        uni.downloadFile({
            url: imageUrl,
            success: (res) => {
                if (res.statusCode === 200) {
                    uploadFile(res.tempFilePath, '/public/upload/image', options)
                        .then((result) => resolve(result))
                        .catch((error) => reject(error))
                } else {
                    reject({ code: res.statusCode, message: '下载失败' })
                }
            },
            fail: (err) => {
                reject({ code: -1, message: '下载失败' })
            }
        })
    })
}

export default {
    uploadImage,
    uploadFile,
    uploadImages,
    chooseAndUploadImage,
    downloadAndUpload
}
