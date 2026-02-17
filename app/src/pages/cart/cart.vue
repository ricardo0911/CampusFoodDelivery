<template>
  <view class="page">
    <!-- 简洁头部 -->
    <view class="header">
      <view class="header-left">
        <text class="header-title">购物车</text>
        <text class="header-count" v-if="cartList.length > 0">({{ cartList.length }})</text>
      </view>
      <text class="header-action" v-if="cartList.length > 0" @click="clearCart">清空</text>
    </view>
    
    <!-- 购物车内容 -->
    <scroll-view class="cart-content" scroll-y v-if="cartList.length > 0">
      <!-- 商品列表 -->
      <view class="cart-list">
        <view class="cart-item" v-for="item in cartList" :key="item.id">
          <view class="item-left">
            <view class="item-check" :class="{ checked: item.selected }" @click="toggleSelect(item)">
              <text v-if="item.selected">✓</text>
            </view>
            <image class="item-image" :src="item.dishImage || '/static/default-dish.jpg'" mode="aspectFill" />
          </view>
          
          <view class="item-right">
            <view class="item-info">
              <text class="item-name">{{ item.dishName }}</text>
              <text class="item-spec" v-if="item.spec">{{ item.spec }}</text>
            </view>
            
            <view class="item-bottom">
              <view class="price-area">
                <text class="price-symbol">¥</text>
                <text class="price-value">{{ item.unitPrice }}</text>
              </view>
              
              <view class="quantity-control">
                <view class="qty-btn minus" @click="updateQuantity(item, -1)">
                  <text>−</text>
                </view>
                <text class="qty-num">{{ item.quantity }}</text>
                <view class="qty-btn plus" @click="updateQuantity(item, 1)">
                  <text>+</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </view>
      
      <!-- ⏰ 智能取餐方式选择 - 核心功能 -->
      <view class="pickup-section">
        <view class="section-header">
          <text class="section-icon">⏰</text>
          <text class="section-title">智能取餐方式</text>
          <view class="smart-badge" v-if="pickupMode === 'reservation'">省时省力</view>
        </view>
        
        <view class="pickup-options">
          <view 
            class="pickup-option" 
            :class="{ active: pickupMode === 'immediate' }"
            @click="selectPickupMode('immediate')"
          >
            <view class="option-icon">🚀</view>
            <view class="option-info">
              <text class="option-title">立即取餐</text>
              <text class="option-desc">约{{ immediateWaitTime }}分钟后</text>
            </view>
            <view class="option-check" v-if="pickupMode === 'immediate'">✓</view>
          </view>
          
          <view 
            class="pickup-option" 
            :class="{ active: pickupMode === 'reservation' }"
            @click="selectPickupMode('reservation')"
          >
            <view class="option-icon">📅</view>
            <view class="option-info">
              <text class="option-title">预约取餐</text>
              <text class="option-desc">选择时间，免排队</text>
            </view>
            <view class="option-discount" v-if="hasOffpeakDiscount">错峰8折</view>
            <view class="option-check" v-if="pickupMode === 'reservation'">✓</view>
          </view>
        </view>
        
        <!-- 预约时段选择 -->
        <view class="timeslot-picker" v-if="pickupMode === 'reservation'">
          <view class="picker-title">
            <text>选择取餐时间</text>
            <text class="queue-hint" v-if="selectedSlot">预计{{ getQueueCount(selectedSlot) }}人排队</text>
          </view>
          
          <scroll-view class="timeslots" scroll-x>
            <view 
              class="slot-item" 
              v-for="slot in availableSlots" 
              :key="slot.time"
              :class="{ active: selectedSlot === slot.time, offpeak: slot.isOffpeak }"
              @click="selectSlot(slot)"
            >
              <text class="slot-time">{{ slot.time }}</text>
              <text class="slot-queue">{{ slot.queueCount }}人</text>
              <text class="slot-discount" v-if="slot.isOffpeak">8折</text>
            </view>
          </scroll-view>
        </view>
      </view>
      
      <!-- 取餐码预览 -->
      <view class="pickup-code-preview" v-if="pickupMode === 'reservation' && selectedSlot">
        <view class="code-header">
          <text class="code-icon">🎫</text>
          <text class="code-title">预计取餐码</text>
        </view>
        <view class="code-info">
          <text class="code-time">取餐时间：{{ selectedSlot }}</text>
          <text class="code-location">取餐地点：{{ shopName }}</text>
        </view>
      </view>
      
      <!-- 优惠信息 -->
      <view class="promo-card">
        <view class="promo-row" @click="selectCoupon">
          <view class="promo-left">
            <text class="promo-icon">🎫</text>
            <text class="promo-label">优惠券</text>
          </view>
          <view class="promo-right">
            <text class="promo-value">暂无可用</text>
            <text class="promo-arrow">›</text>
          </view>
        </view>
        
        <!-- 错峰优惠提示 -->
        <view class="offpeak-promo" v-if="pickupMode === 'reservation' && hasOffpeakDiscount">
          <text class="offpeak-icon">🎁</text>
          <text class="offpeak-text">错峰取餐已省 ¥{{ offpeakSaving.toFixed(2) }}</text>
        </view>
      </view>
      
      <view class="safe-bottom"></view>
    </scroll-view>
    
    <!-- 空购物车 -->
    <view class="empty-state" v-else>
      <view class="empty-icon">🛒</view>
      <text class="empty-title">购物车是空的</text>
      <text class="empty-desc">去挑选心仪的美食吧</text>
      <view class="empty-btn" @click="goIndex">去首页看看</view>
    </view>
    
    <!-- 底部结算栏 -->
    <view class="checkout-bar" v-if="cartList.length > 0">
      <view class="bar-left" @click="toggleAll">
        <view class="check-box" :class="{ checked: allSelected }">
          <text v-if="allSelected">✓</text>
        </view>
        <text class="select-text">全选</text>
      </view>
      
      <view class="bar-center">
        <view class="price-breakdown">
          <text class="total-label">合计</text>
          <view class="total-price">
            <text class="total-symbol">¥</text>
            <text class="total-value">{{ finalAmount }}</text>
          </view>
          <text class="original-price" v-if="hasOffpeakDiscount">¥{{ originalAmount }}</text>
        </view>
        <text class="save-hint" v-if="hasOffpeakDiscount">已省¥{{ offpeakSaving.toFixed(2) }}</text>
      </view>
      
      <view class="checkout-btn" :class="{ disabled: selectedCount === 0 }" @click="checkout">
        {{ pickupMode === 'reservation' ? '预约取餐' : '去结算' }}<text v-if="selectedCount > 0">({{ selectedCount }})</text>
      </view>
    </view>
    
    <!-- 下单成功弹窗 -->
    <view class="order-success-modal" v-if="showSuccessModal">
      <view class="modal-mask" @click="closeSuccessModal"></view>
      <view class="modal-content">
        <view class="modal-header">
          <text class="success-icon">✅</text>
          <text class="success-title">下单成功！</text>
        </view>
        
        <view class="pickup-code-card">
          <text class="code-label">您的取餐码</text>
          <text class="pickup-code">{{ generatedPickupCode }}</text>
          <text class="code-time">取餐时间：{{ selectedSlot || '尽快' }}</text>
        </view>
        
        <view class="modal-tips">
          <view class="tip-item">
            <text class="tip-icon">📍</text>
            <text>请到 {{ shopName }} 取餐窗口出示取餐码</text>
          </view>
          <view class="tip-item">
            <text class="tip-icon">⏰</text>
            <text>请在预约时间前后10分钟内取餐</text>
          </view>
        </view>
        
        <view class="modal-actions">
          <view class="action-btn primary" @click="viewOrderDetail">查看订单详情</view>
          <view class="action-btn" @click="closeSuccessModal">返回首页</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { get, put, del, post } from '@/utils/request'

