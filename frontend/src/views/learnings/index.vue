<template>
  <div class="learnings-page">
    <div class="page-header">
      <h2>学习记录</h2>
      <div class="header-actions">
        <el-button
          type="danger"
          :disabled="selectedIds.length === 0"
          @click="batchDelete"
        >
          批量删除 ({{ selectedIds.length }})
        </el-button>
        <el-button type="primary" @click="openAddDialog">+ 添加记录</el-button>
      </div>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-num">{{ stats?.recentCount || 0 }}</div>
            <div class="stat-label">近30天学习次数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-num">{{ stats?.recentMinutes || 0 }}</div>
            <div class="stat-label">近30天学习分钟数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-num">{{ total }}</div>
            <div class="stat-label">总记录数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-num">
              {{ Object.keys(stats?.tagDistribution || {}).length }}
            </div>
            <div class="stat-label">标签种类</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <el-form :inline="true" :model="filters" class="filter-bar">
        <el-form-item label="搜索">
          <el-input
            v-model="filters.keyword"
            placeholder="标题或内容"
            style="width: 200px"
            clearable
            @keyup.enter="search"
          />
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="filters.source" clearable style="width: 140px" placeholder="全部">
            <el-option label="文档" value="文档" />
            <el-option label="视频" value="视频" />
            <el-option label="项目" value="项目" />
            <el-option label="面试准备" value="面试准备" />
            <el-option label="书籍" value="书籍" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始"
            end-placeholder="结束"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <div v-if="records.length === 0" class="empty-state">
        <el-empty description="暂无学习记录" />
      </div>
      <el-table
        v-else
        :data="records"
        stripe
        style="width: 100%"
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="content-preview">{{ row.content || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="durationMinutes" label="时长(分钟)" width="100" />
        <el-table-column prop="source" label="来源" width="100" />
        <el-table-column label="标签" width="200">
          <template #default="{ row }">
            <el-tag
              v-for="t in parseTags(row.tags)"
              :key="t"
              size="small"
              style="margin-right: 4px"
            >
              {{ t }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="日期" width="120">
          <template #default="{ row }">{{ row.createdAt?.split('T')[0] }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteRecord(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          :page-size="20"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadRecords"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="showDialog"
      :title="editingId ? '编辑学习记录' : '添加学习记录'"
      width="600px"
    >
      <el-form :model="recordForm" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="recordForm.title" placeholder="学习主题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            v-model="recordForm.content"
            type="textarea"
            rows="5"
            placeholder="学习内容详细记录"
          />
        </el-form-item>
        <el-form-item label="时长">
          <el-input-number v-model="recordForm.durationMinutes" :min="0" :step="15" /> 分钟
        </el-form-item>
        <el-form-item label="来源">
          <el-input v-model="recordForm.source" placeholder="文档/视频/项目/面试准备" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select
            v-model="recordForm.tagInput"
            multiple
            allow-create
            filterable
            placeholder="输入标签后回车"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitRecord">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { learningApi, type LearningRecord } from '@/api/modules/learning'
import { ElMessage, ElMessageBox } from 'element-plus'

const records = ref<LearningRecord[]>([])
const stats = ref<any>(null)
const total = ref(0)
const page = ref(1)
const showDialog = ref(false)
const editingId = ref<number | null>(null)
const selectedIds = ref<number[]>([])

const filters = reactive({
  keyword: '',
  source: '',
  dateRange: [] as string[]
})
const recordForm = reactive({
  title: '',
  content: '',
  durationMinutes: 30,
  source: '',
  tagInput: [] as string[]
})

const parseTags = (tagsStr: string | undefined): string[] => {
  if (!tagsStr) return []
  try {
    const parsed = JSON.parse(tagsStr)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return tagsStr.split(',').map(s => s.trim()).filter(Boolean)
  }
}

const buildParams = () => {
  const params: any = { page: page.value, size: 20 }
  if (filters.keyword) params.keyword = filters.keyword
  if (filters.source) params.source = filters.source
  if (filters.dateRange && filters.dateRange.length === 2) {
    params.start = filters.dateRange[0]
    params.end = filters.dateRange[1]
  }
  return params
}

const loadRecords = async () => {
  try {
    const res = await learningApi.list(buildParams()) as any
    records.value = res.records || []
    total.value = res.total || 0
  } catch {}
}

const loadStats = async () => {
  try {
    stats.value = await learningApi.stats()
  } catch {}
}

const search = () => {
  page.value = 1
  loadRecords()
}

const resetFilters = () => {
  filters.keyword = ''
  filters.source = ''
  filters.dateRange = []
  search()
}

const openAddDialog = () => {
  editingId.value = null
  Object.assign(recordForm, {
    title: '',
    content: '',
    durationMinutes: 30,
    source: '',
    tagInput: []
  })
  showDialog.value = true
}

const openEditDialog = (record: LearningRecord) => {
  editingId.value = record.id
  Object.assign(recordForm, {
    title: record.title,
    content: record.content || '',
    durationMinutes: record.durationMinutes || 0,
    source: record.source || '',
    tagInput: parseTags(record.tags)
  })
  showDialog.value = true
}

const submitRecord = async () => {
  if (!recordForm.title.trim()) {
    ElMessage.warning('请填写标题')
    return
  }
  const payload = {
    title: recordForm.title,
    content: recordForm.content,
    durationMinutes: recordForm.durationMinutes,
    source: recordForm.source,
    tags: recordForm.tagInput
  }
  if (editingId.value) {
    await learningApi.update(editingId.value, payload)
    ElMessage.success('已更新')
  } else {
    await learningApi.create(payload)
    ElMessage.success('添加成功')
  }
  showDialog.value = false
  await loadRecords()
  await loadStats()
}

const deleteRecord = async (record: LearningRecord) => {
  await ElMessageBox.confirm(`确定删除「${record.title}」？`, '提示', {
    type: 'warning'
  })
  await learningApi.delete(record.id)
  ElMessage.success('已删除')
  await loadRecords()
  await loadStats()
}

const onSelectionChange = (rows: LearningRecord[]) => {
  selectedIds.value = rows.map(r => r.id)
}

const batchDelete = async () => {
  await ElMessageBox.confirm(
    `确定删除选中的 ${selectedIds.value.length} 条记录？`,
    '批量删除',
    { type: 'warning' }
  )
  await Promise.all(selectedIds.value.map(id => learningApi.delete(id)))
  ElMessage.success('已批量删除')
  selectedIds.value = []
  await loadRecords()
  await loadStats()
}

onMounted(() => {
  loadRecords()
  loadStats()
})
</script>

<style scoped>
.learnings-page { max-width: 1200px; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.header-actions { display: flex; gap: 8px; }
.stats-row { margin-bottom: 20px; }
.stat-card { text-align: center; padding: 10px; }
.stat-num { font-size: 28px; font-weight: bold; color: #409eff; }
.stat-label { color: #666; margin-top: 4px; font-size: 13px; }
.filter-bar { margin-bottom: 16px; }
.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.empty-state { padding: 40px 0; }
.content-preview {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: #666;
  font-size: 13px;
}
</style>
