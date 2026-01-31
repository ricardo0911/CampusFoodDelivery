<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <view class="back-icon"></view>
      </view>
      <text class="nav-title">美食社区</text>
      <view class="nav-action" @click="goCreate">
        <text>发布</text>
      </view>
    </view>

    <!-- 动态列表 -->
    <scroll-view
      class="post-list"
      scroll-y
      @scrolltolower="loadMore"
    >
      <view
        class="post-item"
        v-for="post in posts"
        :key="post.id"
        @click="goDetail(post.id)"
      >
        <!-- 用户信息 -->
        <view class="post-header">
          <image class="user-avatar" :src="post.userAvatar || '/static/default-avatar.jpg'" mode="aspectFill" />
          <view class="user-info">
            <text class="user-name">{{ post.userNickname || '美食达人' }}</text>
            <text class="post-time">{{ formatTime(post.createdAt) }}</text>
          </view>
        </view>

        <!-- 内容 -->
        <view class="post-content">
          <text class="content-text">{{ post.content }}</text>
          <view class="image-grid" v-if="post.images && getImages(post.images).length > 0">
            <image
              class="content-image"
              v-for="(img, index) in getImages(post.images).slice(0, 9)"
              :key="index"
              :src="img"
              mode="aspectFill"
              @click.stop="previewImage(getImages(post.images), index)"
            />
          </view>
        </view>

        <!-- 关联信息 -->
        <view class="post-tag" v-if="post.shopName || post.dishName">
          <view class="tag-icon"></view>
          <text>{{ post.shopName || post.dishName }}</text>
        </view>

        <!-- 互动栏 -->
        <view class="post-actions">
          <view class="action-item" @click.stop="handleLike(post)">
            <view class="action-icon" :class="{ liked: post.liked }">
              <view class="heart-icon"></view>
            </view>
            <text :class="{ liked: post.liked }">{{ post.likes || 0 }}</text>
          </view>
          <view class="action-item">
            <view class="action-icon">
              <view class="comment-icon"></view>
            </view>
            <text>{{ post.comments || 0 }}</text>
          </view>
          <view class="action-item">
            <view class="action-icon">
              <view class="share-icon"></view>
            </view>
            <text>分享</text>
          </view>
        </view>
      </view>

      <view class="loading" v-if="loading">
        <text>加载中...</text>
      </view>
      <view class="no-more" v-if="noMore && posts.length > 0">
        <text>没有更多了</text>
      </view>
      <view class="empty" v-if="!loading && posts.length === 0">
        <text>暂无动态，快来发布第一条吧</text>
      </view>
    </scroll-view>

    <!-- 发布按钮 -->
    <view class="fab" @click="goCreate">
      <view class="fab-icon"></view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const posts = ref([])
const page = ref(1)
const loading = ref(false)
const noMore = ref(false)

const goBack = () => {
  uni.navigateBack()
}

const goCreate = () => {
  uni.navigateTo({ url: '/pages/community/create-post' })
}

const goDetail = (id) => {
  uni.navigateTo({ url: `/pages/community/post-detail?id=${id}` })
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'

  return `${date.getMonth() + 1}月${date.getDate()}日`
}

