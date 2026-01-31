<template>
  <view class="page">
    <!-- 头部 -->
    <view class="header">
      <view class="title-bar">
        <text class="back-btn" @click="goBack">←</text>
        <text class="title">🏆 我的徽章</text>
      </view>
      
      <!-- 徽章统计 -->
      <view class="stats-card">
        <view class="stats-ring">
          <view class="ring-progress" :style="{ '--progress': stats.progress + '%' }">
            <text class="ring-value">{{ stats.obtained }}</text>
            <text class="ring-label">/{{ stats.total }}</text>
          </view>
        </view>
        <view class="stats-info">
          <text class="stats-title">收集进度</text>
          <text class="stats-desc">已解锁 {{ stats.obtained }} 个徽章</text>
          <view class="stats-bar">
            <view class="bar-fill" :style="{ width: stats.progress + '%' }"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 徽章分类 -->
    <view class="category-tabs">
      <view 
        class="category-tab" 
        :class="{ active: activeCategory === 'all' }"
        @click="activeCategory = 'all'"
      >
        全部
      </view>
      <view 
        class="category-tab" 
        :class="{ active: activeCategory === 1 }"
        @click="activeCategory = 1"
      >
        订单类
      </view>
      <view 
        class="category-tab" 
        :class="{ active: activeCategory === 2 }"
        @click="activeCategory = 2"
      >
        消费类
      </view>
      <view 
        class="category-tab" 
        :class="{ active: activeCategory === 4 }"
        @click="activeCategory = 4"
      >
        特殊类
      </view>
    </view>

    <!-- 徽章列表 -->
    <scroll-view class="badges-container" scroll-y>
      <view class="badge-grid">
        <view 
          class="badge-card" 
          v-for="badge in filteredBadges" 
          :key="badge.id"
          :class="{ obtained: badge.obtained, locked: !badge.obtained }"
          @click="showBadgeDetail(badge)"
        >
          <view class="badge-glow" v-if="badge.obtained"></view>
          <view class="badge-icon-container" :class="'rarity-' + badge.rarity">
            <text class="badge-icon">{{ badge.icon }}</text>
          </view>
          <text class="badge-name">{{ badge.name }}</text>
          <view class="badge-rarity">
            <text v-for="n in badge.rarity" :key="n" class="star">★</text>
          </view>
          <view class="badge-lock" v-if="!badge.obtained">
            <text>🔒</text>
          </view>
        </view>
      </view>
      
      <!-- 底部提示 -->
      <view class="footer-tip">
        <text>完成更多订单，解锁更多徽章</text>
      </view>
    </scroll-view>

    <!-- 徽章详情弹窗 -->
    <view class="modal" v-if="showDetail" @click="showDetail = false">
      <view class="detail-card" @click.stop>
        <view class="detail-header" :class="'rarity-bg-' + currentBadge.rarity">
          <text class="detail-icon">{{ currentBadge.icon }}</text>
          <view class="detail-stars">
            <text v-for="n in currentBadge.rarity" :key="n" class="star">★</text>
          </view>
        </view>
        
        <view class="detail-content">
          <text class="detail-name">{{ currentBadge.name }}</text>
          <text class="detail-desc">{{ currentBadge.description }}</text>
          
          <view class="detail-status" v-if="currentBadge.obtained">
            <text class="status-icon">✅</text>
            <text class="status-text">已获得</text>
            <text class="status-time" v-if="currentBadge.obtainedAt">
              {{ formatDate(currentBadge.obtainedAt) }}
            </text>
          </view>
          
          <view class="detail-status locked" v-else>
            <text class="status-icon">🔒</text>
            <text class="status-text">未解锁</text>
            <text class="status-hint">{{ getHint(currentBadge) }}</text>
          </view>
          
          <view class="rarity-info">
            <text class="rarity-label">稀有度：</text>
            <text class="rarity-name" :class="'rarity-text-' + currentBadge.rarity">
              {{ getRarityName(currentBadge.rarity) }}
            </text>
          </view>
        </view>
        
        <button class="btn-close" @click="showDetail = false">知道了</button>
      </view>
    </view>

    <!-- 新徽章获得动画 -->
    <view class="new-badge-animation" v-if="showNewBadge">
      <view class="animation-bg"></view>
      <view class="animation-content">
        <view class="particles">
          <view v-for="n in 12" :key="n" class="particle" :style="getParticleStyle(n)"></view>
        </view>
        <text class="congrats-text">🎉 恭喜获得新徽章！</text>
        <view class="new-badge-icon">{{ newBadge.icon }}</view>
        <text class="new-badge-name">{{ newBadge.name }}</text>
        <text class="new-badge-desc">{{ newBadge.description }}</text>
        <button class="btn-collect" @click="collectBadge">收下徽章</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const activeCategory = ref('all')
