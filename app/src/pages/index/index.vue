<template>
  <view class="page">
    <!-- 顶部渐变头部 -->
    <view class="header">
      <view class="location-bar" @click="chooseLocation">
        <view class="location-icon">
          <text class="location-symbol">◤</text>
        </view>
        <text class="location-text">{{ locationName }}</text>
        <view class="arrow-down"></view>
      </view>
      
      <!-- 搜索栏 -->
      <view class="search-bar">
        <view class="search-icon-wrap">
          <view class="search-icon">
            <view class="search-circle"></view>
            <view class="search-line"></view>
          </view>
        </view>
        <input class="search-input" placeholder="搜索店铺或菜品" v-model="keyword" @confirm="search" />
      </view>
      
      <!-- 分类导航 -->
      <scroll-view class="category-nav" scroll-x>
        <view 
          class="category-item" 
          :class="{ active: activeCategory === '' }" 
          @click="selectCategory('')"
        >
          <view class="category-icon" :class="{ active: activeCategory === '' }">
            <text class="category-symbol">全部</text>
          </view>
        </view>
        <view 
          class="category-item" 
          :class="{ active: activeCategory === '快餐' }" 
          @click="selectCategory('快餐')"
        >
          <view class="category-icon" :class="{ active: activeCategory === '快餐' }">
            <text class="category-symbol">快餐</text>
          </view>
        </view>
        <view 
          class="category-item" 
          :class="{ active: activeCategory === '中餐' }" 
          @click="selectCategory('中餐')"
        >
          <view class="category-icon" :class="{ active: activeCategory === '中餐' }">
            <text class="category-symbol">中餐</text>
          </view>
        </view>
        <view 
          class="category-item" 
          :class="{ active: activeCategory === '饮品' }" 
          @click="selectCategory('饮品')"
        >
          <view class="category-icon" :class="{ active: activeCategory === '饮品' }">
            <text class="category-symbol">饮品</text>
          </view>
        </view>
        <view 
          class="category-item" 
          :class="{ active: activeCategory === '甜点' }" 
          @click="selectCategory('甜点')"
        >
          <view class="category-icon" :class="{ active: activeCategory === '甜点' }">
            <text class="category-symbol">甜点</text>
          </view>
        </view>
        <view 
          class="category-item" 
          :class="{ active: activeCategory === '小吃' }" 
          @click="selectCategory('小吃')"
        >
          <view class="category-icon" :class="{ active: activeCategory === '小吃' }">
            <text class="category-symbol">小吃</text>
          </view>
        </view>
      </scroll-view>
    </view>
    
    <!-- 快捷功能入口 -->
    <view class="quick-actions">
      <view class="action-card random-card" @click="goToRandomPick">
        <view class="action-icon-bg">
          <view class="dice-icon">
            <view class="dice-face">
              <view class="dot dot-1"></view>
              <view class="dot dot-2"></view>
            </view>
          </view>
        </view>
        <view class="action-info">
          <text class="action-title">今天吃什么</text>
          <text class="action-desc">转盘随机选</text>
        </view>
        <view class="action-arrow"></view>
      </view>
      <view class="action-card group-card" @click="goToGroupOrder">
        <view class="action-icon-bg">
          <view class="group-icon">
            <view class="person person-1"></view>
            <view class="person person-2"></view>
            <view class="person person-3"></view>
          </view>
        </view>
        <view class="action-info">
          <text class="action-title">宿舍拼单</text>
          <text class="action-desc">一起更优惠</text>
        </view>
        <view class="action-arrow"></view>
      </view>
    </view>

    <!-- 特色功能入口 -->
    <view class="feature-grid">
      <view class="feature-item" @click="goToFlashSale">
        <view class="feature-icon flash-sale">
          <text class="icon-text">秒</text>
        </view>
        <text class="feature-name">限时秒杀</text>
      </view>
      <view class="feature-item" @click="goToRanking">
        <view class="feature-icon ranking">
          <text class="icon-text">榜</text>
        </view>
        <text class="feature-name">美食排行</text>
      </view>
      <view class="feature-item" @click="goToMysteryBox">
        <view class="feature-icon mystery-box">
          <text class="icon-text">盲</text>
        </view>
        <text class="feature-name">美食盲盒</text>
      </view>
      <view class="feature-item" @click="goToCommunity">
        <view class="feature-icon community">
          <text class="icon-text">社</text>
        </view>
        <text class="feature-name">美食社区</text>
      </view>
    </view>
    
    <!-- 筛选栏 -->
    <view class="filter-bar">
      <view class="filter-item active">
        <text>综合排序</text>
        <view class="filter-arrow"></view>
      </view>
      <view class="filter-item">
        <text>销量最高</text>
      </view>
      <view class="filter-item">
        <text>距离最近</text>
      </view>
      <view class="filter-item">
        <text>筛选</text>
        <view class="filter-dots">
          <view class="dot-item"></view>
          <view class="dot-item"></view>
        </view>
      </view>
    </view>
    
    <!-- 店铺列表 -->
    <scroll-view class="shop-list" scroll-y>
      <!-- 推荐标签 -->
      <view class="section-title">
        <view class="title-icon"></view>
        <text class="title-text">附近商家</text>
        <text class="title-sub">为您精选优质店铺</text>
      </view>
      
      <!-- 骨架屏加载状态 -->
      <view v-if="isLoading" class="skeleton-list">
        <view v-for="n in 5" :key="n" class="skeleton-item">
          <CSkeleton type="image" style="width: 160rpx; height: 160rpx; border-radius: 16rpx;" />
          <view class="skeleton-content">
            <CSkeleton type="title" style="width: 60%;" />
            <CSkeleton type="text" :rows="3" />
          </view>
        </view>
      </view>
      
      <!-- 商家列表 -->
      <view v-else class="merchant-list">
        <CMerchantCard
          v-for="shop in shopList"
          :key="shop.id"
          :id="shop.id"
          :name="shop.name"
          :image="getShopCover(shop)"
          :rating="shop.rating || 4.5"
          :monthly-sales="shop.monthlySales || 0"
          :delivery-time="shop.deliveryTime || 30"
          :min-order-amount="shop.minOrderAmount || 15"
          :delivery-fee="shop.deliveryFee || 3"
          :is-brand="shop.rating >= 4.5"
          :is-new="shop.isNew"
          :activities="getShopActivities(shop)"
          @click="goShop"
        />
      </view>
      
      <!-- 空状态 -->
      <CEmpty
        v-if="!isLoading && shopList.length === 0"
        title="暂无营业中的店铺"
        description="换个时间再来看看吧"
      >
        <template #action>
          <CButton type="primary" size="sm" @click="refreshShops">刷新试试</CButton>
        </template>
      </CEmpty>
      
      <!-- 底部安全区 -->
      <view class="safe-bottom"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '@/utils/request'
