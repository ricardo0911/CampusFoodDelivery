<template>
  <view class="page">
    <!-- 动态背景粒子 -->
    <view class="particle" v-for="i in 20" :key="i" :class="'p' + i"></view>
    
    <!-- 顶部状态栏 -->
    <view class="top-bar">
      <view class="energy-display">
        <text class="energy-icon">⚡</text>
        <view class="energy-bar">
          <view class="energy-fill" :style="{ width: energy + '%' }"></view>
        </view>
        <text class="energy-text">{{ energy }}%</text>
      </view>
      <view class="streak-badge" v-if="streak > 0">
        <text class="streak-fire">🔥</text>
        <text class="streak-num">{{ streak }}</text>
      </view>
    </view>

    <!-- 主标题区域 - 3D效果 -->
    <view class="hero-section">
      <view class="title-3d">
        <text class="title-shadow">美食命运</text>
        <text class="title-main">美食命运</text>
        <text class="title-glow">美食命运</text>
      </view>
      <text class="subtitle">✨ 让命运决定今天的味蕾冒险 ✨</text>
    </view>

    <!-- 命运之球 - 核心交互区 -->
    <view class="destiny-orb-container" @touchstart="startCharge" @touchend="release">
      <view class="orb-ring ring-outer" :class="{ charging: isCharging }"></view>
      <view class="orb-ring ring-middle" :class="{ charging: isCharging }"></view>
      <view class="orb-ring ring-inner" :class="{ charging: isCharging }"></view>
      
      <view class="destiny-orb" :class="{ charging: isCharging, released: isRolling }">
        <view class="orb-core">
          <view class="orb-face" v-if="!isRolling && !result">
            <text class="orb-emoji">🎱</text>
            <text class="orb-hint">按住蓄力</text>
          </view>
          <view class="orb-spinning" v-if="isRolling">
            <text class="spinning-text">{{ spinningItem }}</text>
          </view>
          <view class="orb-result" v-if="result && !isRolling">
            <text class="result-emoji">{{ result.emoji }}</text>
          </view>
        </view>
        
        <!-- 充能进度环 -->
        <svg class="charge-ring" viewBox="0 0 100 100" v-if="isCharging">
          <circle 
            class="charge-progress" 
            cx="50" cy="50" r="45" 
            :stroke-dasharray="283" 
            :stroke-dashoffset="283 - (chargeLevel / 100) * 283"
          />
        </svg>
      </view>
      
      <!-- 能量波动 -->
      <view class="energy-waves" v-if="isCharging">
        <view class="wave wave-1"></view>
        <view class="wave wave-2"></view>
        <view class="wave wave-3"></view>
      </view>
    </view>

    <!-- 结果展示卡片 - 独特设计 -->
    <view class="result-reveal" v-if="result && showResult" :class="{ show: showResult }">
      <view class="reveal-card">
        <view class="card-accent"></view>
        <view class="card-content">
          <view class="result-header">
            <text class="destiny-label">命运之选</text>
            <view class="rarity-badge" :class="result.rarity">
              {{ result.rarityText }}
            </view>
          </view>
          
          <image class="result-image" :src="result.image || '/static/food1.jpg'" mode="aspectFill" />
          
          <view class="result-info">
            <text class="result-name">{{ result.name }}</text>
            <text class="result-shop">@ {{ result.shopName }}</text>
            
            <view class="result-stats">
              <view class="stat-item">
                <text class="stat-icon">⭐</text>
                <text class="stat-value">{{ result.rating }}</text>
              </view>
              <view class="stat-item">
                <text class="stat-icon">🔥</text>
                <text class="stat-value">{{ result.calories }}卡</text>
              </view>
              <view class="stat-item">
                <text class="stat-icon">💰</text>
                <text class="stat-value">¥{{ result.price }}</text>
              </view>
            </view>
          </view>
          
          <view class="action-buttons">
            <button class="btn-destiny" @click="acceptDestiny">
              <text class="btn-icon">🎯</text>
              <text>接受命运</text>
            </button>
            <button class="btn-reroll" @click="resetOrb">
              <text class="btn-icon">🔄</text>
              <text>挑战命运</text>
            </button>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部模式选择器 - 创新设计 -->
    <view class="mode-dock" v-if="!showResult">
      <view class="dock-bg"></view>
      <view 
        class="dock-item" 
        v-for="(mode, index) in modes" 
        :key="mode.id"
        :class="{ active: currentMode === mode.id }"
        @click="selectMode(mode.id)"
      >
        <view class="dock-icon-wrap" :style="{ '--accent': mode.color }">
          <text class="dock-icon">{{ mode.icon }}</text>
        </view>
        <text class="dock-label">{{ mode.name }}</text>
        <view class="dock-indicator" v-if="currentMode === mode.id"></view>
      </view>
    </view>

    <!-- 历史轨迹 - 悬浮展示 -->
    <view class="history-orbit" v-if="history.length > 0 && !showResult">
      <text class="orbit-title">命运轨迹</text>
      <scroll-view class="orbit-scroll" scroll-x>
        <view class="orbit-item" v-for="(item, index) in history" :key="index">
          <view class="orbit-dot" :style="{ background: getRarityColor(item.rarity) }"></view>
          <text class="orbit-name">{{ item.name }}</text>
          <text class="orbit-time">{{ item.time }}</text>
        </view>
      </scroll-view>
    </view>

    <!-- 成就提示 -->
    <view class="achievement-popup" v-if="showAchievement" :class="{ show: showAchievement }">
      <view class="achievement-content">
        <text class="achievement-icon">🏆</text>
        <text class="achievement-title">{{ currentAchievement.title }}</text>
        <text class="achievement-desc">{{ currentAchievement.desc }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { get } from '@/utils/request'

// 状态管理
const energy = ref(100)
const streak = ref(0)
const isCharging = ref(false)
const chargeLevel = ref(0)
const isRolling = ref(false)
const result = ref(null)
const showResult = ref(false)
const spinningItem = ref('')
const history = ref([])
const currentMode = ref('shop')
const showAchievement = ref(false)
const currentAchievement = ref({})

// 模式配置
const modes = ref([
  { id: 'shop', name: '店铺', icon: '🏪', color: '#ff6b6b' },
  { id: 'dish', name: '菜品', icon: '🍜', color: '#4ecdc4' },
  { id: 'category', name: '品类', icon: '📦', color: '#ffe66d' },
  { id: 'surprise', name: '惊喜', icon: '🎁', color: '#a855f7' }
])

// 美食池数据
const foodPool = ref([
  { name: '黄焖鸡米饭', shopName: '老味道', rating: 4.8, calories: 520, price: 18, emoji: '🍗', rarity: 'common', rarityText: '普通' },
  { name: '兰州拉面', shopName: '正宗面馆', rating: 4.6, calories: 450, price: 15, emoji: '🍜', rarity: 'common', rarityText: '普通' },
  { name: '麻辣香锅', shopName: '川味轩', rating: 4.9, calories: 680, price: 35, emoji: '🌶️', rarity: 'rare', rarityText: '稀有' },
  { name: '日式寿司', shopName: '樱花屋', rating: 4.7, calories: 320, price: 48, emoji: '🍣', rarity: 'rare', rarityText: '稀有' },
  { name: '瑞幸咖啡', shopName: '瑞幸', rating: 4.5, calories: 120, price: 12, emoji: '☕', rarity: 'common', rarityText: '普通' },
  { name: '蜜雪冰城', shopName: '蜜雪冰城', rating: 4.8, calories: 180, price: 6, emoji: '🧋', rarity: 'common', rarityText: '普通' },
  { name: '和牛烧肉', shopName: '炙烤殿', rating: 5.0, calories: 750, price: 128, emoji: '🥩', rarity: 'legendary', rarityText: '传说' },
  { name: '龙虾大餐', shopName: '海鲜汇', rating: 4.9, calories: 420, price: 168, emoji: '🦞', rarity: 'legendary', rarityText: '传说' },
  { name: '宫保鸡丁', shopName: '川菜馆', rating: 4.6, calories: 380, price: 22, emoji: '🥢', rarity: 'common', rarityText: '普通' },
  { name: '沙县小吃', shopName: '沙县', rating: 4.4, calories: 350, price: 12, emoji: '🥟', rarity: 'common', rarityText: '普通' },
  { name: '必胜客披萨', shopName: '必胜客', rating: 4.5, calories: 580, price: 89, emoji: '🍕', rarity: 'rare', rarityText: '稀有' },
  { name: '星巴克', shopName: '星巴克', rating: 4.7, calories: 150, price: 38, emoji: '☕', rarity: 'rare', rarityText: '稀有' }
])

// 计时器
let chargeTimer = null
let spinTimer = null

// 开始蓄力
const startCharge = () => {
  if (isRolling.value || showResult.value) return
  
  isCharging.value = true
  chargeLevel.value = 0
  
  chargeTimer = setInterval(() => {
    if (chargeLevel.value < 100) {
      chargeLevel.value += 2
    }
  }, 30)
  
  // 震动反馈（如果支持）
  uni.vibrateShort && uni.vibrateShort()
}

// 释放启动
const release = () => {
  if (!isCharging.value) return
  
  clearInterval(chargeTimer)
  isCharging.value = false
  
  // 根据蓄力程度决定稀有度权重
  const chargeBonus = chargeLevel.value / 100
  startRolling(chargeBonus)
}

// 开始滚动
const startRolling = (chargeBonus) => {
  isRolling.value = true
  energy.value = Math.max(0, energy.value - 10)
  
  const spinDuration = 2000 + chargeBonus * 1000
  const spinInterval = 80
  let elapsed = 0
  
  spinTimer = setInterval(() => {
    elapsed += spinInterval
    
    // 随机显示食物
    const randomIndex = Math.floor(Math.random() * foodPool.value.length)
    spinningItem.value = foodPool.value[randomIndex].emoji + ' ' + foodPool.value[randomIndex].name
    
    if (elapsed >= spinDuration) {
      clearInterval(spinTimer)
      showFinalResult(chargeBonus)
    }
  }, spinInterval)
}

// 显示最终结果
const showFinalResult = (chargeBonus) => {
  isRolling.value = false
  
  // 根据蓄力程度增加稀有度概率
  let roll = Math.random() + chargeBonus * 0.3
  let selectedPool = []
  
  if (roll > 0.95) {
    selectedPool = foodPool.value.filter(f => f.rarity === 'legendary')
  } else if (roll > 0.7) {
    selectedPool = foodPool.value.filter(f => f.rarity === 'rare')
  } else {
    selectedPool = foodPool.value.filter(f => f.rarity === 'common')
  }
  
  if (selectedPool.length === 0) {
    selectedPool = foodPool.value
  }
  
  const selected = selectedPool[Math.floor(Math.random() * selectedPool.length)]
  result.value = { ...selected }
  
  setTimeout(() => {
    showResult.value = true
    
    // 添加到历史
    history.value.unshift({
      ...selected,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
    
    // 保持最近10条
    if (history.value.length > 10) {
      history.value = history.value.slice(0, 10)
    }
    
    // 连胜检测
    streak.value++
    
    // 成就检测
    checkAchievements()
    
    // 震动反馈
    uni.vibrateShort && uni.vibrateShort()
  }, 300)
}

// 检测成就
const checkAchievements = () => {
  if (streak.value === 3) {
    triggerAchievement({ title: '命运三连', desc: '连续抽取3次！' })
  } else if (streak.value === 5) {
    triggerAchievement({ title: '命运大师', desc: '连续抽取5次！' })
  } else if (result.value.rarity === 'legendary') {
    triggerAchievement({ title: '传说降临', desc: '获得传说级美食！' })
  }
}

// 触发成就
const triggerAchievement = (achievement) => {
  currentAchievement.value = achievement
  showAchievement.value = true
  
  setTimeout(() => {
    showAchievement.value = false
  }, 3000)
}

// 接受命运
const acceptDestiny = () => {
  uni.navigateTo({ url: `/pages/shop/shop?id=1` })
}

// 重置
const resetOrb = () => {
  result.value = null
  showResult.value = false
  chargeLevel.value = 0
  
  // 恢复能量
  setTimeout(() => {
    if (energy.value < 100) {
      energy.value = Math.min(100, energy.value + 5)
    }
  }, 1000)
}

// 选择模式
const selectMode = (modeId) => {
  currentMode.value = modeId
  uni.showToast({
    title: `切换到${modes.value.find(m => m.id === modeId).name}模式`,
    icon: 'none'
  })
}

// 获取稀有度颜色
const getRarityColor = (rarity) => {
  const colors = {
    common: '#9ca3af',
    rare: '#3b82f6',
    legendary: '#f59e0b'
  }
  return colors[rarity] || colors.common
}

// 加载真实数据
const loadRealData = async () => {
  try {
    const res = await get('/public/shop/list', { limit: 20 })
    if (res.data && res.data.records && res.data.records.length > 0) {
      // 将真实数据混入美食池
      res.data.records.forEach((shop, index) => {
        if (index < 5) {
          foodPool.value.push({
            name: shop.name,
            shopName: shop.name,
            rating: shop.rating || 4.5,
            calories: 400 + Math.floor(Math.random() * 300),
            price: shop.avgPrice || 25,
            emoji: ['🍔', '🍜', '🍕', '🥗', '🍱'][index % 5],
            rarity: index < 2 ? 'rare' : 'common',
            rarityText: index < 2 ? '稀有' : '普通',
            shopId: shop.id
          })
        }
      })
    }
  } catch (e) {
    console.log('使用本地数据')
  }
}

onMounted(() => {
  loadRealData()
  
  // 能量恢复定时器
  setInterval(() => {
    if (energy.value < 100 && !isCharging.value && !isRolling.value) {
      energy.value = Math.min(100, energy.value + 1)
    }
  }, 3000)
})
</script>

<style scoped>
/* 页面基础 - 深邃宇宙主题 */
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  position: relative;
  overflow: hidden;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 动态粒子背景 */
.particle {
  position: absolute;
  width: 4rpx;
  height: 4rpx;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: float 15s infinite ease-in-out;
}

@keyframes float {
  0%, 100% { transform: translateY(0) translateX(0); opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { transform: translateY(-100vh) translateX(50rpx); opacity: 0; }
}

.p1 { left: 5%; animation-delay: 0s; }
.p2 { left: 10%; animation-delay: 1s; }
.p3 { left: 15%; animation-delay: 2s; }
.p4 { left: 20%; animation-delay: 3s; }
.p5 { left: 25%; animation-delay: 4s; }
.p6 { left: 30%; animation-delay: 5s; }
.p7 { left: 35%; animation-delay: 6s; }
.p8 { left: 40%; animation-delay: 7s; }
.p9 { left: 45%; animation-delay: 8s; }
.p10 { left: 50%; animation-delay: 9s; }
.p11 { left: 55%; animation-delay: 0.5s; }
.p12 { left: 60%; animation-delay: 1.5s; }
.p13 { left: 65%; animation-delay: 2.5s; }
.p14 { left: 70%; animation-delay: 3.5s; }
.p15 { left: 75%; animation-delay: 4.5s; }
.p16 { left: 80%; animation-delay: 5.5s; }
.p17 { left: 85%; animation-delay: 6.5s; }
.p18 { left: 90%; animation-delay: 7.5s; }
.p19 { left: 95%; animation-delay: 8.5s; }
.p20 { left: 98%; animation-delay: 9.5s; }

/* 顶部状态栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 60rpx 40rpx 30rpx;
  padding-top: calc(var(--status-bar-height) + 30rpx);
}

.energy-display {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.energy-icon {
  font-size: 36rpx;
}

.energy-bar {
  width: 160rpx;
  height: 16rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10rpx;
  overflow: hidden;
}

.energy-fill {
  height: 100%;
  background: linear-gradient(90deg, #00ff87, #60efff);
  border-radius: 10rpx;
  transition: width 0.3s ease;
}

.energy-text {
  font-size: 24rpx;
  color: #00ff87;
  font-weight: bold;
}

.streak-badge {
  display: flex;
  align-items: center;
  background: rgba(255, 107, 107, 0.3);
  padding: 10rpx 20rpx;
  border-radius: 30rpx;
  border: 2rpx solid rgba(255, 107, 107, 0.5);
}

.streak-fire {
  font-size: 28rpx;
}

.streak-num {
  font-size: 28rpx;
  color: #ff6b6b;
  font-weight: bold;
  margin-left: 8rpx;
}

/* 主标题 - 3D效果 */
.hero-section {
  text-align: center;
  padding: 20rpx 0 40rpx;
}

.title-3d {
  position: relative;
  display: inline-block;
}

.title-shadow, .title-main, .title-glow {
  font-size: 72rpx;
  font-weight: 900;
  letter-spacing: 8rpx;
}

.title-shadow {
  position: absolute;
  color: #000;
  transform: translate(6rpx, 6rpx);
  opacity: 0.3;
}

.title-main {
  position: relative;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.title-glow {
  position: absolute;
  left: 0;
  color: transparent;
  text-shadow: 0 0 40rpx rgba(245, 87, 108, 0.5);
}

.subtitle {
  display: block;
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 16rpx;
}

/* 命运之球容器 */
.destiny-orb-container {
  position: relative;
  width: 100%;
  height: 500rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 20rpx 0;
}

/* 轨道环 */
.orb-ring {
  position: absolute;
  border-radius: 50%;
  border: 2rpx solid rgba(255, 255, 255, 0.1);
  animation: orbit 10s linear infinite;
}

.ring-outer {
  width: 480rpx;
  height: 480rpx;
  animation-duration: 20s;
}

.ring-middle {
  width: 400rpx;
  height: 400rpx;
  animation-duration: 15s;
  animation-direction: reverse;
}

.ring-inner {
  width: 320rpx;
  height: 320rpx;
  animation-duration: 10s;
}

.orb-ring.charging {
  border-color: rgba(162, 89, 255, 0.5);
  box-shadow: 0 0 30rpx rgba(162, 89, 255, 0.3);
}

@keyframes orbit {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 命运之球 */
.destiny-orb {
  position: relative;
  width: 260rpx;
  height: 260rpx;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #667eea, #764ba2, #302b63);
  box-shadow: 
    0 0 60rpx rgba(102, 126, 234, 0.5),
    inset 0 0 60rpx rgba(0, 0, 0, 0.5),
    0 20rpx 60rpx rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.destiny-orb.charging {
  transform: scale(1.1);
  box-shadow: 
    0 0 100rpx rgba(162, 89, 255, 0.7),
    inset 0 0 60rpx rgba(0, 0, 0, 0.5),
    0 20rpx 60rpx rgba(0, 0, 0, 0.4);
}

.destiny-orb.released {
  animation: shake 0.1s linear infinite;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-10rpx); }
  75% { transform: translateX(10rpx); }
}

.orb-core {
  text-align: center;
}

.orb-face .orb-emoji {
  font-size: 80rpx;
  display: block;
}

.orb-face .orb-hint {
  font-size: 22rpx;
  color: rgba(255, 255, 255, 0.8);
  margin-top: 8rpx;
}

.orb-spinning .spinning-text {
  font-size: 28rpx;
  color: #fff;
  animation: flash 0.1s linear infinite;
}

@keyframes flash {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.orb-result .result-emoji {
  font-size: 100rpx;
  animation: pop 0.5s ease;
}

@keyframes pop {
  0% { transform: scale(0); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

/* 充能环 */
.charge-ring {
  position: absolute;
  width: 300rpx;
  height: 300rpx;
  transform: rotate(-90deg);
}

.charge-progress {
  fill: none;
  stroke: #a855f7;
  stroke-width: 4;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.1s ease;
}

/* 能量波动 */
.energy-waves {
  position: absolute;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.wave {
  position: absolute;
  border: 2rpx solid rgba(162, 89, 255, 0.5);
  border-radius: 50%;
  animation: wave-expand 1.5s ease-out infinite;
}

.wave-1 { width: 280rpx; height: 280rpx; animation-delay: 0s; }
.wave-2 { width: 280rpx; height: 280rpx; animation-delay: 0.5s; }
.wave-3 { width: 280rpx; height: 280rpx; animation-delay: 1s; }

@keyframes wave-expand {
  0% { transform: scale(1); opacity: 1; }
  100% { transform: scale(2); opacity: 0; }
}

/* 结果展示卡片 */
.result-reveal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  opacity: 0;
  transform: scale(0.9);
  transition: all 0.3s ease;
  pointer-events: none;
}

.result-reveal.show {
  opacity: 1;
  transform: scale(1);
  pointer-events: auto;
}

.reveal-card {
  width: 85%;
  max-width: 620rpx;
  background: linear-gradient(145deg, #1e1e2f, #2d2d44);
  border-radius: 40rpx;
  overflow: hidden;
  position: relative;
  box-shadow: 0 40rpx 100rpx rgba(0, 0, 0, 0.5);
}

.card-accent {
  height: 8rpx;
  background: linear-gradient(90deg, #f093fb, #f5576c, #feca57);
}

.card-content {
  padding: 40rpx;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.destiny-label {
  font-size: 28rpx;
  color: rgba(255, 255, 255, 0.6);
  text-transform: uppercase;
  letter-spacing: 4rpx;
}

.rarity-badge {
  padding: 8rpx 20rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
}

.rarity-badge.common {
  background: rgba(156, 163, 175, 0.3);
  color: #9ca3af;
}

.rarity-badge.rare {
  background: rgba(59, 130, 246, 0.3);
  color: #60a5fa;
}

.rarity-badge.legendary {
  background: linear-gradient(135deg, rgba(245, 158, 11, 0.3), rgba(234, 88, 12, 0.3));
  color: #fbbf24;
  animation: glow 2s ease-in-out infinite;
}

@keyframes glow {
  0%, 100% { box-shadow: 0 0 10rpx rgba(245, 158, 11, 0.5); }
  50% { box-shadow: 0 0 30rpx rgba(245, 158, 11, 0.8); }
}

.result-image {
  width: 100%;
  height: 320rpx;
  border-radius: 24rpx;
  object-fit: cover;
}

.result-info {
  padding: 30rpx 0;
}

.result-name {
  font-size: 44rpx;
  font-weight: 800;
  color: #fff;
  display: block;
}

.result-shop {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 8rpx;
  display: block;
}

.result-stats {
  display: flex;
  gap: 30rpx;
  margin-top: 24rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.stat-icon {
  font-size: 28rpx;
}

.stat-value {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.8);
}

.action-buttons {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}

.btn-destiny, .btn-reroll {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  padding: 28rpx 0;
  border-radius: 20rpx;
  font-size: 28rpx;
  font-weight: bold;
  border: none;
}

.btn-destiny {
  background: linear-gradient(135deg, #f093fb, #f5576c);
  color: #fff;
}

.btn-reroll {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
  border: 2rpx solid rgba(255, 255, 255, 0.2);
}

.btn-icon {
  font-size: 32rpx;
}

/* 底部模式选择器 */
.mode-dock {
  position: fixed;
  bottom: 60rpx;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 30rpx;
  padding: 20rpx 40rpx;
  background: rgba(30, 30, 47, 0.9);
  border-radius: 50rpx;
  backdrop-filter: blur(20rpx);
  box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.3);
}

.dock-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  position: relative;
  transition: transform 0.2s ease;
}

.dock-item.active {
  transform: translateY(-10rpx);
}

.dock-icon-wrap {
  width: 80rpx;
  height: 80rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.dock-item.active .dock-icon-wrap {
  background: var(--accent);
  box-shadow: 0 8rpx 24rpx var(--accent);
}

.dock-icon {
  font-size: 36rpx;
}

.dock-label {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.6);
}

.dock-item.active .dock-label {
  color: #fff;
}

.dock-indicator {
  position: absolute;
  bottom: -16rpx;
  width: 8rpx;
  height: 8rpx;
  background: var(--accent);
  border-radius: 50%;
}

/* 历史轨迹 */
.history-orbit {
  position: absolute;
  bottom: 200rpx;
  left: 0;
  right: 0;
  padding: 0 30rpx;
}

.orbit-title {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.5);
  margin-bottom: 16rpx;
  padding-left: 10rpx;
}

.orbit-scroll {
  white-space: nowrap;
}

.orbit-item {
  display: inline-flex;
  align-items: center;
  gap: 12rpx;
  background: rgba(255, 255, 255, 0.05);
  padding: 16rpx 24rpx;
  border-radius: 30rpx;
  margin-right: 16rpx;
}

.orbit-dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
}

.orbit-name {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.orbit-time {
  font-size: 20rpx;
  color: rgba(255, 255, 255, 0.4);
}

/* 成就弹窗 */
.achievement-popup {
  position: fixed;
  top: 150rpx;
  left: 50%;
  transform: translateX(-50%) translateY(-50rpx);
  opacity: 0;
  transition: all 0.5s ease;
  z-index: 200;
  pointer-events: none;
}

.achievement-popup.show {
  transform: translateX(-50%) translateY(0);
  opacity: 1;
}

.achievement-content {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: linear-gradient(135deg, #fbbf24, #f59e0b);
  padding: 20rpx 40rpx;
  border-radius: 50rpx;
  box-shadow: 0 10rpx 40rpx rgba(245, 158, 11, 0.4);
}

.achievement-icon {
  font-size: 40rpx;
}

.achievement-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #1e1e2f;
}

.achievement-desc {
  font-size: 22rpx;
  color: rgba(30, 30, 47, 0.7);
}
</style>