const badges = ref([])
const stats = ref({ total: 0, obtained: 0, progress: 0 })
const showDetail = ref(false)
const currentBadge = ref({})
const showNewBadge = ref(false)
const newBadge = ref({})

// 筛选后的徽章
const filteredBadges = computed(() => {
  if (activeCategory.value === 'all') {
    return badges.value
  }
  return badges.value.filter(b => b.type === activeCategory.value)
})

// 加载徽章数据
const loadBadges = () => {
  // 模拟数据
  badges.value = [
    { id: 1, name: '新手上路', description: '完成第一单', icon: '🎉', type: 1, rarity: 1, obtained: true, obtainedAt: '2024-01-15T10:30:00' },
    { id: 2, name: '常客', description: '累计下单5次', icon: '⭐', type: 1, rarity: 1, obtained: true, obtainedAt: '2024-01-18T14:20:00' },
    { id: 3, name: '老顾客', description: '累计下单20次', icon: '🌟', type: 1, rarity: 2, obtained: false },
    { id: 4, name: '美食达人', description: '累计下单50次', icon: '👑', type: 1, rarity: 3, obtained: false },
    { id: 5, name: '传说食客', description: '累计下单100次', icon: '🏆', type: 1, rarity: 4, obtained: false },
    { id: 6, name: '小确幸', description: '累计消费满100元', icon: '💰', type: 2, rarity: 1, obtained: true, obtainedAt: '2024-01-16T12:00:00' },
    { id: 7, name: '大手笔', description: '累计消费满500元', icon: '💎', type: 2, rarity: 2, obtained: false },
    { id: 8, name: '土豪金', description: '累计消费满1000元', icon: '🏅', type: 2, rarity: 3, obtained: false },
    { id: 9, name: '饮品爱好者', description: '购买过饮品类商品', icon: '🧋', type: 4, rarity: 1, obtained: true, obtainedAt: '2024-01-17T09:45:00' },
    { id: 10, name: '甜品控', description: '购买过甜点类商品', icon: '🍰', type: 4, rarity: 1, obtained: false },
    { id: 11, name: '快餐达人', description: '购买过快餐类商品', icon: '🍔', type: 4, rarity: 1, obtained: true, obtainedAt: '2024-01-15T11:00:00' },
    { id: 12, name: '早起鸟', description: '在8点前下单', icon: '🌅', type: 4, rarity: 2, obtained: false },
    { id: 13, name: '夜猫子', description: '在22点后下单', icon: '🌙', type: 4, rarity: 2, obtained: false },
    { id: 14, name: '辣王', description: '点过3份特辣菜品', icon: '🌶️', type: 4, rarity: 3, obtained: false },
    { id: 15, name: '拼单达人', description: '成功发起5次拼单', icon: '👥', type: 4, rarity: 3, obtained: false },
    { id: 16, name: '探店王', description: '在10家不同店铺下单', icon: '🗺️', type: 4, rarity: 4, obtained: false }
  ]
  
  // 计算统计
  const total = badges.value.length
  const obtained = badges.value.filter(b => b.obtained).length
  stats.value = {
    total,
    obtained,
    progress: Math.round((obtained / total) * 100)
  }
}

// 显示徽章详情
const showBadgeDetail = (badge) => {
  currentBadge.value = badge
  showDetail.value = true
}

// 获取稀有度名称
const getRarityName = (rarity) => {
  const names = ['', '普通', '稀有', '史诗', '传说']
  return names[rarity] || '普通'
}

// 获取提示
const getHint = (badge) => {
  return badge.description
}

// 格式化日期
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 粒子样式
const getParticleStyle = (n) => {
  const angle = (n / 12) * 360
  return {
    '--angle': angle + 'deg',
    '--delay': (n * 0.1) + 's'
  }
}