const cartList = ref([])
const discount = ref(0)
const pickupMode = ref('reservation') // 'immediate' 或 'reservation'
const selectedSlot = ref('')
const shopName = ref('第一食堂（学苑餐厅）')
const showSuccessModal = ref(false)
const generatedPickupCode = ref('')

// 当前排队等待时间
const immediateWaitTime = ref(15)

// 可选时段
const availableSlots = ref([
  { time: '11:30', queueCount: 12, isOffpeak: false },
  { time: '12:00', queueCount: 25, isOffpeak: false },
  { time: '12:30', queueCount: 18, isOffpeak: false },
  { time: '13:00', queueCount: 8, isOffpeak: true },
  { time: '13:30', queueCount: 5, isOffpeak: true },
  { time: '17:30', queueCount: 10, isOffpeak: false },
  { time: '18:00', queueCount: 22, isOffpeak: false },
  { time: '18:30', queueCount: 15, isOffpeak: false },
  { time: '19:00', queueCount: 6, isOffpeak: true },
])

// 是否有错峰优惠
const hasOffpeakDiscount = computed(() => {
  if (pickupMode.value !== 'reservation') return false
  const slot = availableSlots.value.find(s => s.time === selectedSlot.value)
  return slot?.isOffpeak || false
})

// 错峰节省金额
const offpeakSaving = computed(() => {
  if (!hasOffpeakDiscount.value) return 0
  return parseFloat(totalAmount.value) * 0.2
})

