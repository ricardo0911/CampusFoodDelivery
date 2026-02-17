<template>
  <view class="upload-container">
    <!-- 图片预览列表 -->
    <view class="preview-list" v-if="modelValue && modelValue.length > 0">
      <view class="preview-item" v-for="(url, index) in modelValue" :key="index">
        <image :src="getFullUrl(url)" mode="aspectFill" class="preview-image" @click="previewImage(index)"></image>
        <view class="delete-btn" @click="removeImage(index)" v-if="!disabled">
          <text class="delete-icon">×</text>
        </view>
        <!-- 上传进度 -->
        <view class="upload-progress" v-if="uploadingIndex === index">
          <text>{{ uploadProgress }}%</text>
        </view>
      </view>
    </view>

    <!-- 上传按钮 -->
    <view
      class="upload-btn"
      :class="{ disabled: disabled || isMax }"
      @click="handleClick"
      v-if="!isMax || showAddWhenMax"
    >
      <text class="upload-icon">+</text>
      <text class="upload-text">{{ buttonText }}</text>
    </view>

    <!-- 提示文字 -->
    <view class="tips" v-if="tips">{{ tips }}</view>
  </view>
</template>

<script>
import { uploadImage, uploadImages, chooseAndUploadImage } from '@/utils/upload.js'

export default {
  name: 'UploadImage',
  props: {
    // 双向绑定值：图片URL数组
    modelValue: {
      type: Array,
      default: () => []
    },
    // 最大上传数量
    max: {
      type: Number,
      default: 9
    },
    // 是否禁用
    disabled: {
      type: Boolean,
      default: false
    },
    // 按钮文字
    buttonText: {
      type: String,
      default: '上传图片'
    },
    // 提示文字
    tips: {
      type: String,
      default: '支持 JPG、PNG、GIF 格式，单张最大5MB'
    },
    // 基础URL（用于拼接相对路径）
    baseUrl: {
      type: String,
      default: 'http://localhost:8080'
    },
    // 是否在达到最大数量时仍然显示添加按钮
    showAddWhenMax: {
      type: Boolean,
      default: false
    },
    // 是否支持多选
    multiple: {
      type: Boolean,
      default: true
    }
  },
  emits: ['update:modelValue', 'change', 'error', 'success'],
  data() {
    return {
      uploadingIndex: -1,
      uploadProgress: 0,
      localUrls: [] // 本地临时文件路径，用于预览
    }
  },
  computed: {
    isMax() {
      return this.modelValue.length >= this.max
    }
  },
  methods: {
    // 获取完整的图片URL
    getFullUrl(url) {
      if (!url) return ''
      if (url.startsWith('http') || url.startsWith('/')) return url
      return this.baseUrl + url
    },

    // 处理点击上传
    handleClick() {
      if (this.disabled || this.isMax) return

      if (this.multiple && !this.isMax) {
        const count = Math.min(this.max - this.modelValue.length, 9)
        this.chooseImage(count)
      } else {
        this.chooseImage(1)
      }
    },

    // 选择图片
    chooseImage(count) {
      uni.chooseImage({
        count: count,
        sourceType: ['album', 'camera'],
        success: (res) => {
          const tempFilePaths = res.tempFilePaths

          if (tempFilePaths.length === 1) {
            this.uploadSingle(tempFilePaths[0], -1)
          } else {
            this.uploadMultiple(tempFilePaths)
          }
        },
        fail: (err) => {
          if (err.errMsg !== 'chooseImage:fail cancel') {
            this.$emit('error', { type: 'choose', error: err })
          }
        }
      })
    },

    // 单张上传
    async uploadSingle(filePath, localIndex) {
      const index = localIndex === -1 ? this.modelValue.length : localIndex
      this.uploadingIndex = index
      this.uploadProgress = 0

      try {
        const result = await uploadImage(filePath, {
          onProgressUpdate: (res) => {
            this.uploadProgress = res.progress
          }
        })

        // 添加到图片列表
        const newUrls = [...this.modelValue, result.data.url]
        this.updateValue(newUrls)
        this.$emit('success', { type: 'single', data: result.data, urls: newUrls })

        this.uploadingIndex = -1
      } catch (error) {
        this.uploadingIndex = -1
        this.$emit('error', { type: 'upload', error: error })
        uni.showToast({
          title: error.message || '上传失败',
          icon: 'none'
        })
      }
    },

    // 多张上传
    async uploadMultiple(filePaths) {
      const startIndex = this.modelValue.length
      let successCount = 0

      for (let i = 0; i < filePaths.length; i++) {
        const localIndex = startIndex + i
        this.uploadingIndex = localIndex
        this.uploadProgress = 0

        try {
          const result = await uploadImage(filePaths[i], {
            onProgressUpdate: (res) => {
              this.uploadProgress = res.progress
            }
          })

          const newUrls = [...this.modelValue]
          newUrls.push(result.data.url)
          this.updateValue(newUrls)
          successCount++
        } catch (error) {
          this.$emit('error', { type: 'upload', index: i, error: error })
        }
      }

      this.uploadingIndex = -1
      this.$emit('change', { type: 'multiple', count: filePaths.length, success: successCount })

      if (successCount < filePaths.length) {
        uni.showToast({
          title: `成功上传 ${successCount}/${filePaths.length} 张图片`,
          icon: 'none'
        })
      }
    },

    // 删除图片
    removeImage(index) {
      uni.showModal({
        title: '提示',
        content: '确定删除这张图片吗？',
        success: (res) => {
          if (res.confirm) {
            const newUrls = [...this.modelValue]
            newUrls.splice(index, 1)
            this.updateValue(newUrls)
            this.$emit('change', { type: 'remove', index: index, urls: newUrls })
          }
        }
      })
    },

    // 预览图片
    previewImage(index) {
      const urls = this.modelValue.map(url => this.getFullUrl(url))
      uni.previewImage({
        current: index,
        urls: urls
      })
    },

    // 更新值
    updateValue(value) {
      this.$emit('update:modelValue', value)
      this.$emit('change', { type: 'update', urls: value })
    }
  }
}
</script>

<style scoped>
.upload-container {
  padding: 20rpx;
}

.preview-list {
  display: flex;
  flex-wrap: wrap;
  margin-bottom: 20rpx;
}

.preview-item {
  position: relative;
  width: 180rpx;
  height: 180rpx;
  margin-right: 20rpx;
  margin-bottom: 20rpx;
  border-radius: 12rpx;
  overflow: hidden;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.delete-btn {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 40rpx;
  height: 40rpx;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-icon {
  color: #fff;
  font-size: 28rpx;
  font-weight: bold;
}

.upload-progress {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24rpx;
}

.upload-btn {
  width: 180rpx;
  height: 180rpx;
  border: 2rpx dashed #ddd;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

.upload-btn.disabled {
  opacity: 0.5;
}

.upload-icon {
  font-size: 60rpx;
  color: #999;
  line-height: 1;
}

.upload-text {
  font-size: 24rpx;
  color: #999;
  margin-top: 10rpx;
}

.tips {
  font-size: 24rpx;
  color: #999;
  margin-top: 16rpx;
}
</style>
