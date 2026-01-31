<template>
  <view class="page">
    <!-- 装饰星星 (使用静态类名) -->
    <view class="stars-bg">
      <view class="star star-1"></view>
      <view class="star star-2"></view>
      <view class="star star-3"></view>
      <view class="star star-4"></view>
      <view class="star star-5"></view>
      <view class="star star-6"></view>
      <view class="star star-7"></view>
      <view class="star star-8"></view>
    </view>

    <!-- 头部 -->
    <view class="header">
      <view class="title-bar">
        <text class="back-btn" @click="goBack">←</text>
        <text class="title">🎰 今天吃什么</text>
      </view>
      <text class="subtitle">转动命运之轮，让选择不再困难</text>
      
      <!-- 幸运指数 -->
      <view class="lucky-bar">
        <text class="lucky-label">🍀 今日幸运指数</text>
        <view class="lucky-stars">
          <text v-for="n in 5" :key="n" :class="n <= luckyIndex ? 'star-on' : 'star-off'">★</text>
        </view>
        <text class="lucky-hint">{{ luckyHint }}</text>
      </view>
    </view>

    <!-- 转盘区域 -->
    <view class="wheel-container">
      <view class="wheel-wrapper">
        
        <!-- 转盘 -->
        <view 
          class="wheel" 
          :style="{ transform: `rotate(${rotation}deg)`, transition: isSpinning ? 'transform 4s cubic-bezier(0.17, 0.67, 0.12, 0.99)' : 'none' }"
        >
          <view 
            v-for="(item, index) in wheelItems" 
            :key="index"
            class="wheel-item"
            :style="getItemStyle(index)"
          >
            <view class="item-content" :style="getItemContentStyle(index)">
              <text class="item-icon">{{ item.icon }}</text>
              <text class="item-name">{{ item.name }}</text>
            </view>
          </view>
        </view>
        
        <!-- 中心按钮 -->
        <view class="wheel-center" :class="{ spinning: isSpinning }" @click="spin">
          <text class="center-text">{{ isSpinning ? '🎲' : '开始' }}</text>
        </view>
        
        <!-- 指针 -->
        <view class="wheel-pointer">▼</view>
      </view>
      
      <!-- 转动次数 -->
      <view class="spin-count">
        <text>今日已转 {{ spinCount }} 次</text>
      </view>
    </view>

    <!-- 结果展示 -->
    <view class="result-section" v-if="selectedItem">
      <view class="result-card">
        <view class="confetti" v-if="showConfetti">🎉</view>
        <view class="result-header">
          <text class="result-icon">{{ selectedItem.icon }}</text>
          <view class="result-info">
            <text class="result-name">{{ selectedItem.name }}</text>
            <view class="result-meta">
              <text class="rating">⭐ {{ selectedItem.rating?.toFixed(1) || '4.5' }}</text>
              <text class="delivery">🚴 {{ selectedItem.deliveryTime || 25 }}分钟</text>
            </view>
          </view>
        </view>
        <view class="result-actions">
          <button class="btn-go" @click="goToShop">去点餐 →</button>
          <button class="btn-again" @click="spin">再转一次</button>
        </view>
      </view>
    </view>

    <!-- 历史记录 -->
    <view class="history-section" v-if="history.length > 0">
      <text class="section-title">📜 转盘记录</text>
      <scroll-view class="history-list" scroll-x>
        <view class="history-item" v-for="(item, index) in history" :key="index">
          <text class="history-icon">{{ item.icon }}</text>
          <text class="history-name">{{ item.name }}</text>
          <text class="history-time">{{ item.time }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 模式切换 -->
    <view class="mode-section">
      <text class="section-title">选择模式</text>
      <view class="mode-tabs">
        <view 
          class="mode-tab" 
          :class="{ active: mode === 'shop' }"
          @click="switchMode('shop')"
        >
          <text class="mode-icon">🏪</text>
          <text class="mode-name">选店铺</text>
        </view>
        <view 
          class="mode-tab" 
          :class="{ active: mode === 'dish' }"
          @click="switchMode('dish')"
        >
          <text class="mode-icon">🍽️</text>
          <text class="mode-name">选菜品</text>
        </view>
        <view 
          class="mode-tab" 
          :class="{ active: mode === 'category' }"
          @click="switchMode('category')"
        >
          <text class="mode-icon">📂</text>
          <text class="mode-name">选分类</text>
        </view>
      </view>
    </view>

    <!-- 今日挑战 -->
    <view class="challenge-section">
      <view class="challenge-card" @click="refreshChallenge">
        <view class="challenge-icon">{{ challenge.icon }}</view>
        <view class="challenge-info">
          <text class="challenge-name">{{ challenge.name }}</text>
          <text class="challenge-desc">{{ challenge.description }}</text>
          <text class="challenge-reward">🎁 {{ challenge.reward }}</text>
        </view>
        <view class="refresh-btn">🔄</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { get } from '@/utils/request'

const mode = ref('shop')
const wheelItems = ref([])
const rotation = ref(0)
const isSpinning = ref(false)
const selectedItem = ref(null)
const spinCount = ref(0)
const history = ref([])
const showConfetti = ref(false)
const luckyIndex = ref(Math.floor(Math.random() * 3) + 3) // 3-5星

const luckyHint = computed(() => {
  const hints = ['运气一般', '运气不错', '运气很好', '超级幸运', '欧皇附体']
  return hints[luckyIndex.value - 1] || '运气不错'
})

const challenge = ref({
  name: '探店达人',
  description: '在5家不同店铺下单',
  reward: '获得【探店达人】徽章',
  icon: '🗺️'
})

// 获取扇形样式
const getItemStyle = (index) => {
  const count = wheelItems.value.length
  const angle = 360 / count
  return {
    transform: `rotate(${index * angle}deg)`,
    clipPath: `polygon(50% 50%, 50% 0%, ${50 + 50 * Math.tan(Math.PI / count)}% 0%, 50% 50%)`
  }
}

const getItemContentStyle = (index) => {
  const count = wheelItems.value.length
  const angle = 360 / count
  return {
    transform: `rotate(${angle / 2}deg) translateY(-80rpx)`
  }
}

// 店铺图标映射
const shopIcons = ['🍚', '🍜', '🍲', '🥟', '🧋', '🍔', '☕', '🍕']

// 加载转盘数据
const loadWheelData = async () => {
  if (mode.value === 'shop') {
    // 尝试从后端获取真实店铺数据
    try {
      const res = await get('/shop/list', { page: 1, size: 8 })
      if (res.code === 200 && res.data && res.data.records && res.data.records.length > 0) {
        wheelItems.value = res.data.records.map((shop, index) => ({
          id: shop.id,
          name: shop.name,
          icon: shopIcons[index % shopIcons.length],
          rating: shop.rating || 4.5,
          deliveryTime: shop.deliveryTime || 25
        }))
        return
      }
    } catch (e) {
      console.log('获取店铺列表失败，使用模拟数据')
    }
    // 使用模拟数据（ID改为1和2，对应真实存在的店铺）
    wheelItems.value = [
      { id: 1, name: '美味快餐店', icon: '🍚', rating: 4.8, deliveryTime: 25 },
      { id: 2, name: '幸福面馆', icon: '🍜', rating: 4.6, deliveryTime: 20 },
      { id: 1, name: '麻辣香锅', icon: '🍲', rating: 4.9, deliveryTime: 35 },
      { id: 2, name: '沙县小吃', icon: '🥟', rating: 4.5, deliveryTime: 20 },
      { id: 1, name: '蜜雪冰城', icon: '🧋', rating: 4.8, deliveryTime: 15 },
      { id: 2, name: '肯德基', icon: '🍔', rating: 4.4, deliveryTime: 30 },
      { id: 1, name: '瑞幸咖啡', icon: '☕', rating: 4.6, deliveryTime: 20 },
      { id: 2, name: '必胜客', icon: '🍕', rating: 4.5, deliveryTime: 35 }
    ]
  } else if (mode.value === 'dish') {
    wheelItems.value = [
      { id: 1, name: '红烧牛肉面', icon: '🍜', price: 18 },
      { id: 2, name: '黄焖鸡米饭', icon: '🍚', price: 22 },
      { id: 1, name: '麻辣烫', icon: '🍲', price: 25 },
      { id: 2, name: '鸡排饭', icon: '🍗', price: 20 },
      { id: 1, name: '珍珠奶茶', icon: '🧋', price: 12 },
      { id: 2, name: '炸鸡腿堡', icon: '🍔', price: 28 },
      { id: 1, name: '拿铁咖啡', icon: '☕', price: 18 },
      { id: 2, name: '披萨套餐', icon: '🍕', price: 45 }
    ]
  } else {
    wheelItems.value = [
      { id: 1, name: '快餐', icon: '🍔' },
      { id: 2, name: '中餐', icon: '🍜' },
      { id: 1, name: '饮品', icon: '🧋' },
      { id: 2, name: '甜点', icon: '🍰' },
      { id: 1, name: '火锅', icon: '🍲' },
      { id: 2, name: '烧烤', icon: '🍖' },
      { id: 1, name: '日料', icon: '🍣' },
      { id: 2, name: '西餐', icon: '🥗' }
    ]
  }
}

// 转动转盘
const spin = () => {
  if (isSpinning.value) return
  
  isSpinning.value = true
  selectedItem.value = null
  showConfetti.value = false
  
  // 随机转动角度 (至少转5圈 + 随机角度)
  const randomIndex = Math.floor(Math.random() * wheelItems.value.length)
  const itemAngle = 360 / wheelItems.value.length
  const targetAngle = 360 * 5 + (360 - randomIndex * itemAngle - itemAngle / 2)
  
  rotation.value = rotation.value + targetAngle
  
  // 4秒后显示结果
  setTimeout(() => {
    isSpinning.value = false
    selectedItem.value = wheelItems.value[randomIndex]
    showConfetti.value = true
    spinCount.value++
    
    // 添加到历史记录
    const now = new Date()
    const timeStr = `${now.getHours()}:${String(now.getMinutes()).padStart(2, '0')}`
    history.value.unshift({
      ...selectedItem.value,
      time: timeStr
    })
    // 保留最近5条
    if (history.value.length > 5) {
      history.value = history.value.slice(0, 5)
    }
    
    uni.vibrateShort()
    uni.showToast({
      title: `🎉 选中了 ${selectedItem.value.name}！`,
      icon: 'none'
    })
  }, 4000)
}

// 切换模式
const switchMode = (newMode) => {
  if (isSpinning.value) return
  mode.value = newMode
  rotation.value = 0
  selectedItem.value = null
  loadWheelData()
}

// 去店铺
const goToShop = () => {
  if (selectedItem.value) {
    uni.navigateTo({ url: `/pages/shop/shop?id=${selectedItem.value.id}` })
  }
}

// 刷新挑战
const refreshChallenge = async () => {
  const challenges = [
    { name: '早餐达人', description: '连续7天在8点前下单', reward: '获得【早起鸟】徽章', icon: '🌅' },
    { name: '辣王挑战', description: '点3份特辣菜品', reward: '获得【辣王】徽章', icon: '🌶️' },
    { name: '拼单王', description: '成功发起3次拼单', reward: '优惠券5元', icon: '👥' },
    { name: '探店达人', description: '在5家不同店铺下单', reward: '获得【探店达人】徽章', icon: '🗺️' }
  ]
  challenge.value = challenges[Math.floor(Math.random() * challenges.length)]
}

const goBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  loadWheelData()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  padding-bottom: 60rpx;
  position: relative;
  overflow: hidden;
}