const totalAmount = computed(() => {
  return cartList.value
    .filter(item => item.selected)
    .reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)
    .toFixed(2)
})

const originalAmount = computed(() => {
  return (parseFloat(totalAmount.value)).toFixed(2)
})

const finalAmount = computed(() => {
  let total = parseFloat(totalAmount.value) - discount.value
  if (hasOffpeakDiscount.value) {
    total = total * 0.8  // 错峰8折
  }
  return total.toFixed(2)
})

const selectedCount = computed(() => {
  return cartList.value.filter(item => item.selected).reduce((sum, item) => sum + item.quantity, 0)
})

const allSelected = computed(() => {
  return cartList.value.length > 0 && cartList.value.every(item => item.selected)
})

const loadCart = async () => {
  try {
    const res = await get('/customer/cart/list')
    if (res.data && res.data.length > 0) {
      cartList.value = res.data.map(item => ({ ...item, selected: true }))
    } else {
      loadMockCart()
    }
  } catch (e) {
    loadMockCart()
  }
}

const loadMockCart = () => {
  cartList.value = [
    { id: 1, dishName: '招牌红烧肉套餐', dishImage: '/static/shop1.jpg', unitPrice: 18, quantity: 1, selected: true, spec: '米饭+例汤' },
    { id: 2, dishName: '番茄鸡蛋盖饭', dishImage: '/static/shop2.jpg', unitPrice: 12, quantity: 1, selected: true },
    { id: 3, dishName: '酸梅汤', dishImage: '/static/shop3.jpg', unitPrice: 3, quantity: 2, selected: true },
  ]
  // 默认选中一个时段
  selectedSlot.value = '13:00'
}

const selectPickupMode = (mode) => {
  pickupMode.value = mode
  if (mode === 'reservation' && !selectedSlot.value) {
    // 默认选中第一个错峰时段
    const offpeakSlot = availableSlots.value.find(s => s.isOffpeak)
    selectedSlot.value = offpeakSlot?.time || availableSlots.value[0]?.time
  }
}

const selectSlot = (slot) => {
  selectedSlot.value = slot.time
}

const getQueueCount = (time) => {
  const slot = availableSlots.value.find(s => s.time === time)
  return slot?.queueCount || 0
}

const toggleSelect = (item) => {
  item.selected = !item.selected
}

const toggleAll = () => {
  const newVal = !allSelected.value
  cartList.value.forEach(item => item.selected = newVal)
}

const updateQuantity = async (item, delta) => {
  const newQty = item.quantity + delta
  if (newQty <= 0) {
    cartList.value = cartList.value.filter(i => i.id !== item.id)
    try { await del(`/customer/cart/delete/${item.id}`) } catch (e) {}
  } else {
    item.quantity = newQty
    try { await put(`/customer/cart/update/${item.id}`, { quantity: newQty }) } catch (e) {}
  }
}

const clearCart = async () => {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空购物车吗？',
    success: (res) => {
      if (res.confirm) {
        cartList.value = []
      }
    }
  })
}

const generatePickupCode = () => {
  // 生成取餐码：字母+数字组合
  const letters = 'ABCDEFGHJKLMNPQRSTUVWXYZ'
  const letter = letters[Math.floor(Math.random() * letters.length)]
  const numbers = String(Math.floor(Math.random() * 1000)).padStart(3, '0')
  return letter + numbers
}

const goIndex = () => { uni.switchTab({ url: '/pages/index/index' }) }

const checkout = async () => {
  if (selectedCount.value === 0) {
    uni.showToast({ title: '请选择商品', icon: 'none' })
    return
  }
  
  if (pickupMode.value === 'reservation' && !selectedSlot.value) {
    uni.showToast({ title: '请选择取餐时间', icon: 'none' })
    return
  }
  
  // 生成取餐码
  generatedPickupCode.value = generatePickupCode()
  
  // 显示成功弹窗
  showSuccessModal.value = true
  
  // 清空购物车
  cartList.value = []
}

