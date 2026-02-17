<template>
  <view class="page">
    <!-- 头部搜索栏 -->
    <view class="header">
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          placeholder="搜索菜品名称"
          v-model="searchKeyword"
          @confirm="handleSearch"
        />
        <text class="clear-icon" v-if="searchKeyword" @click="clearSearch">×</text>
      </view>
      <view class="filter-bar">
        <view
          class="filter-item"
          :class="{ active: currentFilter === 'all' }"
          @click="setFilter('all')"
        >
          全部
        </view>
        <view
          class="filter-item"
          :class="{ active: currentFilter === 'on' }"
          @click="setFilter('on')"
        >
          在售
        </view>
        <view
          class="filter-item"
          :class="{ active: currentFilter === 'off' }"
          @click="setFilter('off')"
        >
          下架
        </view>
      </view>
    </view>

    <!-- 菜品列表 -->
    <scroll-view class="dish-list" scroll-y @scrolltolower="loadMore">
      <view class="dish-card" v-for="dish in dishList" :key="dish.id">
        <image class="dish-image" :src="dish.image || '/static/shop1.jpg'" mode="aspectFill" />
        <view class="dish-content">
          <view class="dish-header">
            <text class="dish-name">{{ dish.name }}</text>
            <view class="dish-status" :class="{ on: dish.status === 1 }">
              {{ dish.status === 1 ? '在售' : '下架' }}
            </view>
          </view>

          <view class="dish-stats">
            <text class="stat-item">销量 {{ dish.sales || 0 }}</text>
            <text class="stat-divider">|</text>
            <text class="stat-item">库存 {{ dish.stock || 0 }}</text>
          </view>

          <view class="dish-footer">
            <text class="dish-price">¥{{ dish.price }}</text>
            <view class="dish-actions">
              <switch
                :checked="dish.status === 1"
                @change="toggleStatus(dish)"
                color="#667eea"
              />
              <text class="action-btn edit" @click="editDish(dish)">编辑</text>
              <text class="action-btn delete" @click="deleteDish(dish.id)">删除</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="dishList.length > 0">
        <text v-if="loading">加载中...</text>
        <text v-else-if="noMore">没有更多了</text>
        <text v-else>上拉加载更多</text>
      </view>

      <!-- 空状态 -->
      <view class="empty-state" v-if="dishList.length === 0 && !loading">
        <view class="empty-icon">🍽️</view>
        <text class="empty-text">暂无菜品</text>
        <text class="empty-hint">点击右下角添加菜品</text>
      </view>

      <view class="safe-bottom"></view>
    </scroll-view>

    <!-- 添加按钮 -->
    <view class="fab" @click="showAddModal">
      <text class="fab-icon">+</text>
    </view>

    <!-- 添加/编辑菜品弹窗 -->
    <view class="modal" v-if="showModal" @click="closeModal">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ isEdit ? '编辑菜品' : '添加菜品' }}</text>
          <text class="modal-close" @click="closeModal">×</text>
        </view>

        <scroll-view class="modal-body" scroll-y>
          <view class="form-item">
            <text class="form-label">菜品名称</text>
            <input
              class="form-input"
              placeholder="请输入菜品名称"
              v-model="formData.name"
            />
          </view>

          <view class="form-item">
            <text class="form-label">菜品价格</text>
            <input
              class="form-input"
              type="digit"
              placeholder="请输入价格"
              v-model="formData.price"
            />
          </view>

          <view class="form-item">
            <text class="form-label">库存数量</text>
            <input
              class="form-input"
              type="number"
              placeholder="请输入库存"
              v-model="formData.stock"
            />
          </view>

          <view class="form-item">
            <text class="form-label">菜品描述</text>
            <textarea
              class="form-textarea"
              placeholder="请输入菜品描述"
              v-model="formData.description"
            />
          </view>

          <view class="form-item">
            <text class="form-label">菜品图片</text>
            <view class="image-upload" @click="chooseImage">
              <image
                v-if="formData.image"
                :src="formData.image"
                class="upload-preview"
                mode="aspectFill"
              />
              <view v-else class="upload-placeholder">
                <text class="upload-icon">📷</text>
                <text class="upload-text">点击上传图片</text>
              </view>
            </view>
          </view>

          <view class="form-item">
            <text class="form-label">上架状态</text>
            <switch :checked="formData.status === 1" @change="toggleFormStatus" color="#667eea" />
          </view>
        </scroll-view>

        <view class="modal-footer">
          <button class="btn-cancel" @click="closeModal">取消</button>
          <button class="btn-confirm" @click="submitForm">确定</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getDishList, addDish, updateDish, deleteDish as deleteDishApi } from '@/api/merchant'

