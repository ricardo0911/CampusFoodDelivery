<template>
  <view class="page">
    <!-- 头部 -->
    <view class="header">
      <view class="title-bar">
        <text class="back-btn" @click="goBack">←</text>
        <text class="title">👥 拼单</text>
      </view>
      <text class="subtitle">和室友一起点餐，享团购优惠</text>
    </view>

    <!-- 创建/加入切换 -->
    <view class="tab-container">
      <view class="tabs">
        <view class="tab" :class="{ active: activeTab === 'create' }" @click="activeTab = 'create'">
          发起拼单
        </view>
        <view class="tab" :class="{ active: activeTab === 'join' }" @click="activeTab = 'join'">
          加入拼单
        </view>
        <view class="tab" :class="{ active: activeTab === 'my' }" @click="activeTab = 'my'">
          我的拼单
        </view>
      </view>
    </view>

    <!-- 创建拼单 -->
    <view class="content" v-if="activeTab === 'create'">
      <view class="form-card">
        <view class="form-item">
          <text class="label">选择店铺</text>
          <view class="shop-selector" @click="showShopPicker = true">
            <text v-if="selectedShop">{{ selectedShop.name }}</text>
            <text v-else class="placeholder">点击选择店铺</text>
            <text class="arrow">→</text>
          </view>
        </view>
        
        <view class="form-item">
          <text class="label">拼单标题</text>
          <input class="input" v-model="groupTitle" placeholder="例如：宿舍楼下午茶" />
        </view>
        
        <view class="form-item">
          <text class="label">最大人数</text>
          <view class="number-picker">
            <text class="btn-minus" @click="maxMembers > 2 && maxMembers--">-</text>
            <text class="number">{{ maxMembers }}</text>
            <text class="btn-plus" @click="maxMembers < 10 && maxMembers++">+</text>
          </view>
        </view>
        
        <view class="form-item">
          <text class="label">目标金额</text>
          <view class="amount-options">
            <view 
              v-for="amount in [30, 50, 80, 100]" 
              :key="amount"
              class="amount-option"
              :class="{ active: targetAmount === amount }"
              @click="targetAmount = amount"
            >
              ¥{{ amount }}
            </view>
          </view>
        </view>
        
        <view class="form-item">
          <text class="label">优惠类型</text>
          <view class="discount-options">
            <view 
              class="discount-option"
              :class="{ active: discountType === 3 }"
              @click="discountType = 3"
            >
              <text class="discount-icon">🚴</text>
              <text class="discount-text">免配送费</text>
            </view>
            <view 
              class="discount-option"
              :class="{ active: discountType === 1 }"
              @click="discountType = 1"
            >
              <text class="discount-icon">💰</text>
              <text class="discount-text">满减5元</text>
            </view>
          </view>
        </view>
        
        <button class="btn-create" type="primary" @click="createGroup">发起拼单</button>
      </view>
      
      <!-- 拼单说明 -->
      <view class="tips-card">
        <text class="tips-title">📝 拼单规则</text>
        <view class="tips-list">
          <text class="tip">1. 发起拼单后，分享给室友或同学</text>
          <text class="tip">2. 达到目标金额即可享受优惠</text>
          <text class="tip">3. 拼单2小时内有效</text>
          <text class="tip">4. 每人单独结算，统一配送</text>
        </view>
      </view>
    </view>

    <!-- 加入拼单 -->
    <view class="content" v-if="activeTab === 'join'">
      <view class="join-card">
        <text class="join-title">输入拼单码</text>
        <view class="code-input-container">
          <input 
            class="code-input" 
            v-model="joinCode" 
            placeholder="请输入6位拼单码"
            maxlength="6"
          />
        </view>
        <button class="btn-join" type="primary" @click="joinGroup">加入拼单</button>
      </view>
      
      <!-- 扫码加入 -->
      <view class="scan-card" @click="scanCode">
        <text class="scan-icon">📷</text>
        <text class="scan-text">扫码加入拼单</text>
      </view>
    </view>

    <!-- 我的拼单 -->
    <view class="content" v-if="activeTab === 'my'">
      <view class="my-groups" v-if="myGroups.length > 0">
        <view 
          class="group-card" 
          v-for="group in myGroups" 
          :key="group.id"
          @click="viewGroupDetail(group)"
        >
          <view class="group-header">
            <text class="group-title">{{ group.title || group.shopName }}</text>
            <view class="group-status" :class="getStatusClass(group.status)">
              {{ getStatusText(group.status) }}
            </view>
          </view>
          
          <view class="group-info">
            <text class="shop-name">🏪 {{ group.shopName }}</text>
            <view class="group-progress">
              <view class="progress-bar">
                <view 
                  class="progress-fill" 
                  :style="{ width: getProgressWidth(group) }"
                ></view>
              </view>
              <text class="progress-text">
                {{ group.currentMembers }}/{{ group.maxMembers }}人
              </text>
            </view>
          </view>
          
          <view class="group-footer">
            <text class="group-code">拼单码: {{ group.groupCode }}</text>
            <text class="group-time">{{ formatTime(group.createdAt) }}</text>
          </view>
        </view>
      </view>
      
      <view class="empty-state" v-else>
        <text class="empty-icon">📭</text>
        <text class="empty-text">暂无拼单记录</text>
        <text class="empty-hint">发起或加入拼单，和朋友一起享优惠</text>
      </view>
    </view>

    <!-- 拼单详情弹窗 -->
    <view class="modal" v-if="showDetail" @click="showDetail = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">拼单详情</text>
          <text class="modal-close" @click="showDetail = false">×</text>
        </view>
        
        <view class="detail-section" v-if="currentGroup">
          <view class="detail-code">
            <text class="code-label">拼单码</text>
            <text class="code-value">{{ currentGroup.groupCode }}</text>
            <text class="copy-btn" @click="copyCode">复制</text>
          </view>
          
          <view class="detail-members">
            <text class="members-title">成员列表</text>
            <view class="member-item" v-for="(member, index) in groupMembers" :key="index">
              <view class="member-avatar">{{ member.nickname?.charAt(0) || '用' }}</view>
              <text class="member-name">{{ member.nickname || '用户' + member.userId }}</text>
              <text class="member-amount">¥{{ member.subtotal || 0 }}</text>
            </view>
          </view>
          
          <view class="detail-actions">
            <button class="btn-share" @click="shareGroup">分享给好友</button>
            <button class="btn-order" @click="goToOrder">去点餐</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 店铺选择器 -->
    <view class="modal" v-if="showShopPicker" @click="showShopPicker = false">
      <view class="shop-picker" @click.stop>
        <view class="picker-header">
          <text class="picker-title">选择店铺</text>
          <text class="picker-close" @click="showShopPicker = false">×</text>
        </view>
        <scroll-view class="shop-list" scroll-y>
          <view 
            class="shop-item" 
            v-for="shop in shopList" 
            :key="shop.id"
            @click="selectShop(shop)"
          >
            <text class="shop-icon">{{ shop.icon || '🏪' }}</text>
            <view class="shop-info">
              <text class="shop-name">{{ shop.name }}</text>
              <text class="shop-rating">⭐ {{ shop.rating || 4.5 }}</text>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const activeTab = ref('create')