/* 星星装饰 */
.stars-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.stars-bg .star {
  position: absolute;
  background: #fff;
  border-radius: 50%;
  width: 4rpx;
  height: 4rpx;
  animation: twinkle 2s infinite;
}

.star-1 { left: 10%; top: 15%; animation-delay: 0s; }
.star-2 { left: 25%; top: 8%; animation-delay: 0.3s; }
.star-3 { left: 45%; top: 5%; animation-delay: 0.6s; }
.star-4 { left: 70%; top: 12%; animation-delay: 0.9s; }
.star-5 { left: 85%; top: 6%; animation-delay: 1.2s; }
.star-6 { left: 15%; top: 25%; animation-delay: 1.5s; }
.star-7 { left: 80%; top: 22%; animation-delay: 1.8s; }
.star-8 { left: 55%; top: 18%; animation-delay: 2.1s; }

@keyframes twinkle {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 1; }
}

/* 头部 */
.header {
  padding: 60rpx 40rpx 30rpx;
  text-align: center;
  position: relative;
  z-index: 1;
}

.title-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-bottom: 20rpx;
}

.back-btn {
  position: absolute;
  left: 0;
  font-size: 40rpx;
  color: #fff;
  padding: 10rpx;
}

.title {
  font-size: 44rpx;
  font-weight: bold;
  color: #fff;
}