// 收下徽章
const collectBadge = () => {
  showNewBadge.value = false
}

// 模拟获得新徽章
const simulateNewBadge = () => {
  // 可以通过某些条件触发
  newBadge.value = { 
    icon: '🎉', 
    name: '新手上路', 
    description: '完成第一单' 
  }
  // showNewBadge.value = true
}

const goBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  loadBadges()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

/* 头部 */
.header {
  padding: 60rpx 40rpx 30rpx;
}

.title-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-bottom: 30rpx;
}

.back-btn {
  position: absolute;
  left: 0;
  font-size: 40rpx;
  color: #fff;
}

.title {
  font-size: 44rpx;
  font-weight: bold;
  color: #fff;
}

/* 统计卡片 */
.stats-card {
  display: flex;
  align-items: center;
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border-radius: 24rpx;
  padding: 30rpx;
  border: 1rpx solid rgba(255,255,255,0.2);
}

.stats-ring {
  width: 120rpx;
  height: 120rpx;
  margin-right: 30rpx;
}

.ring-progress {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: conic-gradient(
    #ffd700 0deg calc(var(--progress) * 3.6deg),
    rgba(255,255,255,0.2) calc(var(--progress) * 3.6deg) 360deg
  );
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.ring-progress::before {
  content: '';
  position: absolute;
  width: 80%;
  height: 80%;
  background: #1a1a2e;
  border-radius: 50%;
}

.ring-value {
  position: relative;
  font-size: 36rpx;
  font-weight: bold;
  color: #ffd700;
  z-index: 1;
}

.ring-label {
  position: relative;
  font-size: 20rpx;
  color: rgba(255,255,255,0.6);
  z-index: 1;
}

.stats-info {
  flex: 1;
}

.stats-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
  display: block;
  margin-bottom: 8rpx;
}

.stats-desc {
  font-size: 24rpx;
  color: rgba(255,255,255,0.7);
  display: block;
  margin-bottom: 16rpx;
}

.stats-bar {
  height: 12rpx;
  background: rgba(255,255,255,0.2);
  border-radius: 6rpx;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #ffd700, #ff6b6b);
  border-radius: 6rpx;
  transition: width 0.5s ease;
}

/* 分类标签 */
.category-tabs {
  display: flex;
  padding: 20rpx 40rpx;
  gap: 20rpx;
}

.category-tab {
  padding: 16rpx 28rpx;
  background: rgba(255,255,255,0.1);
  border-radius: 30rpx;
  font-size: 26rpx;
  color: rgba(255,255,255,0.7);
  border: 1rpx solid transparent;
  transition: all 0.3s;
}

.category-tab.active {
  background: rgba(255, 215, 0, 0.2);
  border-color: #ffd700;
  color: #ffd700;
}

/* 徽章容器 */
.badges-container {
  height: calc(100vh - 450rpx);
  padding: 20rpx 30rpx;
}

.badge-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.badge-card {
  width: calc(33.33% - 14rpx);
  aspect-ratio: 1;
  background: rgba(255,255,255,0.08);
  border-radius: 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  border: 1rpx solid rgba(255,255,255,0.1);
  transition: all 0.3s;
}

.badge-card.obtained {
  background: rgba(255, 215, 0, 0.1);
  border-color: rgba(255, 215, 0, 0.3);
}

.badge-card.locked {
  opacity: 0.6;
}

