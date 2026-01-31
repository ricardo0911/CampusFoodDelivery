<template>
  <view class="page">
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <view class="back-icon"></view>
      </view>
      <text class="nav-title">动态详情</text>
      <view class="nav-placeholder"></view>
    </view>

    <scroll-view class="content" scroll-y>
      <!-- 动态内容 -->
      <view class="post-detail" v-if="post">
        <view class="post-header">
          <image class="user-avatar" :src="post.userAvatar || '/static/default-avatar.jpg'" mode="aspectFill" />
          <view class="user-info">
            <text class="user-name">{{ post.userNickname || '美食达人' }}</text>
            <text class="post-time">{{ formatTime(post.createdAt) }}</text>
          </view>
        </view>

        <view class="post-content">
          <text class="content-text">{{ post.content }}</text>
          <view class="image-grid" v-if="post.images && getImages(post.images).length > 0">
            <image
              class="content-image"
              v-for="(img, index) in getImages(post.images)"
              :key="index"
              :src="img"
              mode="aspectFill"
              @click="previewImage(getImages(post.images), index)"
            />
          </view>
        </view>

        <view class="post-tag" v-if="post.shopName || post.dishName">
          <view class="tag-icon"></view>
          <text>{{ post.shopName || post.dishName }}</text>
        </view>

        <view class="post-stats">
          <text>{{ post.likes || 0 }} 赞</text>
          <text>{{ post.comments || 0 }} 评论</text>
        </view>
      </view>

      <!-- 评论列表 -->
      <view class="comments-section">
        <view class="section-header">
          <text class="section-title">评论 ({{ comments.length }})</text>
        </view>

        <view class="comment-list">
          <view class="comment-item" v-for="comment in comments" :key="comment.id">
            <image class="comment-avatar" :src="comment.userAvatar || '/static/default-avatar.jpg'" mode="aspectFill" />
            <view class="comment-content">
              <view class="comment-header">
                <text class="comment-name">{{ comment.userNickname || '用户' }}</text>
                <text class="comment-time">{{ formatTime(comment.createdAt) }}</text>
              </view>
              <text class="comment-text">
                <text v-if="comment.replyToNickname" class="reply-to">回复 @{{ comment.replyToNickname }}: </text>
                {{ comment.content }}
              </text>
            </view>
          </view>
        </view>

        <view class="empty-comments" v-if="comments.length === 0">
          <text>暂无评论，快来抢沙发吧</text>
        </view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view class="bottom-bar">
      <view class="input-wrapper" @click="showInput = true">
        <text class="input-placeholder">写评论...</text>
      </view>
      <view class="action-btn" :class="{ liked: post?.liked }" @click="handleLike">
        <view class="heart-icon"></view>
        <text>{{ post?.likes || 0 }}</text>
      </view>
    </view>

    <!-- 评论输入框 -->
    <view class="input-modal" v-if="showInput" @click="showInput = false">
      <view class="input-box" @click.stop>
        <input
          class="comment-input"
          v-model="commentText"
          placeholder="写评论..."
          :focus="showInput"
          @confirm="submitComment"
        />
        <view class="send-btn" @click="submitComment">发送</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const post = ref(null)
const comments = ref([])
const showInput = ref(false)
const commentText = ref('')
const postId = ref('')

const goBack = () => {
  uni.navigateBack()
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'

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

const loadData = async () => {
  try {
    const token = uni.getStorageSync('token')
    const res = await uni.request({
      url: `http://localhost:8080/api/customer/post/${postId.value}`,
      method: 'GET',
      header: token ? { 'Authorization': `Bearer ${token}` } : {}
    })

    if (res.data.code === 200) {
      post.value = res.data.data.post
      comments.value = res.data.data.comments || []
    }
  } catch (e) {
    console.error(e)
  }
}

const handleLike = async () => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }

  try {
    const method = post.value.liked ? 'DELETE' : 'POST'
    const res = await uni.request({
      url: `http://localhost:8080/api/customer/post/${postId.value}/like`,
      method,
      header: { 'Authorization': `Bearer ${token}` }
    })

    if (res.data.code === 200) {
      post.value.liked = !post.value.liked
      post.value.likes = post.value.liked ?
        (post.value.likes || 0) + 1 :
        Math.max((post.value.likes || 0) - 1, 0)
    }
  } catch (e) {
    console.error(e)
  }
}

const submitComment = async () => {
  if (!commentText.value.trim()) return

  const token = uni.getStorageSync('token')
  if (!token) {
    uni.navigateTo({ url: '/pages/login/login' })
    return
  }

  try {
    const res = await uni.request({
      url: `http://localhost:8080/api/customer/post/${postId.value}/comment`,
      method: 'POST',
      header: { 'Authorization': `Bearer ${token}` },
      data: { content: commentText.value }
    })

    if (res.data.code === 200) {
      uni.showToast({ title: '评论成功', icon: 'success' })
      commentText.value = ''
      showInput.value = false
      loadData()
    }
  } catch (e) {
    uni.showToast({ title: '评论失败', icon: 'none' })
  }
}

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  postId.value = page.options?.id
  if (postId.value) {
    loadData()
  }
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

.nav-placeholder {
  width: 60rpx;
}

.content {
  height: calc(100vh - 88rpx - var(--status-bar-height) - 100rpx);
}

.post-detail {
  background: #fff;
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
  font-size: 30rpx;
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
  font-size: 30rpx;
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

.post-stats {
  display: flex;
  gap: 30rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.post-stats text {
  font-size: 26rpx;
  color: #999;
}

.comments-section {
  background: #fff;
  padding: 24rpx;
}

.section-header {
  margin-bottom: 20rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.comment-item {
  display: flex;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  margin-right: 16rpx;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.comment-name {
  font-size: 26rpx;
  font-weight: 600;
  color: #333;
}

.comment-time {
  font-size: 22rpx;
  color: #999;
}

.comment-text {
  font-size: 28rpx;
  color: #333;
  line-height: 1.5;
}

.reply-to {
  color: #ff6b35;
}

.empty-comments {
  text-align: center;
  padding: 60rpx;
  color: #999;
  font-size: 26rpx;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 100rpx;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  gap: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.input-wrapper {
  flex: 1;
  height: 64rpx;
  background: #f5f5f5;
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
}

.input-placeholder {
  font-size: 28rpx;
  color: #999;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 12rpx 24rpx;
}

.heart-icon {
  width: 36rpx;
  height: 32rpx;
  background: #ccc;
  position: relative;
  transform: rotate(-45deg);
}

.heart-icon::before,
.heart-icon::after {
  content: '';
  position: absolute;
  width: 36rpx;
  height: 32rpx;
  background: #ccc;
  border-radius: 50%;
}

.heart-icon::before {
  top: -16rpx;
  left: 0;
}

.heart-icon::after {
  top: 0;
  left: 18rpx;
}

.action-btn.liked .heart-icon,
.action-btn.liked .heart-icon::before,
.action-btn.liked .heart-icon::after {
  background: #ff6b35;
}

.action-btn text {
  font-size: 26rpx;
  color: #666;
}

.input-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 999;
}

.input-box {
  width: 100%;
  background: #fff;
  padding: 24rpx;
  display: flex;
  gap: 20rpx;
}

.comment-input {
  flex: 1;
  height: 72rpx;
  background: #f5f5f5;
  border-radius: 36rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
}

.send-btn {
  width: 120rpx;
  height: 72rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  color: #fff;
}
</style>
