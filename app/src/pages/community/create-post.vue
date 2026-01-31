<template>
  <view class="page">
    <view class="header">
      <view class="close-btn" @click="goBack">
        <view class="close-icon"></view>
      </view>
      <text class="header-title">发布动态</text>
      <view class="publish-btn" :class="{ disabled: !canPublish }" @click="handlePublish">
        发布
      </view>
    </view>

    <view class="content">
      <textarea
        class="input-area"
        v-model="content"
        placeholder="分享你的美食体验..."
        maxlength="500"
        :auto-height="true"
      />

      <view class="image-section">
        <view class="image-list">
          <view class="image-item" v-for="(img, index) in images" :key="index">
            <image :src="img" mode="aspectFill" />
            <view class="remove-btn" @click="removeImage(index)">
              <view class="remove-icon"></view>
            </view>
          </view>
          <view class="add-image" v-if="images.length < 9" @click="chooseImage">
            <view class="add-icon"></view>
            <text>添加图片</text>
          </view>
        </view>
      </view>

      <view class="tag-section">
        <text class="section-title">关联内容（可选）</text>
        <view class="tag-list">
          <view
            class="tag-item"
            :class="{ active: tagType === 'shop' }"
            @click="selectTag('shop')"
          >
            <view class="tag-icon shop"></view>
            <text>关联店铺</text>
          </view>
          <view
            class="tag-item"
            :class="{ active: tagType === 'dish' }"
            @click="selectTag('dish')"
          >
            <view class="tag-icon dish"></view>
            <text>关联菜品</text>
          </view>
        </view>
      </view>
    </view>

    <view class="char-count">
      {{ content.length }}/500
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const content = ref('')
const images = ref([])
const tagType = ref('')
const shopId = ref(null)
const dishId = ref(null)

const canPublish = computed(() => {
  return content.value.trim().length > 0 || images.value.length > 0
})

const goBack = () => {
  uni.navigateBack()
}

const chooseImage = () => {
  uni.chooseImage({
    count: 9 - images.value.length,
    success: (res) => {
      images.value = [...images.value, ...res.tempFilePaths]
    }
  })
}

const removeImage = (index) => {
  images.value.splice(index, 1)
}

const selectTag = (type) => {
  if (tagType.value === type) {
    tagType.value = ''
    shopId.value = null
    dishId.value = null
  } else {
    tagType.value = type
    uni.showToast({ title: '功能开发中', icon: 'none' })
  }
}

const handlePublish = async () => {
  if (!canPublish.value) return

  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }

  try {
    uni.showLoading({ title: '发布中...' })

    const res = await uni.request({
      url: 'http://localhost:8080/api/customer/post/create',
      method: 'POST',
      header: { 'Authorization': `Bearer ${token}` },
      data: {
        content: content.value,
        images: JSON.stringify(images.value),
        shopId: shopId.value,
        dishId: dishId.value
      }
    })

    uni.hideLoading()

    if (res.data.code === 200) {
      uni.showToast({ title: '发布成功', icon: 'success' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      uni.showToast({ title: res.data.message || '发布失败', icon: 'none' })
    }
  } catch (e) {
    uni.hideLoading()
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #fff;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  height: 88rpx;
  padding-top: var(--status-bar-height);
  border-bottom: 1rpx solid #f0f0f0;
}

.close-btn {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-icon {
  width: 32rpx;
  height: 32rpx;
  position: relative;
}

.close-icon::before,
.close-icon::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  height: 4rpx;
  background: #333;
  border-radius: 2rpx;
}

.close-icon::before {
  transform: rotate(45deg);
}

.close-icon::after {
  transform: rotate(-45deg);
}

.header-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.publish-btn {
  padding: 12rpx 32rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 30rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #fff;
}

.publish-btn.disabled {
  background: #ccc;
}

.content {
  padding: 30rpx;
}

.input-area {
  width: 100%;
  min-height: 300rpx;
  font-size: 32rpx;
  line-height: 1.6;
  color: #333;
}

.image-section {
  margin-top: 30rpx;
}

.image-list {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.image-item {
  width: calc(33.33% - 11rpx);
  aspect-ratio: 1;
  position: relative;
  border-radius: 12rpx;
  overflow: hidden;
}

.image-item image {
  width: 100%;
  height: 100%;
}

.remove-btn {
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

.remove-icon {
  width: 20rpx;
  height: 20rpx;
  position: relative;
}

.remove-icon::before,
.remove-icon::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  height: 3rpx;
  background: #fff;
}

.remove-icon::before {
  transform: rotate(45deg);
}

.remove-icon::after {
  transform: rotate(-45deg);
}

.add-image {
  width: calc(33.33% - 11rpx);
  aspect-ratio: 1;
  background: #f5f5f5;
  border-radius: 12rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
}

.add-icon {
  width: 48rpx;
  height: 48rpx;
  position: relative;
}

.add-icon::before,
.add-icon::after {
  content: '';
  position: absolute;
  background: #999;
  border-radius: 2rpx;
}

.add-icon::before {
  top: 50%;
  left: 0;
  right: 0;
  height: 4rpx;
  transform: translateY(-50%);
}

.add-icon::after {
  left: 50%;
  top: 0;
  bottom: 0;
  width: 4rpx;
  transform: translateX(-50%);
}

.add-image text {
  font-size: 24rpx;
  color: #999;
}

.tag-section {
  margin-top: 40rpx;
  padding-top: 30rpx;
  border-top: 1rpx solid #f0f0f0;
}

.section-title {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 20rpx;
  display: block;
}

.tag-list {
  display: flex;
  gap: 20rpx;
}

.tag-item {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 16rpx 24rpx;
  background: #f5f5f5;
  border-radius: 30rpx;
  border: 2rpx solid transparent;
}

.tag-item.active {
  background: #fff5f0;
  border-color: #ff6b35;
}

.tag-icon {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
}

.tag-icon.shop {
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
}

.tag-icon.dish {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
}

.tag-item text {
  font-size: 26rpx;
  color: #666;
}

.tag-item.active text {
  color: #ff6b35;
}

.char-count {
  position: fixed;
  bottom: 40rpx;
  right: 40rpx;
  font-size: 24rpx;
  color: #999;
}
</style>