.badge-glow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at center, rgba(255, 215, 0, 0.2) 0%, transparent 70%);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.badge-icon-container {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.badge-icon-container.rarity-1 { background: linear-gradient(135deg, #a8a8a8, #d4d4d4); }
.badge-icon-container.rarity-2 { background: linear-gradient(135deg, #4a90e2, #67b3f4); }
.badge-icon-container.rarity-3 { background: linear-gradient(135deg, #9b59b6, #c39bd3); }
.badge-icon-container.rarity-4 { background: linear-gradient(135deg, #f39c12, #f1c40f); }

.badge-icon {
  font-size: 40rpx;
}

.badge-name {
  font-size: 22rpx;
  color: #fff;
  text-align: center;
  margin-bottom: 8rpx;
}

.badge-rarity {
  display: flex;
  gap: 2rpx;
}

.star {
  font-size: 18rpx;
  color: #ffd700;
}

.badge-lock {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  font-size: 24rpx;
}

/* 底部提示 */
.footer-tip {
  text-align: center;
  padding: 40rpx;
  color: rgba(255,255,255,0.5);
  font-size: 24rpx;
}

/* 详情弹窗 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.detail-card {
  width: 80%;
  background: #1a1a2e;
  border-radius: 24rpx;
  overflow: hidden;
  border: 1rpx solid rgba(255,255,255,0.2);
}

.detail-header {
  padding: 50rpx;
  text-align: center;
}

.detail-header.rarity-bg-1 { background: linear-gradient(135deg, #a8a8a8, #d4d4d4); }
.detail-header.rarity-bg-2 { background: linear-gradient(135deg, #4a90e2, #67b3f4); }
.detail-header.rarity-bg-3 { background: linear-gradient(135deg, #9b59b6, #c39bd3); }
.detail-header.rarity-bg-4 { background: linear-gradient(135deg, #f39c12, #f1c40f); }

.detail-icon {
  font-size: 80rpx;
  display: block;
  margin-bottom: 16rpx;
}

.detail-stars {
  display: flex;
  justify-content: center;
  gap: 8rpx;
}

.detail-stars .star {
  font-size: 28rpx;
}

.detail-content {
  padding: 30rpx;
  text-align: center;
}

.detail-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  display: block;
  margin-bottom: 12rpx;
}

.detail-desc {
  font-size: 26rpx;
  color: rgba(255,255,255,0.7);
  display: block;
  margin-bottom: 30rpx;
}

.detail-status {
  background: rgba(255, 215, 0, 0.1);
  border-radius: 12rpx;
  padding: 20rpx;
  margin-bottom: 20rpx;
}

.detail-status.locked {
  background: rgba(255,255,255,0.05);
}

.status-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.status-text {
  font-size: 28rpx;
  color: #ffd700;
}

.detail-status.locked .status-text {
  color: rgba(255,255,255,0.5);
}

.status-time, .status-hint {
  display: block;
  font-size: 22rpx;
  color: rgba(255,255,255,0.5);
  margin-top: 8rpx;
}

.rarity-info {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8rpx;
}

.rarity-label {
  font-size: 24rpx;
  color: rgba(255,255,255,0.6);
}

.rarity-name {
  font-size: 24rpx;
  font-weight: bold;
}

.rarity-text-1 { color: #d4d4d4; }
.rarity-text-2 { color: #67b3f4; }
.rarity-text-3 { color: #c39bd3; }
.rarity-text-4 { color: #f1c40f; }

.btn-close {
  width: calc(100% - 60rpx);
  margin: 30rpx;
  background: linear-gradient(135deg, #ffd700, #ff6b6b);
  color: #fff;
  font-size: 30rpx;
  font-weight: bold;
  padding: 24rpx;
  border-radius: 40rpx;
  border: none;
}

/* 新徽章动画 */
.new-badge-animation {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 200;
}

.animation-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.9);
}

.animation-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.particles {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.particle {
  position: absolute;
  width: 10rpx;
  height: 10rpx;
  background: #ffd700;
  border-radius: 50%;
  animation: particle-burst 1s ease-out var(--delay) forwards;
  transform: rotate(var(--angle)) translateY(-100rpx);
}

@keyframes particle-burst {
  0% {
    opacity: 1;
    transform: rotate(var(--angle)) translateY(0);
  }
  100% {
    opacity: 0;
    transform: rotate(var(--angle)) translateY(-200rpx);
  }
}

.congrats-text {
  font-size: 36rpx;
  color: #ffd700;
  display: block;
  margin-bottom: 30rpx;
}

.new-badge-icon {
  font-size: 120rpx;
  display: block;
  margin-bottom: 20rpx;
  animation: badge-bounce 0.6s ease;
}

@keyframes badge-bounce {
  0% { transform: scale(0); }
  50% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

.new-badge-name {
  font-size: 40rpx;
  font-weight: bold;
  color: #fff;
  display: block;
  margin-bottom: 12rpx;
}

.new-badge-desc {
  font-size: 26rpx;
  color: rgba(255,255,255,0.7);
  display: block;
  margin-bottom: 40rpx;
}

.btn-collect {
  background: linear-gradient(135deg, #ffd700, #ff6b6b);
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  padding: 24rpx 80rpx;
  border-radius: 50rpx;
  border: none;
}
</style>