const selectedShop = ref(null)
const groupTitle = ref('')
const maxMembers = ref(4)
const targetAmount = ref(50)
const discountType = ref(3)
const joinCode = ref('')
const myGroups = ref([])
const showDetail = ref(false)
const currentGroup = ref(null)
const groupMembers = ref([])
const showShopPicker = ref(false)
const shopList = ref([])

// 加载店铺列表
const loadShops = () => {
  shopList.value = [
    { id: 1, name: '黄焖鸡米饭', icon: '🍚', rating: 4.8 },
    { id: 2, name: '兰州拉面馆', icon: '🍜', rating: 4.6 },
    { id: 3, name: '麻辣香锅', icon: '🍲', rating: 4.9 },
    { id: 4, name: '沙县小吃', icon: '🥟', rating: 4.5 },
    { id: 5, name: '蜜雪冰城', icon: '🧋', rating: 4.8 },
    { id: 6, name: '瑞幸咖啡', icon: '☕', rating: 4.6 },
    { id: 7, name: '肯德基', icon: '🍔', rating: 4.4 },
    { id: 8, name: '必胜客', icon: '🍕', rating: 4.5 }
  ]
}

// 选择店铺
const selectShop = (shop) => {
  selectedShop.value = shop
  showShopPicker.value = false
}

// 创建拼单
const createGroup = () => {
  console.log('createGroup called, selectedShop:', selectedShop.value)
  
  if (!selectedShop.value) {
    uni.showToast({ title: '请选择店铺', icon: 'none' })
    return
  }
  
  // 生成拼单码
  const code = generateCode()
  console.log('Generated code:', code)
  
  // 创建拼单对象
  const newGroup = {
    id: Date.now(),
    groupCode: code,
    shopId: selectedShop.value.id,
    shopName: selectedShop.value.name,
    title: groupTitle.value || selectedShop.value.name + '拼单',
    maxMembers: maxMembers.value,
    currentMembers: 1,
    targetAmount: targetAmount.value,
    currentAmount: 0,
    discountType: discountType.value,
    status: 0,
    createdAt: new Date().toISOString()
  }
  
  // 添加到列表
  myGroups.value.unshift(newGroup)
  console.log('Group added, myGroups:', myGroups.value.length)
  
  // 先显示成功提示
  uni.showToast({ 
    title: '创建成功: ' + code, 
    icon: 'none',
    duration: 3000
  })
  
  // 延迟操作，确保toast显示
  setTimeout(() => {
    // 重置表单
    selectedShop.value = null
    groupTitle.value = ''
    // 切换到我的拼单
    activeTab.value = 'my'
  }, 500)
}

