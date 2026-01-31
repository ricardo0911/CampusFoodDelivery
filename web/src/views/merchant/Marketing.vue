<template>
  <div class="marketing">
    <!-- 营销活动列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>营销活动</span>
          <el-button type="primary" @click="showDialog()">
            <el-icon><Plus /></el-icon>创建活动
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="loadData">
        <el-tab-pane label="进行中" name="active" />
        <el-tab-pane label="未开始" name="pending" />
        <el-tab-pane label="已结束" name="ended" />
        <el-tab-pane label="全部" name="all" />
      </el-tabs>

      <el-table :data="campaigns" v-loading="loading">
        <el-table-column prop="name" label="活动名称" />
        <el-table-column prop="type" label="活动类型">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="活动时间">
          <template #default="{ row }">
            {{ formatDate(row.startTime) }} ~ {{ formatDate(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="discount" label="优惠力度">
          <template #default="{ row }">
            <span v-if="row.type === 'discount'">{{ row.discount }}折</span>
            <span v-else-if="row.type === 'reduce'">满{{ row.threshold }}减{{ row.reduceAmount }}</span>
            <span v-else-if="row.type === 'gift'">赠品活动</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="效果">
          <template #default="{ row }">
            <div>参与: {{ row.participantCount || 0 }}人</div>
            <div>带动销售: ¥{{ row.salesAmount || 0 }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="showDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)" v-if="row.status === 0">删除</el-button>
            <el-button size="small" type="warning" @click="handleStop(row)" v-if="row.status === 1">停止</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 智能推荐 -->
    <el-card style="margin-top: 20px;">
      <template #header><span>智能营销建议</span></template>
      <el-row :gutter="20">
        <el-col :span="8" v-for="(suggestion, index) in suggestions" :key="index">
          <el-card shadow="hover" class="suggestion-card">
            <div class="suggestion-icon">
              <el-icon :size="32"><component :is="suggestion.icon" /></el-icon>
            </div>
            <div class="suggestion-content">
              <div class="suggestion-title">{{ suggestion.title }}</div>
              <div class="suggestion-desc">{{ suggestion.description }}</div>
              <el-button type="primary" size="small" @click="applySuggestion(suggestion)">
                一键创建
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <!-- 创建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑活动' : '创建活动'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择活动类型" style="width: 100%;">
            <el-option label="折扣活动" value="discount" />
            <el-option label="满减活动" value="reduce" />
            <el-option label="赠品活动" value="gift" />
            <el-option label="新客专享" value="new_customer" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动时间" prop="timeRange">
          <el-date-picker
            v-model="form.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            style="width: 100%;"
          />
        </el-form-item>

        <!-- 折扣活动 -->
        <el-form-item label="折扣" v-if="form.type === 'discount'" prop="discount">
          <el-input-number v-model="form.discount" :min="1" :max="9.9" :step="0.1" />
          <span style="margin-left: 10px;">折</span>
        </el-form-item>

        <!-- 满减活动 -->
        <template v-if="form.type === 'reduce'">
          <el-form-item label="满足金额" prop="threshold">
            <el-input-number v-model="form.threshold" :min="1" />
            <span style="margin-left: 10px;">元</span>
          </el-form-item>
          <el-form-item label="减免金额" prop="reduceAmount">
            <el-input-number v-model="form.reduceAmount" :min="1" />
            <span style="margin-left: 10px;">元</span>
          </el-form-item>
        </template>

        <!-- 适用菜品 -->
        <el-form-item label="适用菜品">
          <el-radio-group v-model="form.applyScope">
            <el-radio label="all">全部菜品</el-radio>
            <el-radio label="selected">指定菜品</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.applyScope === 'selected'">
          <el-select v-model="form.dishIds" multiple placeholder="选择菜品" style="width: 100%;">
            <el-option v-for="dish in dishes" :key="dish.id" :label="dish.name" :value="dish.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { TrendCharts, Timer, Gift } from '@element-plus/icons-vue'
import request from '../../api/request'

const activeTab = ref('active')
const campaigns = ref([])
const dishes = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const form = reactive({
  name: '',
  type: 'discount',
  timeRange: [],
  discount: 8,
  threshold: 30,
  reduceAmount: 5,
  applyScope: 'all',
  dishIds: [],
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择活动类型', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择活动时间', trigger: 'change' }]
}

const suggestions = ref([
  {
    icon: TrendCharts,
    title: '午餐高峰折扣',
    description: '根据数据分析，建议在11:00-13:00推出8折优惠，预计可提升30%订单量',
    type: 'discount',
    discount: 8,
    timeRange: 'lunch'
  },
  {
    icon: Timer,
    title: '下午茶满减',
    description: '14:00-17:00订单较少，建议推出满20减5活动吸引顾客',
    type: 'reduce',
    threshold: 20,
    reduceAmount: 5,
    timeRange: 'afternoon'
  },
  {
    icon: Gift,
    title: '新客首单优惠',
    description: '新客转化率较低，建议推出新客首单立减10元活动',
    type: 'new_customer',
    reduceAmount: 10
  }
])

const getTypeName = (type) => {
  const map = { discount: '折扣', reduce: '满减', gift: '赠品', new_customer: '新客专享' }
  return map[type] || type
}

const getTypeTag = (type) => {
  const map = { discount: 'primary', reduce: 'success', gift: 'warning', new_customer: 'danger' }
  return map[type] || ''
}

const getStatusName = (status) => {
  const map = { 0: '未开始', 1: '进行中', 2: '已结束', 3: '已停止' }
  return map[status] || ''
}

const getStatusTag = (status) => {
  const map = { 0: 'info', 1: 'success', 2: '', 3: 'danger' }
  return map[status] || ''
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {}
    if (activeTab.value !== 'all') {
      const statusMap = { active: 1, pending: 0, ended: 2 }
      params.status = statusMap[activeTab.value]
    }
    const res = await request.get('/merchant/marketing/campaigns', { params })
    campaigns.value = res.data || []
  } catch (e) {
    console.error(e)
  }
  loading.value = false
}

const loadDishes = async () => {
  try {
    const res = await request.get('/merchant/dish/list')
    dishes.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const showDialog = (row = null) => {
  editingId.value = row?.id || null
  if (row) {
    Object.assign(form, {
      name: row.name,
      type: row.type,
      timeRange: [new Date(row.startTime), new Date(row.endTime)],
      discount: row.discount || 8,
      threshold: row.threshold || 30,
      reduceAmount: row.reduceAmount || 5,
      applyScope: row.dishIds?.length ? 'selected' : 'all',
      dishIds: row.dishIds || [],
      description: row.description || ''
    })
  } else {
    Object.assign(form, {
      name: '',
      type: 'discount',
      timeRange: [],
      discount: 8,
      threshold: 30,
      reduceAmount: 5,
      applyScope: 'all',
      dishIds: [],
      description: ''
    })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    const data = {
      ...form,
      startTime: form.timeRange[0],
      endTime: form.timeRange[1],
      dishIds: form.applyScope === 'all' ? [] : form.dishIds
    }
    delete data.timeRange
    delete data.applyScope

    if (editingId.value) {
      await request.put(`/merchant/marketing/campaigns/${editingId.value}`, data)
      ElMessage.success('更新成功')
    } else {
      await request.post('/merchant/marketing/campaigns', data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定删除该活动吗？', '提示', { type: 'warning' })
  try {
    await request.delete(`/merchant/marketing/campaigns/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const handleStop = async (row) => {
  await ElMessageBox.confirm('确定停止该活动吗？', '提示', { type: 'warning' })
  try {
    await request.post(`/merchant/marketing/campaigns/${row.id}/stop`)
    ElMessage.success('已停止')
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const applySuggestion = (suggestion) => {
  Object.assign(form, {
    name: suggestion.title,
    type: suggestion.type,
    discount: suggestion.discount || 8,
    threshold: suggestion.threshold || 30,
    reduceAmount: suggestion.reduceAmount || 5,
    applyScope: 'all',
    dishIds: [],
    description: suggestion.description
  })

  // 设置默认时间范围
  const now = new Date()
  const start = new Date(now)
  const end = new Date(now)
  end.setDate(end.getDate() + 7)
  form.timeRange = [start, end]

  dialogVisible.value = true
}

onMounted(() => {
  loadData()
  loadDishes()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.suggestion-card {
  display: flex;
  padding: 20px;
}
.suggestion-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 15px;
  flex-shrink: 0;
}
.suggestion-content {
  flex: 1;
}
.suggestion-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}
.suggestion-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.5;
}
</style>