.subtitle {
  font-size: 26rpx;
  color: rgba(255,255,255,0.6);
  display: block;
  margin-bottom: 24rpx;
}

/* 幸运指数 */
.lucky-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  background: rgba(255,255,255,0.1);
  border-radius: 40rpx;
  padding: 16rpx 28rpx;
  margin: 0 auto;
  width: fit-content;
}

.lucky-label {
  font-size: 24rpx;
  color: rgba(255,255,255,0.8);
}

.lucky-stars {
  display: flex;
  gap: 4rpx;
}

.star-on {
  color: #ffd700;
  font-size: 28rpx;
}

.star-off {
  color: rgba(255,255,255,0.3);
  font-size: 28rpx;
}

.lucky-hint {
  font-size: 22rpx;
  color: #ffd700;
  font-weight: bold;
}

/* 转盘容器 */
.wheel-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30rpx 0;
  position: relative;
  z-index: 1;
}

.wheel-wrapper {
  position: relative;
  width: 600rpx;
  height: 600rpx;
}

/* 转动次数 */
.spin-count {
  margin-top: 20rpx;
  font-size: 24rpx;
  color: rgba(255,255,255,0.5);
}

.wheel {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: conic-gradient(
    from 0deg,
    #ff6b6b 0deg 45deg,
    #feca57 45deg 90deg,
    #48dbfb 90deg 135deg,
    #ff9ff3 135deg 180deg,
    #54a0ff 180deg 225deg,
    #5f27cd 225deg 270deg,
    #00d2d3 270deg 315deg,
    #1dd1a1 315deg 360deg
  );
  box-shadow: 0 0 40rpx rgba(255, 107, 107, 0.5);
}