import { CMerchantCard, CTag, CEmpty, CSkeleton, CButton } from '@/components/common'

const keyword = ref('')
const shopList = ref([])
const activeCategory = ref('')
const locationName = ref('获取位置中...')
const latitude = ref(0)
const longitude = ref(0)
const isLoading = ref(false)

// 格式化销量显示
const formatSales = (sales) => {
  if (!sales) return '999+'
  if (sales >= 10000) {
    return (sales / 10000).toFixed(1) + '万'
  }
  return sales + '+'
}

// 根据经纬度判断城市
const getCityFromCoords = (lat, lng) => {
  if (lat > 22 && lat < 25 && lng > 112 && lng < 115) return '广州市'
  if (lat > 39 && lat < 41 && lng > 115 && lng < 118) return '北京市'
  if (lat > 30 && lat < 32 && lng > 120 && lng < 123) return '上海市'
  if (lat > 22 && lat < 23 && lng > 113 && lng < 115) return '深圳市'
  if (lat > 36 && lat < 38 && lng > 116 && lng < 118) return '济南市'
  if (lat > 29 && lat < 31 && lng > 119 && lng < 121) return '杭州市'
  if (lat > 31 && lat < 33 && lng > 117 && lng < 119) return '南京市'
  if (lat > 22 && lat < 24 && lng > 107 && lng < 109) return '南宁市'
  if (lat > 28 && lat < 30 && lng > 112 && lng < 114) return '长沙市'
  if (lat > 29 && lat < 31 && lng > 103 && lng < 105) return '成都市'
  if (lat > 29 && lat < 30 && lng > 105 && lng < 107) return '重庆市'
  if (lat > 33 && lat < 35 && lng > 108 && lng < 110) return '西安市'
  if (lat > 22 && lat < 24 && lng > 113 && lng < 114) return '东莞市'
  return '当前位置'
}

// 自动获取位置
const getLocation = () => {
  locationName.value = '定位中...'
  uni.getLocation({
    type: 'gcj02',
    success: (res) => {
      latitude.value = res.latitude
      longitude.value = res.longitude
      locationName.value = getCityFromCoords(res.latitude, res.longitude)
    },
    fail: (err) => {
      console.log('定位失败:', err)
      locationName.value = '点击重新定位'
    }
  })
}