const dishList = ref([])
const searchKeyword = ref('')
const currentFilter = ref('all')
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const noMore = ref(false)

// 弹窗相关
const showModal = ref(false)
const isEdit = ref(false)
const formData = ref({
  id: null,
  name: '',
  price: '',
  stock: '',
  description: '',
  image: '',
  status: 1
})

// 加载菜品列表
const loadDishList = async (isRefresh = true) => {
  if (loading.value) return
  loading.value = true

  if (isRefresh) {
    page.value = 1
    noMore.value = false
  }

  try {
    const params = {
      page: page.value,
      size: size.value,
      keyword: searchKeyword.value
    }

    if (currentFilter.value !== 'all') {
      params.status = currentFilter.value === 'on' ? 1 : 0
    }

    const res = await getDishList(params)
    if (res.data && res.data.records) {
      if (isRefresh) {
        dishList.value = res.data.records
      } else {
        dishList.value = [...dishList.value, ...res.data.records]
      }

      if (res.data.records.length < size.value) {
        noMore.value = true
      }
    }
  } catch (e) {
    console.error('获取菜品列表失败:', e)
    uni.showToast({ title: '获取列表失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 加载更多
const loadMore = () => {
  if (!noMore.value && !loading.value) {
    page.value++
    loadDishList(false)
  }
}

// 搜索
const handleSearch = () => {
  loadDishList(true)
}

// 清除搜索
const clearSearch = () => {
  searchKeyword.value = ''
  loadDishList(true)
}

// 筛选
const setFilter = (filter) => {
  currentFilter.value = filter
  loadDishList(true)
}

// 切换菜品状态
const toggleStatus = async (dish) => {
  const newStatus = dish.status === 1 ? 0 : 1
  try {
    await updateDish({ ...dish, status: newStatus })
    dish.status = newStatus
    uni.showToast({
      title: newStatus === 1 ? '已上架' : '已下架',
      icon: 'success'
    })
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// 删除菜品
const deleteDish = (id) => {
  uni.showModal({
    title: '确认删除',
    content: '确定要删除此菜品吗？删除后不可恢复',
    confirmColor: '#ff6b35',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteDishApi(id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          loadDishList(true)
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    }
  })
}

// 显示添加弹窗
const showAddModal = () => {
  isEdit.value = false
  formData.value = {
    id: null,
    name: '',
    price: '',
    stock: '',
    description: '',
    image: '',
    status: 1
  }
  showModal.value = true
}

// 编辑菜品
const editDish = (dish) => {
  isEdit.value = true
  formData.value = { ...dish }
  showModal.value = true
}

// 关闭弹窗
const closeModal = () => {
  showModal.value = false
}

// 切换表单状态
const toggleFormStatus = (e) => {
  formData.value.status = e.detail.value ? 1 : 0
}

// 选择图片
const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      // 这里应该上传图片到服务器，简化处理直接使用本地路径
      formData.value.image = res.tempFilePaths[0]
    }
  })
}

// 提交表单
const submitForm = async () => {
  if (!formData.value.name) {
    uni.showToast({ title: '请输入菜品名称', icon: 'none' })
    return
  }
  if (!formData.value.price) {
    uni.showToast({ title: '请输入菜品价格', icon: 'none' })
    return
  }

  try {
    if (isEdit.value) {
      await updateDish(formData.value)
      uni.showToast({ title: '修改成功', icon: 'success' })
    } else {
      await addDish(formData.value)
      uni.showToast({ title: '添加成功', icon: 'success' })
    }
    closeModal()
    loadDishList(true)
  } catch (e) {
    uni.showToast({ title: isEdit.value ? '修改失败' : '添加失败', icon: 'none' })
  }
}

onMounted(() => {
  loadDishList()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
}

/* 头部 */
.header {
  background: linear-gradient(135deg, #1a1a2e 0%, #2d2d44 100%);
  padding: 30rpx;
  padding-top: calc(var(--status-bar-height) + 20rpx);
}

.search-bar {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 40rpx;
  padding: 20rpx 30rpx;
}

.search-icon {
  font-size: 32rpx;
  margin-right: 16rpx;
}

.search-input {
  flex: 1;
  color: #fff;
  font-size: 28rpx;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

.clear-icon {
  color: rgba(255, 255, 255, 0.6);
  font-size: 40rpx;
  padding: 0 10rpx;
}

.filter-bar {
  display: flex;
  margin-top: 24rpx;
  gap: 20rpx;
}

.filter-item {
  padding: 12rpx 32rpx;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 32rpx;
  color: rgba(255, 255, 255, 0.8);
  font-size: 26rpx;
  transition: all 0.3s;
}

.filter-item.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
}

/* 菜品列表 */
.dish-list {
  flex: 1;
  padding: 20rpx;
}

.dish-card {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.05);
}

.dish-image {
  width: 160rpx;
  height: 160rpx;
  border-radius: 12rpx;
  margin-right: 24rpx;
  background: #f5f5f5;
}

.dish-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.dish-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.dish-name {
  font-size: 30rpx;
  font-weight: bold;
  color: #1a1a2e;
  flex: 1;
  margin-right: 16rpx;
}

.dish-status {
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  background: #f5f5f5;
  color: #999;
}

.dish-status.on {
  background: rgba(45, 164, 78, 0.1);
  color: #2da44e;
}

.dish-stats {
  display: flex;
  align-items: center;
  margin-top: 12rpx;
}

.stat-item {
  font-size: 24rpx;
  color: #666;
}

.stat-divider {
  color: #ddd;
  margin: 0 16rpx;
  font-size: 24rpx;
}

.dish-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}

.dish-price {
  font-size: 36rpx;
  font-weight: bold;
  color: #ff6b35;
}

.dish-actions {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.action-btn {
  font-size: 24rpx;
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.action-btn.edit {
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
}

.action-btn.delete {
  color: #ff6b35;
  background: rgba(255, 107, 53, 0.1);
}

/* 加载更多 */
.load-more {
  text-align: center;
  padding: 30rpx;
  color: #999;
  font-size: 24rpx;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 0;
}

.empty-icon {
  font-size: 100rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 32rpx;
  color: #333;
  margin-bottom: 12rpx;
}

.empty-hint {
  font-size: 26rpx;
  color: #999;
}

.safe-bottom {
  height: 120rpx;
}

/* 悬浮按钮 */
.fab {
  position: fixed;
  right: 40rpx;
  bottom: 60rpx;
  width: 100rpx;
  height: 100rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 30rpx rgba(102, 126, 234, 0.4);
  z-index: 100;
}

.fab-icon {
  color: #fff;
  font-size: 48rpx;
  font-weight: bold;
}

/* 弹窗 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 85%;
  max-height: 80vh;
  background: #fff;
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
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
  color: #1a1a2e;
}

.modal-close {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
}

.modal-body {
  padding: 30rpx;
  max-height: 60vh;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333;
  margin-bottom: 16rpx;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 80rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.form-textarea {
  width: 100%;
  height: 160rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  font-size: 28rpx;
  box-sizing: border-box;
}

.image-upload {
  width: 200rpx;
  height: 200rpx;
  background: #f8f9fa;
  border-radius: 12rpx;
  border: 2rpx dashed #ddd;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.upload-preview {
  width: 100%;
  height: 100%;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-icon {
  font-size: 48rpx;
  margin-bottom: 8rpx;
}

.upload-text {
  font-size: 24rpx;
  color: #999;
}

.modal-footer {
  display: flex;
  padding: 20rpx 30rpx 40rpx;
  gap: 20rpx;
  border-top: 1rpx solid #eee;
}

.btn-cancel {
  flex: 1;
  height: 80rpx;
  background: #f5f5f5;
  color: #666;
  font-size: 28rpx;
  border-radius: 12rpx;
  border: none;
}

.btn-confirm {
  flex: 1;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  font-size: 28rpx;
  border-radius: 12rpx;
  border: none;
}
</style>
