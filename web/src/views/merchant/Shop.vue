<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>店铺管理</span>
        <el-button :type="shop.status === 1 ? 'danger' : 'success'" @click="toggleStatus">
          {{ shop.status === 1 ? '设为休息' : '开始营业' }}
        </el-button>
      </div>
    </template>
    <el-form :model="shop" label-width="100px" style="max-width: 600px">
      <el-form-item label="店铺名称">
        <el-input v-model="shop.name" />
      </el-form-item>
      <el-form-item label="店铺描述">
        <el-input v-model="shop.description" type="textarea" />
      </el-form-item>
      <el-form-item label="营业时间">
        <el-input v-model="shop.businessHours" placeholder="如: 周一至周日 07:00-22:00" />
      </el-form-item>
      <el-form-item label="配送范围">
        <el-input v-model="shop.deliveryScope" placeholder="如: 1-5号宿舍楼" />
      </el-form-item>
      <el-form-item label="起送价">
        <el-input-number v-model="shop.minOrderAmount" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="配送费">
        <el-input-number v-model="shop.deliveryFee" :min="0" :precision="2" />
      </el-form-item>
      <el-form-item label="店铺公告">
        <el-input v-model="shop.announcement" type="textarea" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveShop">保存</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const shop = reactive({
  name: '', description: '', businessHours: '', deliveryScope: '',
  minOrderAmount: 0, deliveryFee: 0, announcement: '', status: 0
})

const loadData = async () => {
  try {
    const res = await request.get('/merchant/shop/info')
    Object.assign(shop, res.data.shop)
  } catch (e) { console.error(e) }
}

const saveShop = async () => {
  try {
    await request.put('/merchant/shop/update', shop)
    ElMessage.success('保存成功')
  } catch (e) { console.error(e) }
}

const toggleStatus = async () => {
  try {
    const res = await request.post('/merchant/shop/toggle-status')
    shop.status = res.data.status
    ElMessage.success(res.message)
  } catch (e) { console.error(e) }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