const getImages = (images) => {
  if (!images) return []
  if (Array.isArray(images)) return images
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

const previewImage = (images, index) => {
  uni.previewImage({
    urls: images,
    current: index
  })
}

const loadData = async (refresh = false) => {
  if (loading.value) return
  if (!refresh && noMore.value) return

  loading.value = true

  if (refresh) {
    page.value = 1
    noMore.value = false
  }

  try {
    const token = uni.getStorageSync('token')
    const res = await uni.request({
      url: `http://localhost:8080/api/customer/post/list?page=${page.value}&size=10`,
      method: 'GET',
      header: token ? { 'Authorization': `Bearer ${token}` } : {}
    })

    if (res.data.code === 200) {
      const data = res.data.data
      const records = data.records || []

      if (refresh) {
        posts.value = records
      } else {
        posts.value = [...posts.value, ...records]
      }

      if (records.length < 10) {
        noMore.value = true
      } else {
        page.value++
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  loadData()
}

const handleLike = async (post) => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }

  try {
    const method = post.liked ? 'DELETE' : 'POST'
    const res = await uni.request({
      url: `http://localhost:8080/api/customer/post/${post.id}/like`,
      method,
      header: { 'Authorization': `Bearer ${token}` }
    })

    if (res.data.code === 200) {
      post.liked = !post.liked
      post.likes = post.liked ? (post.likes || 0) + 1 : Math.max((post.likes || 0) - 1, 0)
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadData(true)
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  height: 88rpx;
  background: #fff;
  padding-top: var(--status-bar-height);
  border-bottom: 1rpx solid #f0f0f0;
}

.nav-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  width: 20rpx;
  height: 20rpx;
  border-left: 4rpx solid #333;
  border-bottom: 4rpx solid #333;
  transform: rotate(45deg);
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #333;
}

.nav-action {
  padding: 12rpx 24rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 30rpx;
  font-size: 26rpx;
  color: #fff;
}

.post-list {
  height: calc(100vh - 88rpx - var(--status-bar-height));
  padding: 20rpx;
  padding-bottom: 120rpx;
}

.post-item {
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  margin-right: 16rpx;
}

.user-info {
  flex: 1;
}

.user-name {
  display: block;
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 4rpx;
}

.post-time {
  font-size: 24rpx;
  color: #999;
}

.post-content {
  margin-bottom: 20rpx;
}

.content-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
  display: block;
  margin-bottom: 16rpx;
}

.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.content-image {
  width: calc(33.33% - 7rpx);
  aspect-ratio: 1;
  border-radius: 12rpx;
}

.post-tag {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  background: #fff5f0;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  margin-bottom: 20rpx;
}

.tag-icon {
  width: 24rpx;
  height: 24rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 50%;
}

.post-tag text {
  font-size: 24rpx;
  color: #ff6b35;
}

.post-actions {
  display: flex;
  justify-content: space-around;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.action-icon {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.heart-icon {
  width: 28rpx;
  height: 26rpx;
  background: #ccc;
  position: relative;
  transform: rotate(-45deg);
}

.heart-icon::before,
.heart-icon::after {
  content: '';
  position: absolute;
  width: 28rpx;
  height: 26rpx;
  background: #ccc;
  border-radius: 50%;
}

.heart-icon::before {
  top: -13rpx;
  left: 0;
}

.heart-icon::after {
  top: 0;
  left: 14rpx;
}

.action-icon.liked .heart-icon,
.action-icon.liked .heart-icon::before,
.action-icon.liked .heart-icon::after {
  background: #ff6b35;
}

.action-item text {
  font-size: 24rpx;
  color: #999;
}

.action-item text.liked {
  color: #ff6b35;
}

.comment-icon {
  width: 28rpx;
  height: 28rpx;
  border: 3rpx solid #ccc;
  border-radius: 6rpx;
  position: relative;
}

.comment-icon::after {
  content: '';
  position: absolute;
  bottom: -8rpx;
  left: 6rpx;
  width: 10rpx;
  height: 10rpx;
  border-left: 3rpx solid #ccc;
  border-bottom: 3rpx solid #ccc;
  transform: rotate(-45deg);
  background: #fff;
}

.share-icon {
  width: 28rpx;
  height: 28rpx;
  border: 3rpx solid #ccc;
  border-radius: 50%;
  position: relative;
}

.share-icon::after {
  content: '';
  position: absolute;
  top: 50%;
  right: -8rpx;
  transform: translateY(-50%);
  width: 0;
  height: 0;
  border-left: 10rpx solid #ccc;
  border-top: 6rpx solid transparent;
  border-bottom: 6rpx solid transparent;
}

.loading, .no-more, .empty {
  text-align: center;
  padding: 40rpx;
  color: #999;
  font-size: 26rpx;
}

.fab {
  position: fixed;
  right: 40rpx;
  bottom: 120rpx;
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.4);
}

.fab-icon {
  width: 40rpx;
  height: 40rpx;
  position: relative;
}

.fab-icon::before,
.fab-icon::after {
  content: '';
  position: absolute;
  background: #fff;
  border-radius: 4rpx;
}

.fab-icon::before {
  top: 50%;
  left: 0;
  right: 0;
  height: 6rpx;
  transform: translateY(-50%);
}

.fab-icon::after {
  left: 50%;
  top: 0;
  bottom: 0;
  width: 6rpx;
  transform: translateX(-50%);
}
</style>
