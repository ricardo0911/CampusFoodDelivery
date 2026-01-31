<template>
  <div class="inventory">
    <!-- 库存预警概览 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card warning" shadow="hover">
          <div class="stat-icon"><el-icon><WarningFilled /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ alertStats.lowStock || 0 }}</div>
            <div class="stat-label">库存不足</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card danger" shadow="hover">
          <div class="stat-icon"><el-icon><CircleCloseFilled /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ alertStats.outOfStock || 0 }}</div>
            <div class="stat-label">已售罄</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card success" shadow="hover">
          <div class="stat-icon"><el-icon><CircleCheckFilled /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ alertStats.normal || 0 }}</div>
            <div class="stat-label">库存正常</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card info" shadow="hover">
          <div class="stat-icon"><el-icon><Goods /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ alertStats.total || 0 }}</div>
            <div class="stat-label">菜品总数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 库存管理 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>库存管理</span>
          <div>
            <el-button type="primary" @click="showBatchDialog">批量设置</el-button>
            <el-button @click="exportInventory">导出报表</el-button>
          </div>
        </div>
      </template>

      <!-- 筛选 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="库存状态">
          <el-select v-model="filter.status" placeholder="全部" clearable @change="loadData">
            <el-option label="库存不足" value="low" />
            <el-option label="已售罄" value="out" />
            <el-option label="库存正常" value="normal" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜品分类">
          <el-select v-model="filter.categoryId" placeholder="全部" clearable @change="loadData">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input v-model="filter.keyword" placeholder="菜品名称" clearable @clear="loadData" @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="inventoryList" v-loading="loading" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="50" />
        <el-table-column prop="dishName" label="菜品名称">
          <template #default="{ row }">
            <div class="dish-info">
              <el-image :src="row.dishImage" style="width: 50px; height: 50px; border-radius: 8px;" fit="cover" />
              <span style="margin-left: 10px;">{{ row.dishName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="currentStock" label="当前库存" width="120">
          <template #default="{ row }">
            <el-tag :type="getStockType(row)" size="large">
              {{ row.currentStock === -1 ? '不限' : row.currentStock }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertThreshold" label="预警阈值" width="100" />
        <el-table-column prop="dailySales" label="日均销量" width="100" />
        <el-table-column label="预计可售天数" width="120">
          <template #default="{ row }">
            <span v-if="row.currentStock === -1">-</span>
            <span v-else-if="row.dailySales > 0" :class="getDaysClass(row)">
              {{ Math.floor(row.currentStock / row.dailySales) }}天
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="showStockDialog(row)">调整库存</el-button>
            <el-button size="small" @click="showLogDialog(row)">变更记录</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end;"
        @change="loadData"
      />
    </el-card>

    <!-- 调整库存对话框 -->
    <el-dialog v-model="stockDialogVisible" title="调整库存" width="400px">
      <el-form :model="stockForm" label-width="100px">
        <el-form-item label="菜品名称">
          <span>{{ stockForm.dishName }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <span>{{ stockForm.currentStock === -1 ? '不限' : stockForm.currentStock }}</span>
        </el-form-item>
        <el-form-item label="调整类型">
          <el-radio-group v-model="stockForm.adjustType">
            <el-radio label="set">设置为</el-radio>
            <el-radio label="add">增加</el-radio>
            <el-radio label="reduce">减少</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="stockForm.quantity" :min="stockForm.adjustType === 'set' ? -1 : 1" />
          <span style="margin-left: 10px; color: #999;" v-if="stockForm.adjustType === 'set'">(-1表示不限)</span>
        </el-form-item>
        <el-form-item label="预警阈值">
          <el-input-number v-model="stockForm.alertThreshold" :min="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockForm.remark" type="textarea" :rows="2" placeholder="请输入调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStockSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量设置对话框 -->
    <el-dialog v-model="batchDialogVisible" title="批量设置库存" width="400px">
      <el-form :model="batchForm" label-width="100px">
        <el-form-item label="已选菜品">
          <span>{{ selectedRows.length }}个</span>
        </el-form-item>
        <el-form-item label="设置库存">
          <el-input-number v-model="batchForm.stock" :min="-1" />
          <span style="margin-left: 10px; color: #999;">(-1表示不限)</span>
        </el-form-item>
        <el-form-item label="预警阈值">
          <el-input-number v-model="batchForm.alertThreshold" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 变更记录对话框 -->
    <el-dialog v-model="logDialogVisible" title="库存变更记录" width="700px">
      <el-table :data="stockLogs" v-loading="logLoading">
        <el-table-column prop="createTime" label="时间" width="180">
          <template #default="{ row }">
            {{ new Date(row.createTime).toLocaleString('zh-CN') }}
          </template>
        </el-table-column>
        <el-table-column prop="changeType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.changeQuantity > 0 ? 'success' : 'danger'">
              {{ row.changeQuantity > 0 ? '入库' : '出库' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="changeQuantity" label="变更数量" width="100">
          <template #default="{ row }">
            {{ row.changeQuantity > 0 ? '+' : '' }}{{ row.changeQuantity }}
          </template>
        </el-table-column>
        <el-table-column prop="afterStock" label="变更后库存" width="100" />
        <el-table-column prop="remark" label="备注" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const alertStats = ref({})
const inventoryList = ref([])
const categories = ref([])
const selectedRows = ref([])
const loading = ref(false)
const logLoading = ref(false)
const stockLogs = ref([])

const stockDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const logDialogVisible = ref(false)

const filter = reactive({
  status: '',
  categoryId: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const stockForm = reactive({
  dishId: null,
  dishName: '',
  currentStock: 0,
  adjustType: 'set',
  quantity: 0,
  alertThreshold: 10,
  remark: ''
})

const batchForm = reactive({
  stock: 100,
  alertThreshold: 10
})

const getStockType = (row) => {
  if (row.currentStock === -1) return 'info'
  if (row.currentStock === 0) return 'danger'
  if (row.currentStock <= row.alertThreshold) return 'warning'
  return 'success'
}

const getDaysClass = (row) => {
  const days = Math.floor(row.currentStock / row.dailySales)
  if (days <= 1) return 'text-danger'
  if (days <= 3) return 'text-warning'
  return 'text-success'
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size,
      ...filter
    }
    const res = await request.get('/merchant/inventory/list', { params })
    inventoryList.value = res.data?.records || res.data || []
    pagination.total = res.data?.total || inventoryList.value.length
  } catch (e) {
    console.error(e)
  }
  loading.value = false
}

const loadAlertStats = async () => {
  try {
    const res = await request.get('/merchant/inventory/alert-stats')
    alertStats.value = res.data || {}
  } catch (e) {
    console.error(e)
  }
}

const loadCategories = async () => {
  try {
    const res = await request.get('/merchant/dish/categories')
    categories.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const showStockDialog = (row) => {
  Object.assign(stockForm, {
    dishId: row.dishId,
    dishName: row.dishName,
    currentStock: row.currentStock,
    adjustType: 'set',
    quantity: row.currentStock === -1 ? 100 : row.currentStock,
    alertThreshold: row.alertThreshold || 10,
    remark: ''
  })
  stockDialogVisible.value = true
}

const handleStockSubmit = async () => {
  try {
    await request.post('/merchant/inventory/adjust', {
      dishId: stockForm.dishId,
      adjustType: stockForm.adjustType,
      quantity: stockForm.quantity,
      alertThreshold: stockForm.alertThreshold,
      remark: stockForm.remark
    })
    ElMessage.success('调整成功')
    stockDialogVisible.value = false
    loadData()
    loadAlertStats()
  } catch (e) {
    console.error(e)
  }
}

const showBatchDialog = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请先选择菜品')
    return
  }
  batchDialogVisible.value = true
}

const handleBatchSubmit = async () => {
  try {
    await request.post('/merchant/inventory/batch-adjust', {
      dishIds: selectedRows.value.map(r => r.dishId),
      stock: batchForm.stock,
      alertThreshold: batchForm.alertThreshold
    })
    ElMessage.success('批量设置成功')
    batchDialogVisible.value = false
    loadData()
    loadAlertStats()
  } catch (e) {
    console.error(e)
  }
}

const showLogDialog = async (row) => {
  logDialogVisible.value = true
  logLoading.value = true
  try {
    const res = await request.get(`/merchant/inventory/logs/${row.dishId}`)
    stockLogs.value = res.data || []
  } catch (e) {
    console.error(e)
  }
  logLoading.value = false
}

const exportInventory = () => {
  window.open('/api/merchant/inventory/export', '_blank')
}

onMounted(() => {
  loadData()
  loadAlertStats()
  loadCategories()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}
.stat-card .stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: white;
  margin-right: 15px;
}
.stat-card.warning .stat-icon { background: linear-gradient(135deg, #f6d365, #fda085); }
.stat-card.danger .stat-icon { background: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-card.success .stat-icon { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.stat-card.info .stat-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-value { font-size: 28px; font-weight: bold; color: #333; }
.stat-label { font-size: 14px; color: #999; }
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.filter-form { margin-bottom: 20px; }
.dish-info {
  display: flex;
  align-items: center;
}
.text-danger { color: #F56C6C; font-weight: bold; }
.text-warning { color: #E6A23C; font-weight: bold; }
.text-success { color: #67C23A; }
</style>