// 点击位置栏时重新定位
const chooseLocation = () => {
  getLocation()
}

const loadShops = async () => {
  isLoading.value = true
  
  if (activeCategory.value) {
    loadMockShops()
    isLoading.value = false
    return
  }

  try {
    const params = { keyword: keyword.value }
    const res = await get('/public/shop/list', params)
    
    let validRecords = []
    if (res.data && res.data.records) {
      validRecords = res.data.records
    }

    if (validRecords.length > 0) {
      if (activeCategory.value && validRecords.length < 2) {
        loadMockShops()
      } else {
        shopList.value = validRecords
      }
    } else {
      loadMockShops()
    }
  } catch (e) { 
    loadMockShops()
  } finally {
    setTimeout(() => {
      isLoading.value = false
    }, 500)
  }
}

// 模拟商家数据
const loadMockShops = () => {
  const city = locationName.value
  const baseShops = [
    { id: 1, name: '黄焖鸡米饭', logo: '/static/shop1.jpg', rating: 4.8, monthlySales: 1234, minOrderAmount: 15, deliveryFee: 3, deliveryTime: 25, hasPromo: true, category: '快餐' },
    { id: 2, name: '兰州拉面馆', logo: '/static/shop2.jpg', rating: 4.6, monthlySales: 856, minOrderAmount: 12, deliveryFee: 0, deliveryTime: 20, isNew: true, category: '快餐' },
    { id: 3, name: '麻辣香锅', logo: '/static/shop3.jpg', rating: 4.9, monthlySales: 2341, minOrderAmount: 25, deliveryFee: 4, deliveryTime: 35, hasPromo: true, category: '中餐' },
    { id: 4, name: '沙县小吃', logo: '/static/shop1.jpg', rating: 4.5, monthlySales: 3200, minOrderAmount: 10, deliveryFee: 2, deliveryTime: 20, hasPromo: true, category: '快餐' },
    { id: 5, name: '杨国福麻辣烫', logo: '/static/shop2.jpg', rating: 4.7, monthlySales: 1890, minOrderAmount: 18, deliveryFee: 3, deliveryTime: 25, category: '快餐' },
    { id: 6, name: '肯德基', logo: '/static/shop3.jpg', rating: 4.4, monthlySales: 5620, minOrderAmount: 20, deliveryFee: 5, deliveryTime: 30, isNew: true, category: '快餐' },
    { id: 7, name: '蜜雪冰城', logo: '/static/shop1.jpg', rating: 4.8, monthlySales: 8900, minOrderAmount: 8, deliveryFee: 0, deliveryTime: 15, hasPromo: true, category: '饮品' },
    { id: 8, name: '瑞幸咖啡', logo: '/static/shop2.jpg', rating: 4.6, monthlySales: 4560, minOrderAmount: 15, deliveryFee: 2, deliveryTime: 20, category: '饮品' },
    { id: 13, name: '东北饺子馆', logo: '/static/shop1.jpg', rating: 4.7, monthlySales: 1200, minOrderAmount: 20, deliveryFee: 3, deliveryTime: 30, category: '中餐' },
    { id: 14, name: '川菜馆', logo: '/static/shop2.jpg', rating: 4.8, monthlySales: 1500, minOrderAmount: 30, deliveryFee: 4, deliveryTime: 40, hasPromo: true, category: '中餐' },
    { id: 15, name: '必胜客', logo: '/static/shop3.jpg', rating: 4.5, monthlySales: 2200, minOrderAmount: 0, deliveryFee: 9, deliveryTime: 35, category: '快餐' },
    { id: 16, name: '星巴克', logo: '/static/shop1.jpg', rating: 4.9, monthlySales: 3100, minOrderAmount: 25, deliveryFee: 0, deliveryTime: 25, category: '饮品' },
    { id: 17, name: '满记甜品', logo: '/static/shop2.jpg', rating: 4.7, monthlySales: 900, minOrderAmount: 25, deliveryFee: 3, deliveryTime: 35, category: '甜点' },
    { id: 18, name: '鲜芋仙', logo: '/static/shop3.jpg', rating: 4.6, monthlySales: 1100, minOrderAmount: 20, deliveryFee: 3, deliveryTime: 30, category: '甜点' },
    { id: 19, name: '好利来', logo: '/static/shop1.jpg', rating: 4.8, monthlySales: 2500, minOrderAmount: 30, deliveryFee: 0, deliveryTime: 40, hasPromo: true, category: '甜点' },
    { id: 20, name: '一点点', logo: '/static/shop2.jpg', rating: 4.5, monthlySales: 4200, minOrderAmount: 15, deliveryFee: 0, deliveryTime: 20, category: '饮品' },
  ]
  
  // 根据城市添加特色店铺
  if (city.includes('广州') || city.includes('深圳') || city.includes('东莞')) {
    baseShops.push({ id: 9, name: '广式早茶', logo: '/static/shop3.jpg', rating: 4.9, monthlySales: 2100, minOrderAmount: 30, deliveryFee: 5, deliveryTime: 35, hasPromo: true, category: '中餐' })
    baseShops.push({ id: 10, name: '潮汕牛肉火锅', logo: '/static/shop1.jpg', rating: 4.8, monthlySales: 1560, minOrderAmount: 50, deliveryFee: 6, deliveryTime: 40, category: '中餐' })
    baseShops.push({ id: 11, name: '喜茶', logo: '/static/shop2.jpg', rating: 4.9, monthlySales: 3200, minOrderAmount: 20, deliveryFee: 0, deliveryTime: 25, category: '饮品' })
    baseShops.push({ id: 12, name: '许留山', logo: '/static/shop3.jpg', rating: 4.7, monthlySales: 1500, minOrderAmount: 25, deliveryFee: 3, deliveryTime: 30, category: '甜点' })
  } else if (city.includes('北京')) {
    baseShops.push({ id: 9, name: '庆丰包子铺', logo: '/static/shop3.jpg', rating: 4.7, monthlySales: 3400, minOrderAmount: 12, deliveryFee: 2, deliveryTime: 20, category: '快餐' })
    baseShops.push({ id: 10, name: '北京烤鸭', logo: '/static/shop1.jpg', rating: 4.9, monthlySales: 890, minOrderAmount: 80, deliveryFee: 8, deliveryTime: 45, hasPromo: true, category: '中餐' })
    baseShops.push({ id: 11, name: '稻香村', logo: '/static/shop2.jpg', rating: 4.8, monthlySales: 2800, minOrderAmount: 30, deliveryFee: 5, deliveryTime: 40, category: '甜点' })
  } else if (city.includes('上海')) {
    baseShops.push({ id: 9, name: '小杨生煎', logo: '/static/shop3.jpg', rating: 4.8, monthlySales: 4200, minOrderAmount: 15, deliveryFee: 3, deliveryTime: 25, category: '快餐' })
    baseShops.push({ id: 10, name: '南翔小笼包', logo: '/static/shop1.jpg', rating: 4.7, monthlySales: 2800, minOrderAmount: 20, deliveryFee: 3, deliveryTime: 25, isNew: true, category: '中餐' })
    baseShops.push({ id: 11, name: '红宝石蛋糕', logo: '/static/shop2.jpg', rating: 4.9, monthlySales: 1900, minOrderAmount: 25, deliveryFee: 4, deliveryTime: 30, category: '甜点' })
  } else if (city.includes('成都') || city.includes('重庆')) {
    baseShops.push({ id: 9, name: '老火锅', logo: '/static/shop3.jpg', rating: 4.9, monthlySales: 3600, minOrderAmount: 40, deliveryFee: 5, deliveryTime: 35, hasPromo: true, category: '中餐' })
    baseShops.push({ id: 10, name: '串串香', logo: '/static/shop1.jpg', rating: 4.7, monthlySales: 2400, minOrderAmount: 25, deliveryFee: 4, deliveryTime: 30, category: '中餐' })
    baseShops.push({ id: 11, name: '茶百道', logo: '/static/shop2.jpg', rating: 4.8, monthlySales: 5600, minOrderAmount: 15, deliveryFee: 2, deliveryTime: 20, category: '饮品' })
  } else if (city.includes('济南')) {
    baseShops.push({ id: 9, name: '把子肉', logo: '/static/shop3.jpg', rating: 4.6, monthlySales: 1800, minOrderAmount: 15, deliveryFee: 2, deliveryTime: 25, category: '中餐' })
    baseShops.push({ id: 10, name: '油旋', logo: '/static/shop1.jpg', rating: 4.5, monthlySales: 2100, minOrderAmount: 10, deliveryFee: 2, deliveryTime: 20, isNew: true, category: '快餐' })
  }
  
  if (activeCategory.value) {
    shopList.value = baseShops.filter(shop => shop.category === activeCategory.value)
  } else {
    shopList.value = baseShops
  }
}