const closeSuccessModal = () => {
  showSuccessModal.value = false
  uni.switchTab({ url: '/pages/index/index' })
}

const viewOrderDetail = () => {
  showSuccessModal.value = false
  uni.navigateTo({ url: '/pages/reservation/status' })
}

const selectCoupon = () => {
  uni.showToast({ title: '暂无可用优惠券', icon: 'none' })
}

onMounted(() => {
  loadCart()
  // 加载可用时段
  loadAvailableSlots()
})

const loadAvailableSlots = async () => {
  try {
    const res = await get('/customer/reservation/available-slots')
    if (res.code === 200 && res.data) {
      availableSlots.value = res.data
    }
  } catch (e) {
    // 使用默认时段
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 130rpx;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx 32rpx;
  padding-top: calc(var(--status-bar-height) + 24rpx);
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.header-left {
  display: flex;
  align-items: baseline;
}

.header-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1a1a;
}

.header-count {
  font-size: 28rpx;
  color: #999;
  margin-left: 8rpx;
}

.header-action {
  font-size: 28rpx;
  color: #666;
}

.cart-content {
  padding: 20rpx;
}

.cart-list {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.cart-item {
  display: flex;
  padding: 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.cart-item:last-child {
  border-bottom: none;
}

.item-left {
  display: flex;
  align-items: flex-start;
  margin-right: 16rpx;
}

.item-check, .check-box {
  width: 40rpx;
  height: 40rpx;
  border: 2rpx solid #ddd;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16rpx;
  margin-top: 50rpx;
  font-size: 22rpx;
  color: #fff;
  flex-shrink: 0;
  background: #fff;
}

.item-check.checked, .check-box.checked {
  background: #667eea;
  border-color: #667eea;
}

.item-image {
  width: 140rpx;
  height: 140rpx;
  border-radius: 12rpx;
  flex-shrink: 0;
}

.item-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 140rpx;
}

.item-name {
  font-size: 28rpx;
  font-weight: 500;
  color: #1a1a1a;
}

.item-spec {
  font-size: 24rpx;
  color: #999;
  margin-top: 6rpx;
}

.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price-area {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  font-size: 24rpx;
  color: #667eea;
}

.price-value {
  font-size: 32rpx;
  color: #667eea;
  font-weight: 700;
}

.quantity-control {
  display: flex;
  align-items: center;
}

.qty-btn {
  width: 48rpx;
  height: 48rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
}

.qty-btn.minus {
  background: #f5f5f5;
  color: #666;
}

.qty-btn.plus {
  background: #667eea;
  color: #fff;
}

.qty-num {
  min-width: 56rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 600;
}

/* 智能取餐方式 */
.pickup-section {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.section-icon {
  font-size: 32rpx;
  margin-right: 8rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1a1a1a;
  flex: 1;
}

.smart-badge {
  font-size: 20rpx;
  color: #fff;
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 4rpx 12rpx;
  border-radius: 16rpx;
}

.pickup-options {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.pickup-option {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #f8f9ff;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.2s;
}

.pickup-option.active {
  background: #f0f3ff;
  border-color: #667eea;
}

.option-icon {
  font-size: 40rpx;
  margin-right: 16rpx;
}

.option-info {
  flex: 1;
}

.option-title {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1a1a;
  display: block;
}

.option-desc {
  font-size: 24rpx;
  color: #999;
  margin-top: 4rpx;
}

.option-discount {
  font-size: 20rpx;
  color: #f5576c;
  background: rgba(245, 87, 108, 0.1);
  padding: 6rpx 12rpx;
  border-radius: 12rpx;
  margin-right: 12rpx;
}

.option-check {
  width: 36rpx;
  height: 36rpx;
  background: #667eea;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
}

/* 时段选择 */
.timeslot-picker {
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #f0f0f0;
}

.picker-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
  font-size: 26rpx;
  color: #666;
}

.queue-hint {
  font-size: 24rpx;
  color: #f5576c;
}

.timeslots {
  white-space: nowrap;
}

.slot-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 16rpx 24rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  margin-right: 16rpx;
  min-width: 100rpx;
  border: 2rpx solid transparent;
}

.slot-item.active {
  background: #f0f3ff;
  border-color: #667eea;
}

.slot-item.offpeak {
  background: #fff5f5;
}

.slot-item.offpeak.active {
  background: #ffebeb;
  border-color: #f5576c;
}

.slot-time {
  font-size: 28rpx;
  font-weight: 600;
  color: #1a1a1a;
}

.slot-queue {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

.slot-discount {
  font-size: 18rpx;
  color: #f5576c;
  background: rgba(245, 87, 108, 0.1);
  padding: 2rpx 8rpx;
  border-radius: 8rpx;
  margin-top: 4rpx;
}

/* 取餐码预览 */
.pickup-code-preview {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.code-header {
  display: flex;
  align-items: center;
  margin-bottom: 12rpx;
}

.code-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.code-title {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
}

.code-info {
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.code-time, .code-location {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

/* 优惠卡片 */
.promo-card {
  background: #fff;
  border-radius: 16rpx;
  overflow: hidden;
  margin-bottom: 20rpx;
}

.promo-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 28rpx 24rpx;
}

.promo-left {
  display: flex;
  align-items: center;
}

.promo-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.promo-label {
  font-size: 28rpx;
  color: #333;
}

.promo-right {
  display: flex;
  align-items: center;
}

.promo-value {
  font-size: 26rpx;
  color: #999;
}

.promo-arrow {
  font-size: 32rpx;
  color: #ccc;
  margin-left: 8rpx;
}

.offpeak-promo {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  background: linear-gradient(135deg, #fff5f5, #ffebeb);
  border-top: 1rpx solid #f0f0f0;
}

.offpeak-icon {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.offpeak-text {
  font-size: 26rpx;
  color: #f5576c;
  font-weight: 500;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 32rpx;
}

.empty-title {
  font-size: 32rpx;
  color: #333;
  font-weight: 500;
  margin-bottom: 12rpx;
}

.empty-desc {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 48rpx;
}

.empty-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  padding: 24rpx 64rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
}

/* 底部结算栏 */
.checkout-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
  padding-bottom: env(safe-area-inset-bottom);
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.08);
  z-index: 100;
}

.bar-left {
  display: flex;
  align-items: center;
  padding: 0 16rpx;
}

.select-text {
  font-size: 26rpx;
  color: #333;
}

.bar-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  margin-right: 20rpx;
}

.price-breakdown {
  display: flex;
  align-items: baseline;
}

.total-label {
  font-size: 26rpx;
  color: #666;
  margin-right: 4rpx;
}

.total-price {
  display: flex;
  align-items: baseline;
}

.total-symbol {
  font-size: 24rpx;
  color: #667eea;
  font-weight: 600;
}

.total-value {
  font-size: 40rpx;
  color: #667eea;
  font-weight: 700;
}

.original-price {
  font-size: 24rpx;
  color: #999;
  text-decoration: line-through;
  margin-left: 8rpx;
}

.save-hint {
  font-size: 22rpx;
  color: #f5576c;
}

.checkout-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  padding: 24rpx 40rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
  flex-shrink: 0;
}

.checkout-btn.disabled {
  background: #ccc;
}

/* 成功弹窗 */
.order-success-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
}

.modal-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
}

