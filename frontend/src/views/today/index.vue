<template>
  <div class="tasks-page">
    <div class="page-header">
      <h2>任务管理</h2>
      <el-button type="primary" :icon="Plus" @click="openAddDialog">添加任务</el-button>
    </div>

    <el-row :gutter="16" class="stats-row">
      <el-col :xs="6" :sm="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-num todo">{{ todayStats.todo }}</div>
            <div class="stat-label">今日待办</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="6" :sm="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-num doing">{{ todayStats.doing }}</div>
            <div class="stat-label">今日进行</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="6" :sm="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-num done">{{ todayStats.done }}</div>
            <div class="stat-label">今日完成</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="6" :sm="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-num all">{{ allCount }}</div>
            <div class="stat-label">总任务数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="section-header">
          <span class="section-title">
            <el-icon color="#409eff"><Calendar /></el-icon>
            今日任务 ({{ today }})
          </span>
          <el-button text type="primary" size="small" @click="loadAllData">刷新</el-button>
        </div>
      </template>

      <div v-if="todayTasks.length === 0" class="empty-state">
        <el-empty description="今日暂无任务，点击右上角添加" />
      </div>
      <div v-else class="task-grid">
        <div
          v-for="task in todayTasks"
          :key="task.id"
          class="task-card"
          :class="['status-' + task.status, 'priority-' + task.priority]"
        >
          <div class="task-card-main">
            <div class="task-card-header">
              <el-dropdown trigger="click" @command="(status: string) => changeStatus(task, status)">
                <el-tag
                  :type="statusType(task.status)"
                  size="small"
                  effect="dark"
                  class="status-chip"
                >
                  <el-icon class="chip-icon"><CircleCheck v-if="task.status==='done'"/><Loading v-else-if="task.status==='doing'"/><Close v-else-if="task.status==='abandoned'"/><Clock v-else/></el-icon>
                  {{ statusLabel(task.status) }}
                  <el-icon class="chip-caret"><CaretBottom /></el-icon>
                </el-tag>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="todo" :disabled="task.status==='todo'">
                      <el-icon><Clock /></el-icon> 待办
                    </el-dropdown-item>
                    <el-dropdown-item command="doing" :disabled="task.status==='doing'">
                      <el-icon><Loading /></el-icon> 进行中
                    </el-dropdown-item>
                    <el-dropdown-item command="done" :disabled="task.status==='done'">
                      <el-icon><CircleCheck /></el-icon> 已完成
                    </el-dropdown-item>
                    <el-dropdown-item command="abandoned" :disabled="task.status==='abandoned'" divided>
                      <el-icon><Close /></el-icon> 放弃
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <span
                class="task-title"
                :class="{ done: task.status === 'done' }"
              >{{ task.title }}</span>
              <el-tag :type="priorityType(task.priority)" size="small">
                {{ priorityLabel(task.priority) }}
              </el-tag>
            </div>
            <div v-if="task.description" class="task-desc">{{ task.description }}</div>
            <div class="task-meta">
              <span v-if="task.plannedMinutes" class="meta-item">
                <el-icon><Clock /></el-icon>
                预计 {{ formatMinutes(task.plannedMinutes) }}
              </span>
              <span v-if="task.actualMinutes" class="meta-item">
                <el-icon color="#67c23a"><Check /></el-icon>
                实际 {{ formatMinutes(task.actualMinutes) }}
              </span>
            </div>
          </div>
          <div class="task-card-actions">
            <el-button size="small" @click="openEditDialog(task)">编辑</el-button>
            <el-button size="small" type="danger" @click="confirmDelete(task)">删除</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="section-header">
          <span class="section-title">
            <el-icon color="#67c23a"><List /></el-icon>
            全部任务
          </span>
          <div class="header-filters">
            <el-select v-model="filters.status" clearable placeholder="状态" size="small" style="width: 110px">
              <el-option label="待办" value="todo" />
              <el-option label="进行中" value="doing" />
              <el-option label="已完成" value="done" />
              <el-option label="已放弃" value="abandoned" />
            </el-select>
            <el-select v-model="filters.priority" clearable placeholder="优先级" size="small" style="width: 100px">
              <el-option label="低" value="low" />
              <el-option label="中" value="medium" />
              <el-option label="高" value="high" />
              <el-option label="紧急" value="urgent" />
            </el-select>
            <el-button size="small" type="primary" @click="loadAllTasks">搜索</el-button>
          </div>
        </div>
      </template>

      <div v-if="allTasks.length === 0" class="empty-state">
        <el-empty description="暂无任务" />
      </div>
      <el-table v-else :data="pagedAllTasks" stripe style="width: 100%">
        <el-table-column label="日期" width="110">
          <template #default="{ row }">{{ row.plannedDate || '—' }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-dropdown trigger="click" @command="(status: string) => changeStatus(row, status)">
              <el-tag :type="statusType(row.status)" size="small" class="status-chip-sm">
                {{ statusLabel(row.status) }}
                <el-icon class="chip-caret"><CaretBottom /></el-icon>
              </el-tag>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="todo" :disabled="row.status==='todo'">
                    <el-icon><Clock /></el-icon> 待办
                  </el-dropdown-item>
                  <el-dropdown-item command="doing" :disabled="row.status==='doing'">
                    <el-icon><Loading /></el-icon> 进行中
                  </el-dropdown-item>
                  <el-dropdown-item command="done" :disabled="row.status==='done'">
                    <el-icon><CircleCheck /></el-icon> 已完成
                  </el-dropdown-item>
                  <el-dropdown-item command="abandoned" :disabled="row.status==='abandoned'" divided>
                    <el-icon><Close /></el-icon> 放弃
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
        <el-table-column label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small">
              {{ priorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预计" width="100">
          <template #default="{ row }">
            <span v-if="row.plannedMinutes">{{ formatMinutes(row.plannedMinutes) }}</span>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="confirmDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="allPage"
          :page-size="10"
          :total="allTasks.length"
          layout="total, prev, pager, next"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="showDialog"
      :title="editingId ? '编辑任务' : '添加任务'"
      width="560px"
    >
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="taskForm.title" placeholder="任务标题" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            rows="3"
            placeholder="详细描述（可选）"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="taskForm.priority" style="width: 100%">
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="紧急" value="urgent" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计耗时">
          <div class="time-slider">
            <div class="time-display">
              <span class="time-num">{{ taskForm.hours }}</span>
              <span class="time-unit">小时</span>
              <span class="time-num">{{ String(taskForm.minutes).padStart(2, '0') }}</span>
              <span class="time-unit">分钟</span>
            </div>
            <div class="time-controls">
              <div class="time-row">
                <span class="time-row-label">小时</span>
                <el-slider
                  v-model="taskForm.hours"
                  :min="0"
                  :max="12"
                  :step="1"
                  show-stops
                  :marks="hourMarks"
                  class="time-slider-control"
                />
              </div>
              <div class="time-row">
                <span class="time-row-label">分钟</span>
                <el-slider
                  v-model="taskForm.minutes"
                  :min="0"
                  :max="59"
                  :step="5"
                  show-stops
                  :marks="minuteMarks"
                  class="time-slider-control"
                />
              </div>
            </div>
            <div class="time-presets">
              <el-button size="small" @click="setTime(0, 15)">15 分</el-button>
              <el-button size="small" @click="setTime(0, 30)">30 分</el-button>
              <el-button size="small" @click="setTime(1, 0)">1 小时</el-button>
              <el-button size="small" @click="setTime(2, 0)">2 小时</el-button>
              <el-button size="small" @click="setTime(0, 0)">0</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="计划日期">
          <el-date-picker
            v-model="taskForm.plannedDate"
            type="date"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker
            v-model="taskForm.dueAt"
            type="date"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitTask">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { taskApi, type Task } from '@/api/modules/task'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const today = computed(() => dayjs().format('YYYY-MM-DD'))
const todayTasks = ref<Task[]>([])
const allTasks = ref<Task[]>([])
const allPage = ref(1)

const filters = reactive({ status: '', priority: '' })

const showDialog = ref(false)
const editingId = ref<number | null>(null)
const taskForm = reactive({
  title: '',
  description: '',
  priority: 'medium',
  hours: 0,
  minutes: 30,
  plannedDate: today.value,
  dueAt: ''
})

const hourMarks = computed(() => {
  const marks: Record<number, any> = { 0: '0', 3: '3h', 6: '6h', 9: '9h', 12: '12h' }
  return marks
})
const minuteMarks = computed(() => ({ 0: '0', 15: '15', 30: '30', 45: '45', 59: '59' }))

const setTime = (h: number, m: number) => {
  taskForm.hours = h
  taskForm.minutes = m
}

const todayStats = computed(() => ({
  todo: todayTasks.value.filter(t => t.status === 'todo').length,
  doing: todayTasks.value.filter(t => t.status === 'doing').length,
  done: todayTasks.value.filter(t => t.status === 'done').length
}))

const allCount = computed(() => allTasks.value.length)
const pagedAllTasks = computed(() => {
  const start = (allPage.value - 1) * 10
  return allTasks.value.slice(start, start + 10)
})

const formatMinutes = (m: number) => {
  if (!m) return ''
  const h = Math.floor(m / 60)
  const min = m % 60
  if (h > 0 && min > 0) return `${h}小时${min}分`
  if (h > 0) return `${h}小时`
  return `${min}分钟`
}

const loadTodayTasks = async () => {
  try {
    todayTasks.value = await taskApi.today() as Task[]
  } catch (e) { /* ignore */ }
}

const loadAllTasks = async () => {
  try {
    const params: any = { page: 1, size: 100 }
    if (filters.status) params.status = filters.status
    if (filters.priority) params.priority = filters.priority
    const res = await taskApi.list(params) as any
    allTasks.value = res.records || []
    allPage.value = 1
  } catch (e) { /* ignore */ }
}

const loadAllData = async () => {
  await Promise.all([loadTodayTasks(), loadAllTasks()])
}

const openAddDialog = () => {
  editingId.value = null
  Object.assign(taskForm, {
    title: '',
    description: '',
    priority: 'medium',
    hours: 0,
    minutes: 30,
    plannedDate: today.value,
    dueAt: ''
  })
  showDialog.value = true
}

const openEditDialog = (task: Task) => {
  editingId.value = task.id
  const total = task.plannedMinutes || 0
  Object.assign(taskForm, {
    title: task.title,
    description: task.description || '',
    priority: task.priority || 'medium',
    hours: Math.floor(total / 60),
    minutes: total % 60,
    plannedDate: task.plannedDate || today.value,
    dueAt: task.dueAt || ''
  })
  showDialog.value = true
}

const submitTask = async () => {
  if (!taskForm.title.trim()) {
    ElMessage.warning('请填写标题')
    return
  }
  const payload = {
    title: taskForm.title,
    description: taskForm.description,
    priority: taskForm.priority,
    plannedMinutes: taskForm.hours * 60 + taskForm.minutes,
    plannedDate: taskForm.plannedDate,
    dueAt: taskForm.dueAt
  }
  try {
    if (editingId.value) {
      await taskApi.update(editingId.value, payload)
      ElMessage.success('已更新')
    } else {
      await taskApi.create(payload)
      ElMessage.success('添加成功')
    }
    showDialog.value = false
    await loadAllData()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  }
}

const toggleTask = async (task: Task) => {
  if (task.status === 'done') return
  try {
    if (task.status === 'todo') {
      await taskApi.start(task.id)
    } else if (task.status === 'doing') {
      await taskApi.finish(task.id)
    }
    await loadAllData()
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败')
  }
}

const changeStatus = async (task: Task, status: string) => {
  if (task.status === status) return
  // 危险操作（done / abandoned）要求二次确认，避免误点
  const dangerous = status === 'done' || status === 'abandoned'
  if (dangerous) {
    try {
      await ElMessageBox.confirm(
        `确认将「${task.title}」标记为「${statusLabel(status)}」？`,
        '确认操作',
        { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' }
      )
    } catch { return }
  }
  try {
    await taskApi.updateStatus(task.id, status)
    ElMessage.success(`已切换为「${statusLabel(status)}」`)
    await loadAllData()
  } catch (e: any) {
    ElMessage.error(e?.message || '切换失败')
  }
}

const startTask = async (task: Task) => {
  try { await taskApi.start(task.id); await loadAllData() }
  catch (e: any) { ElMessage.error(e?.message || '开始失败') }
}

const finishTask = async (task: Task) => {
  try { await taskApi.finish(task.id); await loadAllData() }
  catch (e: any) { ElMessage.error(e?.message || '完成失败') }
}

const deleteTask = async (task: Task) => {
  try {
    await ElMessageBox.confirm(`确定删除「${task.title}」？`, '提示', { type: 'warning' })
    await taskApi.delete(task.id)
    ElMessage.success('已删除')
    await loadAllData()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error(e?.message || '删除失败')
  }
}

const confirmDelete = deleteTask

const statusType = (s: string) => ({ todo: 'info', doing: 'warning', done: 'success', abandoned: 'info' }[s] || 'info')
const statusLabel = (s: string) => ({ todo: '待办', doing: '进行中', done: '已完成', abandoned: '已放弃' }[s] || s)
const priorityType = (p: string) => ({ urgent: 'danger', high: 'warning', medium: 'info', low: 'info' }[p] || 'info')
const priorityLabel = (p: string) => ({ urgent: '紧急', high: '高', medium: '中', low: '低' }[p] || p)

onMounted(loadAllData)
</script>

<style scoped>
.tasks-page { max-width: 1200px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 22px; font-weight: 600; }

.stats-row { margin-bottom: 20px; }
.stat-card { text-align: center; padding: 10px; }
.stat-num { font-size: 32px; font-weight: bold; line-height: 1.2; }
.stat-num.todo { color: #909399; }
.stat-num.doing { color: #409eff; }
.stat-num.done { color: #67c23a; }
.stat-num.all { color: #303133; }
.stat-label { color: #666; margin-top: 4px; font-size: 13px; }

.section-card { margin-bottom: 16px; border-radius: 8px; }
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 15px;
}
.header-filters { display: flex; gap: 8px; }

.empty-state { padding: 40px 0; }
.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 12px;
}

.status-chip {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  font-weight: 600;
  transition: all 0.15s;
}
.status-chip:hover { opacity: 0.85; transform: translateY(-1px); }
.chip-icon { font-size: 12px; }
.chip-caret { font-size: 10px; opacity: 0.7; margin-left: 2px; }
.status-chip-sm {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.status-chip-sm:hover { opacity: 0.85; }

.task-card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-left: 4px solid #dcdfe6;
  border-radius: 6px;
  transition: all 0.2s;
  gap: 10px;
}
.task-card:hover {
  border-color: #b3d8ff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.08);
}
.task-card.priority-urgent { border-left-color: #f56c6c; }
.task-card.priority-high { border-left-color: #e6a23c; }
.task-card.priority-medium { border-left-color: #409eff; }
.task-card.priority-low { border-left-color: #909399; }
.task-card.status-done { background: #f9faf9; opacity: 0.85; }
.task-card.status-doing { background: #f0f9ff; }

.task-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.task-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  flex: 1;
  word-break: break-all;
}
.task-title.done { text-decoration: line-through; color: #909399; }
.task-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin: 4px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.task-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: #909399;
  flex-wrap: wrap;
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.task-card-actions { display: flex; gap: 6px; flex-wrap: wrap; }

.time-slider {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}
.time-display {
  display: flex;
  align-items: baseline;
  gap: 6px;
  padding: 8px 14px;
  background: linear-gradient(135deg, #f0f9ff 0%, #ecf5ff 100%);
  border-radius: 6px;
  border: 1px solid #d9ecff;
}
.time-num {
  font-size: 24px;
  font-weight: 700;
  color: #409eff;
  font-family: 'JetBrains Mono', Consolas, monospace;
}
.time-unit {
  font-size: 13px;
  color: #606266;
}
.time-controls { display: flex; flex-direction: column; gap: 8px; }
.time-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.time-row-label {
  width: 40px;
  font-size: 13px;
  color: #606266;
  flex-shrink: 0;
}
.time-slider-control { flex: 1; }
.time-presets { display: flex; gap: 6px; flex-wrap: wrap; }

.pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