// 加入拼单
const joinGroup = async () => {
  if (!joinCode.value || joinCode.value.length !== 6) {
    uni.showToast({ title: '请输入6位拼单码', icon: 'none' })
    return
  }
  
  // 模拟加入成功
  uni.showToast({ title: '加入成功', icon: 'success' })
  
  const group = {
    id: Date.now(),
    groupCode: joinCode.value,
    shopName: '模拟店铺',
    title: '好友的拼单',
    maxMembers: 4,
    currentMembers: 2,
    status: 0,
    createdAt: new Date().toISOString()
  }
  
  myGroups.value.unshift(group)
  activeTab.value = 'my'
  joinCode.value = ''
}

// 扫码
const scanCode = () => {
  uni.scanCode({
    success: (res) => {
      joinCode.value = res.result
      joinGroup()
    }
  })
}

// 查看详情
const viewGroupDetail = (group) => {
  currentGroup.value = group
  groupMembers.value = [
    { userId: 1, nickname: '我', subtotal: 25 },
    { userId: 2, nickname: '室友A', subtotal: 18 }
  ]
  showDetail.value = true
}

// 复制拼单码
const copyCode = () => {
  uni.setClipboardData({
    data: currentGroup.value.groupCode,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

// 分享
const shareGroup = () => {
  uni.showToast({ title: '分享功能需要小程序环境', icon: 'none' })
}

// 去点餐
const goToOrder = () => {
  showDetail.value = false
  uni.navigateTo({ url: `/pages/shop/shop?id=${currentGroup.value.shopId}` })
}

// 生成拼单码
const generateCode = () => {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  let code = ''
  for (let i = 0; i < 6; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  return code
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = ['拼单中', '已成团', '已完成', '已取消', '已过期']
  return texts[status] || '拼单中'
}

// 获取状态样式
const getStatusClass = (status) => {
  const classes = ['pending', 'success', 'completed', 'cancelled', 'expired']
  return classes[status] || 'pending'
}

// 获取进度
const getProgressWidth = (group) => {
  return (group.currentMembers / group.maxMembers * 100) + '%'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

const goBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  loadShops()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
}

/* 头部 */
.header {
  padding: 60rpx 40rpx 30rpx;
  text-align: center;
}

.title-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-bottom: 16rpx;
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

.subtitle {
  font-size: 26rpx;
  color: rgba(255,255,255,0.8);
}

/* 标签页 */
.tab-container {
  padding: 0 40rpx;
  margin-bottom: 30rpx;
}

.tabs {
  display: flex;
  background: rgba(255,255,255,0.2);
  border-radius: 50rpx;
  padding: 8rpx;
}

.tab {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  font-size: 28rpx;
  color: rgba(255,255,255,0.8);
  border-radius: 40rpx;
  transition: all 0.3s;
}

.tab.active {
  background: #fff;
  color: #667eea;
  font-weight: bold;
}

/* 内容区 */
.content {
  padding: 0 30rpx 40rpx;
}

/* 表单卡片 */
.form-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 30rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.label {
  font-size: 26rpx;
  color: #666;
  display: block;
  margin-bottom: 16rpx;
}

.shop-selector {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
}

.placeholder {
  color: #999;
}

.arrow {
  color: #999;
}

.input {
  width: 100%;
  padding: 24rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
}

.number-picker {
  display: flex;
  align-items: center;
  gap: 30rpx;
}

.btn-minus, .btn-plus {
  width: 60rpx;
  height: 60rpx;
  background: #f5f5f5;
  border-radius: 50%;
  text-align: center;
  line-height: 60rpx;
  font-size: 36rpx;
  color: #667eea;
}

.number {
  font-size: 36rpx;
  font-weight: bold;
  color: #333;
}

.amount-options {
  display: flex;
  gap: 20rpx;
}

.amount-option {
  flex: 1;
  text-align: center;
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #333;
  border: 2rpx solid transparent;
}

.amount-option.active {
  background: rgba(102, 126, 234, 0.1);
  border-color: #667eea;
  color: #667eea;
}

.discount-options {
  display: flex;
  gap: 20rpx;
}

.discount-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  border: 2rpx solid transparent;
}

.discount-option.active {
  background: rgba(102, 126, 234, 0.1);
  border-color: #667eea;
}

.discount-icon {
  font-size: 36rpx;
  margin-bottom: 8rpx;
}

.discount-text {
  font-size: 24rpx;
  color: #333;
}

.btn-create {
  width: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  padding: 28rpx;
  border-radius: 50rpx;
  border: none;
  margin-top: 20rpx;
}

/* 提示卡片 */
.tips-card {
  background: rgba(255,255,255,0.9);
  border-radius: 20rpx;
  padding: 30rpx;
}

.tips-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.tip {
  font-size: 24rpx;
  color: #666;
}

/* 加入拼单 */
.join-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 50rpx 30rpx;
  text-align: center;
  margin-bottom: 30rpx;
}

.join-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 30rpx;
}

.code-input-container {
  margin-bottom: 30rpx;
}

.code-input {
  width: 80%;
  text-align: center;
  font-size: 48rpx;
  font-weight: bold;
  letter-spacing: 16rpx;
  padding: 30rpx;
  background: #f5f5f5;
  border-radius: 16rpx;
}

.btn-join {
  width: 80%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 32rpx;
  font-weight: bold;
  padding: 28rpx;
  border-radius: 50rpx;
  border: none;
}

.scan-card {
  background: rgba(255,255,255,0.9);
  border-radius: 20rpx;
  padding: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20rpx;
}

.scan-icon {
  font-size: 48rpx;
}

.scan-text {
  font-size: 30rpx;
  color: #333;
}

/* 我的拼单 */
.group-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.group-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.group-status {
  font-size: 22rpx;
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
}

.group-status.pending {
  background: #fff3cd;
  color: #856404;
}

.group-status.success {
  background: #d4edda;
  color: #155724;
}

.group-status.completed {
  background: #e2e3e5;
  color: #383d41;
}

.shop-name {
  font-size: 26rpx;
  color: #666;
  display: block;
  margin-bottom: 16rpx;
}

.group-progress {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.progress-bar {
  flex: 1;
  height: 12rpx;
  background: #eee;
  border-radius: 6rpx;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #667eea, #764ba2);
  border-radius: 6rpx;
}

.progress-text {
  font-size: 24rpx;
  color: #667eea;
}

.group-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 20rpx;
  padding-top: 20rpx;
  border-top: 1rpx solid #eee;
}

.group-code, .group-time {
  font-size: 24rpx;
  color: #999;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 100rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  display: block;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #fff;
  display: block;
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: rgba(255,255,255,0.7);
}

/* 弹窗 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-content {
  width: 85%;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.modal-close {
  font-size: 48rpx;
  color: #999;
}

.detail-section {
  padding: 30rpx;
}

.detail-code {
  display: flex;
  align-items: center;
  padding: 24rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  margin-bottom: 30rpx;
}

.code-label {
  font-size: 26rpx;
  color: #666;
  margin-right: 20rpx;
}

.code-value {
  flex: 1;
  font-size: 36rpx;
  font-weight: bold;
  color: #667eea;
  letter-spacing: 8rpx;
}

.copy-btn {
  font-size: 24rpx;
  color: #667eea;
  padding: 10rpx 20rpx;
  background: rgba(102, 126, 234, 0.1);
  border-radius: 20rpx;
}

.members-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 20rpx;
}

.member-item {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #eee;
}

.member-avatar {
  width: 60rpx;
  height: 60rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  color: #fff;
  text-align: center;
  line-height: 60rpx;
  font-size: 24rpx;
  margin-right: 20rpx;
}

.member-name {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.member-amount {
  font-size: 28rpx;
  color: #667eea;
  font-weight: bold;
}

.detail-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}

.btn-share, .btn-order {
  flex: 1;
  padding: 24rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  border: none;
}

.btn-share {
  background: #f5f5f5;
  color: #333;
}

.btn-order {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

/* 店铺选择器 */
.shop-picker {
  width: 85%;
  max-height: 70vh;
  background: #fff;
  border-radius: 24rpx;
  overflow: hidden;
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.picker-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.picker-close {
  font-size: 48rpx;
  color: #999;
}

.shop-list {
  max-height: 50vh;
}

.shop-item {
  display: flex;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #eee;
}

.shop-icon {
  font-size: 48rpx;
  margin-right: 20rpx;
}

.shop-info {
  flex: 1;
}

.shop-info .shop-name {
  font-size: 30rpx;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.shop-rating {
  font-size: 24rpx;
  color: #ffc107;
}
</style>