.wheel-item {
  position: absolute;
  width: 50%;
  height: 50%;
  left: 50%;
  top: 0;
  transform-origin: 0% 100%;
}

.item-content {
  position: absolute;
  top: 60rpx;
  left: -60rpx;
  width: 120rpx;
  text-align: center;
}

.item-icon {
  font-size: 40rpx;
  display: block;
  margin-bottom: 8rpx;
}

.item-name {
  font-size: 22rpx;
  color: #fff;
  font-weight: bold;
  text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.3);
  display: block;
  white-space: nowrap;
}

/* 中心按钮 */
.wheel-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 160rpx;
  height: 160rpx;
  background: linear-gradient(135deg, #ff6b6b, #feca57);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 30rpx rgba(255, 107, 107, 0.5);
  z-index: 10;
  transition: transform 0.2s;
}

.wheel-center:active {
  transform: translate(-50%, -50%) scale(0.95);
}

.wheel-center.spinning {
  animation: pulse 1s infinite alternate;
}

@keyframes pulse {
  from { box-shadow: 0 0 20rpx rgba(255, 107, 107, 0.5); }
  to { box-shadow: 0 0 50rpx rgba(255, 107, 107, 0.8), 0 0 20rpx #feca57; }
}

.center-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #fff;
}

/* 指针 */
.wheel-pointer {
  position: absolute;
  top: -20rpx;
  left: 50%;
  transform: translateX(-50%);
  font-size: 60rpx;
  color: #ff6b6b;
  text-shadow: 0 4rpx 8rpx rgba(0,0,0,0.3);
  z-index: 20;
}