const selectCategory = (category) => {
  activeCategory.value = category
  uni.showToast({ title: '正在切换...', icon: 'none' })
  
  if (category) {
    loadMockShops()
  } else {
    loadShops()
  }
}

const search = () => { loadShops() }

const goShop = (id) => {
  uni.navigateTo({ url: `/pages/shop/shop?id=${id}` })
}

const getShopCover = (shop) => {
  if (shop.logo) return shop.logo
  const id = Number(shop.id) || 1
  return `/static/shop${(id % 3) + 1}.jpg`
}

const goToRandomPick = () => {
  uni.navigateTo({ url: '/pages/random-pick/random-pick' })
}

// 获取商家活动列表
const getShopActivities = (shop) => {
  const activities = []
  
  if (shop.hasPromo) {
    activities.push({
      type: 'primary',
      label: '满减',
      text: '满30减5，满50减10'
    })
  }
  
  if (shop.isNew) {
    activities.push({
      type: 'success',
      label: '新客',
      text: '新用户立减3元'
    })
  }
  
  activities.push({
    type: 'default',
    label: '会员',
    text: '会员享9折优惠'
  })
  
  return activities
}

// 刷新店铺列表
const refreshShops = () => {
  loadShops()
}

const goToGroupOrder = () => {
  uni.navigateTo({ url: '/pages/group-order/group-order' })
}