.modal-content {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 85%;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
}

.modal-header {
  text-align: center;
  margin-bottom: 30rpx;
}

.success-icon {
  font-size: 80rpx;
  display: block;
  margin-bottom: 16rpx;
}

.success-title {
  font-size: 36rpx;
  font-weight: 700;
  color: #1a1a1a;
}

.pickup-code-card {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 16rpx;
  padding: 30rpx;
  text-align: center;
  margin-bottom: 24rpx;
}

.code-label {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
  display: block;
  margin-bottom: 12rpx;
}

.pickup-code {
  font-size: 72rpx;
  font-weight: 800;
  color: #fff;
  letter-spacing: 8rpx;
  display: block;
  margin-bottom: 12rpx;
}

.code-time {
  font-size: 24rpx;
  color: rgba(255, 255, 255, 0.8);
}

.modal-tips {
  margin-bottom: 30rpx;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12rpx;
  font-size: 26rpx;
  color: #666;
}

.tip-icon {
  margin-right: 8rpx;
}

.modal-actions {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.action-btn {
  padding: 24rpx;
  text-align: center;
  border-radius: 40rpx;
  font-size: 28rpx;
  font-weight: 500;
  background: #f5f5f5;
  color: #666;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

.safe-bottom {
  height: 40rpx;
}
</style>