/* 结果展示 */
.result-section {
  padding: 0 40rpx;
  margin-top: 20rpx;
  position: relative;
}

.result-card {
  background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
  border-radius: 24rpx;
  padding: 30rpx;
  border: 1rpx solid rgba(255,255,255,0.2);
  position: relative;
  overflow: hidden;
  animation: slideIn 0.5s ease-out;
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(20rpx); }
  to { opacity: 1; transform: translateY(0); }
}

.confetti {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  font-size: 60rpx;
  animation: pop 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

@keyframes pop {
  from { transform: scale(0) rotate(-45deg); }
  to { transform: scale(1) rotate(0); }
}

.result-header {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.result-icon {
  font-size: 60rpx;
  margin-right: 20rpx;
}

.result-info {
  flex: 1;
}

.result-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  display: block;
  margin-bottom: 8rpx;
}

.result-meta {
  display: flex;
  gap: 20rpx;
}

.rating, .delivery {
  font-size: 24rpx;
  color: rgba(255,255,255,0.7);
}

.result-actions {
  display: flex;
  gap: 20rpx;
}

.btn-go {
  flex: 2;
  background: linear-gradient(135deg, #ff6b6b, #feca57);
  color: #fff;
  font-size: 28rpx;
  font-weight: bold;
  padding: 20rpx 0;
  border-radius: 40rpx;
  border: none;
}

.btn-again {
  flex: 1;
  background: rgba(255,255,255,0.1);
  color: #fff;
  font-size: 28rpx;
  padding: 20rpx 0;
  border-radius: 40rpx;
  border: 1rpx solid rgba(255,255,255,0.3);
}

/* 历史记录 */
.history-section {
  padding: 0 40rpx;
  margin-top: 30rpx;
}

.history-list {
  white-space: nowrap;
  width: 100%;
}

.history-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  background: rgba(255,255,255,0.1);
  border-radius: 16rpx;
  padding: 16rpx;
  margin-right: 20rpx;
  width: 140rpx;
  border: 1rpx solid rgba(255,255,255,0.05);
}

.history-icon {
  font-size: 40rpx;
  margin-bottom: 8rpx;
}

.history-name {
  font-size: 22rpx;
  color: rgba(255,255,255,0.9);
  margin-bottom: 4rpx;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.history-time {
  font-size: 18rpx;
  color: rgba(255,255,255,0.5);
}

/* 模式切换 */
.mode-section {
  padding: 40rpx;
}

.section-title {
  font-size: 28rpx;
  color: rgba(255,255,255,0.6);
  margin-bottom: 20rpx;
  display: block;
}

.mode-tabs {
  display: flex;
  gap: 20rpx;
}

.mode-tab {
  flex: 1;
  background: rgba(255,255,255,0.1);
  border-radius: 16rpx;
  padding: 24rpx;
  text-align: center;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.mode-tab.active {
  background: rgba(255, 107, 107, 0.2);
  border-color: #ff6b6b;
}

.mode-icon {
  font-size: 40rpx;
  display: block;
  margin-bottom: 8rpx;
}

.mode-name {
  font-size: 24rpx;
  color: #fff;
}

/* 挑战卡片 */
.challenge-section {
  padding: 0 40rpx;
}

.challenge-card {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.2), rgba(254, 202, 87, 0.2));
  border-radius: 24rpx;
  padding: 30rpx;
  display: flex;
  align-items: center;
  border: 1rpx solid rgba(255,255,255,0.1);
  position: relative;
}

.challenge-icon {
  font-size: 60rpx;
  margin-right: 24rpx;
}

.challenge-info {
  flex: 1;
}

.challenge-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #fff;
  display: block;
  margin-bottom: 8rpx;
}

.challenge-desc {
  font-size: 24rpx;
  color: rgba(255,255,255,0.7);
  display: block;
  margin-bottom: 8rpx;
}

.challenge-reward {
  font-size: 24rpx;
  color: #feca57;
}

.refresh-btn {
  font-size: 32rpx;
  padding: 20rpx;
  opacity: 0.8;
}

.refresh-btn:active {
  transform: rotate(180deg);
  transition: transform 0.3s;
}
</style>