const goToFlashSale = () => {
  uni.navigateTo({ url: '/pages/flash-sale/flash-sale' })
}

const goToRanking = () => {
  uni.navigateTo({ url: '/pages/ranking/ranking' })
}

const goToMysteryBox = () => {
  uni.navigateTo({ url: '/pages/mystery-box/mystery-box' })
}

const goToCommunity = () => {
  uni.navigateTo({ url: '/pages/community/community' })
}

onMounted(() => {
  getLocation()
  loadShops()
})
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  width: 100%;
  overflow-x: hidden;
  background-color: #f8f8f8;
}

/* 顶部头部 - 外卖平台标准橙色系 */
.header {
  background: linear-gradient(135deg, #FF6B00 0%, #FF9F43 50%, #FFB347 100%);
  padding: 30rpx;
  padding-top: 50rpx;
  padding-bottom: 40rpx;
  border-radius: 0 0 40rpx 40rpx;
  box-shadow: 0 8rpx 30rpx rgba(255, 107, 0, 0.25);
}

.location-bar {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
}

.location-icon {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.location-symbol {
  font-size: 28rpx;
  color: #fff;
  transform: rotate(-15deg);
  font-weight: bold;
}

.location-text {
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  margin-left: 12rpx;
  max-width: 400rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow-down {
  width: 0;
  height: 0;
  border-left: 8rpx solid transparent;
  border-right: 8rpx solid transparent;
  border-top: 10rpx solid rgba(255,255,255,0.9);
  margin-left: 12rpx;
}

/* 搜索栏 */
.search-bar {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 40rpx;
  padding: 20rpx 32rpx;
  box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.1);
  margin-top: 10rpx;
}

.search-icon-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
}

.search-icon {
  position: relative;
  width: 32rpx;
  height: 32rpx;
}

.search-circle {
  width: 22rpx;
  height: 22rpx;
  border: 3rpx solid #999;
  border-radius: 50%;
  position: absolute;
  top: 0;
  left: 0;
}

.search-line {
  width: 10rpx;
  height: 3rpx;
  background: #999;
  position: absolute;
  bottom: 2rpx;
  right: 0;
  transform: rotate(45deg);
  border-radius: 2rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

/* 分类导航 */
.category-nav {
  display: flex;
  white-space: nowrap;
  margin-top: 36rpx;
  padding-bottom: 12rpx;
}

.category-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  margin-right: 20rpx;
  padding: 8rpx 16rpx;
  border-radius: 60rpx;
  transition: all 0.3s;
}

.category-item.active {
  background: rgba(255,255,255,0.25);
}

.category-icon {
  width: 96rpx;
  height: 96rpx;
  background: rgba(255,255,255,0.9);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 16rpx rgba(0,0,0,0.08);
  transition: all 0.3s;
}

.category-icon.active {
  background: #fff;
  box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.12);
}

.category-symbol {
  font-size: 26rpx;
  color: #FF6B00;
  font-weight: 600;
}

/* 快捷功能入口 */
.quick-actions {
  display: flex;
  gap: 20rpx;
  padding: 30rpx;
  padding-top: 20rpx;
}

.action-card {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 24rpx 20rpx;
  border-radius: 20rpx;
  background: #fff;
  box-shadow: 0 6rpx 20rpx rgba(0,0,0,0.06);
  position: relative;
  overflow: hidden;
}

.random-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.group-card {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.action-icon-bg {
  width: 72rpx;
  height: 72rpx;
  background: rgba(255,255,255,0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  backdrop-filter: blur(4rpx);
}

/* 骰子图标 */
.dice-icon {
  position: relative;
}

.dice-face {
  width: 40rpx;
  height: 40rpx;
  background: #fff;
  border-radius: 8rpx;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dot {
  width: 8rpx;
  height: 8rpx;
  background: #764ba2;
  border-radius: 50%;
  position: absolute;
}

.dot-1 {
  top: 8rpx;
  left: 8rpx;
}

.dot-2 {
  bottom: 8rpx;
  right: 8rpx;
}

/* 拼单图标 */
.group-icon {
  position: relative;
  width: 40rpx;
  height: 40rpx;
}

.person {
  position: absolute;
  width: 16rpx;
  height: 16rpx;
  background: #fff;
  border-radius: 50%;
}

.person::after {
  content: '';
  position: absolute;
  bottom: -10rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 22rpx;
  height: 14rpx;
  background: #fff;
  border-radius: 10rpx 10rpx 0 0;
}

.person-1 {
  top: 0;
  left: 0;
}

.person-2 {
  top: 0;
  right: 0;
}

.person-3 {
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
}

.action-info {
  flex: 1;
}

.action-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #fff;
  display: block;
}

.action-desc {
  font-size: 20rpx;
  color: rgba(255,255,255,0.85);
  display: block;
  margin-top: 4rpx;
}

.action-arrow {
  width: 0;
  height: 0;
  border-top: 8rpx solid transparent;
  border-bottom: 8rpx solid transparent;
  border-left: 10rpx solid rgba(255,255,255,0.6);
}

/* 特色功能入口 */
.feature-grid {
  display: flex;
  justify-content: space-around;
  padding: 20rpx 30rpx;
  background: #fff;
  margin: 0 0 20rpx 0;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx;
}

.feature-icon {
  width: 88rpx;
  height: 88rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
}

.feature-icon.flash-sale {
  background: linear-gradient(135deg, #FF416C, #FF4B2B);
}

.feature-icon.ranking {
  background: linear-gradient(135deg, #FFD200, #F7971E);
}

.feature-icon.mystery-box {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.feature-icon.community {
  background: linear-gradient(135deg, #11998e, #38ef7d);
}

.icon-text {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
}

.feature-name {
  font-size: 24rpx;
  color: #333;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.filter-item {
  display: flex;
  align-items: center;
  margin-right: 40rpx;
  font-size: 26rpx;
  color: #666;
}

.filter-item.active {
  color: #FF6B00;
  font-weight: 600;
}

.filter-arrow {
  width: 0;
  height: 0;
  border-left: 6rpx solid transparent;
  border-right: 6rpx solid transparent;
  border-top: 8rpx solid currentColor;
  margin-left: 8rpx;
}

.filter-dots {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  margin-left: 8rpx;
}

.dot-item {
  width: 6rpx;
  height: 6rpx;
  background: currentColor;
  border-radius: 50%;
}

/* 店铺列表 */
.shop-list {
  flex: 1;
  width: 100%;
  height: calc(100vh - 560rpx);
  box-sizing: border-box;
  padding: 20rpx 30rpx;
}

.section-title {
  display: flex;
  align-items: center;
  margin-bottom: 24rpx;
  padding-left: 10rpx;
}

.title-icon {
  width: 6rpx;
  height: 32rpx;
  background: linear-gradient(180deg, #FF6B00, #FF9F43);
  border-radius: 3rpx;
  margin-right: 12rpx;
}

.title-text {
  font-size: 34rpx;
  font-weight: 700;
  color: #1a1a1a;
}

.title-sub {
  font-size: 22rpx;
  color: #999;
  margin-left: 12rpx;
  font-weight: normal;
}

/* 骨架屏列表 */
.skeleton-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.skeleton-item {
  display: flex;
  gap: 24rpx;
  background: #fff;
  padding: 24rpx;
  border-radius: 20rpx;
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

/* 商家列表 */
.merchant-list {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.safe-bottom {
  height: 120rpx;
}
</style>
