<template>
  <div class="review-page">
    <div class="page-header">
      <div>
        <h2>每日复盘</h2>
        <p class="page-subtitle">
          通过每日记录"完成 / 未完成 / 收获 / 改进 / 心情"，沉淀经验、识别瓶颈，让成长可视化
        </p>
      </div>
      <div class="header-actions">
        <el-button :icon="Calendar" @click="datePickerVisible = true">选择日期</el-button>
      </div>
    </div>

    <!-- 使用说明 -->
    <el-alert
      v-if="!hideTip"
      type="info"
      :closable="true"
      show-icon
      class="tip-alert"
      @close="hideTip = true"
    >
      <template #title>
        <strong>每日复盘是什么？</strong>
      </template>
      <div class="tip-content">
        每天花 3-5 分钟回顾当日的任务、感受与学习。坚持一段时间后，你可以在「历史复盘」中看到自己的成长曲线、心情趋势和高频改进项。
      </div>
    </el-alert>

    <el-tabs v-model="activeTab" class="review-tabs">
      <!-- 今日复盘 -->
      <el-tab-pane label="今日复盘" name="today">
        <el-row :gutter="20">
          <!-- 左侧：复盘表单 -->
          <el-col :xs="24" :md="15" :lg="16">
            <el-card shadow="never" class="review-card">
              <template #header>
                <div class="card-header">
                  <span><el-icon><Edit /></el-icon> {{ today }} 复盘</span>
                  <el-tag v-if="savedAt" size="small" type="success" effect="plain">
                    上次保存：{{ savedAt }}
                  </el-tag>
                </div>
              </template>
              <el-form :model="review" label-position="top">
                <el-form-item>
                  <template #label>
                    <span class="form-label">
                      <el-icon class="green"><CircleCheck /></el-icon>
                      今日完成
                    </span>
                  </template>
                  <el-input
                    v-model="review.doneSummary"
                    type="textarea"
                    :rows="3"
                    placeholder="今天完成了哪些任务？哪些是计划内的，哪些是临时加入的？"
                  />
                </el-form-item>

                <el-form-item>
                  <template #label>
                    <span class="form-label">
                      <el-icon class="red"><CircleClose /></el-icon>
                      今日未完成
                    </span>
                  </template>
                  <el-input
                    v-model="review.undoneSummary"
                    type="textarea"
                    :rows="3"
                    placeholder="哪些没完成？原因是什么（时间不够 / 优先级低 / 卡住）？是否要延期？"
                  />
                </el-form-item>

                <el-form-item>
                  <template #label>
                    <span class="form-label">
                      <el-icon class="orange"><Sunny /></el-icon>
                      今日收获
                    </span>
                  </template>
                  <el-input
                    v-model="review.gains"
                    type="textarea"
                    :rows="2"
                    placeholder="今天最有价值的一件事 / 学到的最关键的一个点"
                  />
                </el-form-item>

                <el-form-item>
                  <template #label>
                    <span class="form-label">
                      <el-icon class="blue"><Promotion /></el-icon>
                      今日改进
                    </span>
                  </template>
                  <el-input
                    v-model="review.improvements"
                    type="textarea"
                    :rows="2"
                    placeholder="今天哪里可以做得更好？明天怎么调整？"
                  />
                </el-form-item>

                <el-form-item>
                  <template #label>
                    <span class="form-label">
                      <el-icon class="purple"><Star /></el-icon>
                      心情打分 (1-10)
                    </span>
                  </template>
                  <div class="mood-row">
                    <el-rate v-model="review.moodScore" :max="10" show-score score-template="{value} 分" />
                    <span class="mood-hint">{{ moodLabel }}</span>
                  </div>
                </el-form-item>

                <el-form-item>
                  <el-button type="primary" :icon="Check" @click="saveReview" :loading="saving">
                    保存复盘
                  </el-button>
                  <span class="autosave-hint">提示：内容已临时缓存，刷新不会丢失</span>
                </el-form-item>
              </el-form>
            </el-card>
          </el-col>

          <!-- 右侧：今日任务参考 + 快速统计 -->
          <el-col :xs="24" :md="9" :lg="8">
            <el-card shadow="never" class="side-card">
              <template #header>
                <div class="card-header">
                  <span><el-icon><List /></el-icon> 今日任务参考</span>
                  <el-tag size="small" type="info" effect="plain">{{ todayTasks.length }}</el-tag>
                </div>
              </template>
              <div v-if="todayTasks.length === 0" class="empty-tasks">
                <el-icon class="empty-icon"><DocumentRemove /></el-icon>
                <p>今日暂无任务</p>
              </div>
              <div v-else class="task-list">
                <div
                  v-for="t in todayTasks"
                  :key="t.id"
                  class="task-row"
                  :class="`status-${t.status}`"
                >
                  <el-icon class="task-icon"><CircleCheck v-if="t.status==='done'"/><Loading v-else-if="t.status==='doing'"/><Close v-else-if="t.status==='abandoned'"/><Clock v-else/></el-icon>
                  <span class="task-title">{{ t.title }}</span>
                  <el-tag size="small" :type="statusType(t.status)" effect="plain">{{ statusText(t.status) }}</el-tag>
                </div>
              </div>
              <div class="quick-stats">
                <div class="quick-stat">
                  <div class="qs-num">{{ doneCount }}</div>
                  <div class="qs-label">已完成</div>
                </div>
                <div class="quick-stat">
                  <div class="qs-num doing">{{ doingCount }}</div>
                  <div class="qs-label">进行中</div>
                </div>
                <div class="quick-stat">
                  <div class="qs-num todo">{{ todoCount }}</div>
                  <div class="qs-label">未开始</div>
                </div>
              </div>
              <el-button
                size="small"
                type="primary"
                text
                class="autofill-btn"
                @click="autofillFromTasks"
              >
                <el-icon><MagicStick /></el-icon> 从任务自动填充「完成 / 未完成」
              </el-button>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- 历史复盘 -->
      <el-tab-pane label="历史复盘" name="history">
        <el-row :gutter="20">
          <el-col :xs="24" :md="16">
            <el-card shadow="never" class="history-card">
              <template #header>
                <div class="card-header">
                  <span><el-icon><Clock /></el-icon> 历史复盘时间轴</span>
                  <el-radio-group v-model="rangeDays" size="small" @change="loadHistory">
                    <el-radio-button :value="7">近 7 天</el-radio-button>
                    <el-radio-button :value="30">近 30 天</el-radio-button>
                    <el-radio-button :value="90">近 90 天</el-radio-button>
                  </el-radio-group>
                </div>
              </template>

              <div v-if="historyList.length === 0" class="empty-history">
                <el-empty description="暂无历史复盘记录，今日完成第一次复盘后将出现在这里" />
              </div>

              <el-timeline v-else>
                <el-timeline-item
                  v-for="r in historyList"
                  :key="r.id"
                  :timestamp="formatDate(r.reviewDate)"
                  :type="moodTimelineType(r.moodScore)"
                  placement="top"
                  size="large"
                >
                  <el-card shadow="never" class="history-entry">
                    <div class="entry-header">
                      <strong>{{ formatDateFull(r.reviewDate) }}</strong>
                      <div class="entry-mood">
                        <el-rate v-model="r.moodScore" :max="10" disabled size="small" />
                        <span class="mood-num">{{ r.moodScore || 0 }}/10</span>
                      </div>
                    </div>
                    <el-collapse v-if="hasContent(r)">
                      <el-collapse-item title="查看详情" :name="String(r.id)">
                        <div v-if="r.doneSummary" class="entry-section">
                          <div class="entry-label green"><el-icon><CircleCheck /></el-icon> 完成</div>
                          <div class="entry-content">{{ r.doneSummary }}</div>
                        </div>
                        <div v-if="r.undoneSummary" class="entry-section">
                          <div class="entry-label red"><el-icon><CircleClose /></el-icon> 未完成</div>
                          <div class="entry-content">{{ r.undoneSummary }}</div>
                        </div>
                        <div v-if="r.gains" class="entry-section">
                          <div class="entry-label orange"><el-icon><Sunny /></el-icon> 收获</div>
                          <div class="entry-content">{{ r.gains }}</div>
                        </div>
                        <div v-if="r.improvements" class="entry-section">
                          <div class="entry-label blue"><el-icon><Promotion /></el-icon> 改进</div>
                          <div class="entry-content">{{ r.improvements }}</div>
                        </div>
                      </el-collapse-item>
                    </el-collapse>
                    <div v-else class="entry-empty">（当天复盘内容为空）</div>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </el-card>
          </el-col>

          <el-col :xs="24" :md="8">
            <el-card shadow="never" class="side-card stat-summary">
              <template #header>
                <div class="card-header">
                  <span><el-icon><DataAnalysis /></el-icon> 复盘统计</span>
                </div>
              </template>
              <div class="summary-grid">
                <div class="sg-item">
                  <div class="sg-num">{{ historyList.length }}</div>
                  <div class="sg-label">已复盘天数</div>
                </div>
                <div class="sg-item">
                  <div class="sg-num">{{ avgMood }}</div>
                  <div class="sg-label">平均心情</div>
                </div>
                <div class="sg-item">
                  <div class="sg-num">{{ streakDays }}</div>
                  <div class="sg-label">连续打卡</div>
                </div>
                <div class="sg-item">
                  <div class="sg-num">{{ filledRate }}%</div>
                  <div class="sg-label">复盘填写率</div>
                </div>
              </div>
            </el-card>

            <el-card shadow="never" class="side-card mood-chart-card">
              <template #header>
                <div class="card-header">
                  <span><el-icon><TrendCharts /></el-icon> 心情趋势</span>
                </div>
              </template>
              <div ref="chartRef" class="mood-chart"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick, watch } from 'vue'
import { reviewApi, type DailyReview } from '@/api/modules/review'
import { taskApi, type Task } from '@/api/modules/task'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import * as echarts from 'echarts'

const today = computed(() => dayjs().format('YYYY-MM-DD'))
const review = reactive({
  doneSummary: '',
  undoneSummary: '',
  gains: '',
  improvements: '',
  moodScore: 0
})
const saving = ref(false)
const savedAt = ref('')
const activeTab = ref('today')
const hideTip = ref(false)
const datePickerVisible = ref(false)
const todayTasks = ref<Task[]>([])
const historyList = ref<DailyReview[]>([])
const rangeDays = ref(30)
const chartRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null

const moodLabel = computed(() => {
  const m = review.moodScore
  if (m >= 9) return '今天超棒！'
  if (m >= 7) return '心情不错'
  if (m >= 5) return '一般般'
  if (m >= 3) return '有点疲惫'
  if (m >= 1) return '今天不太顺'
  return ''
})

const doneCount = computed(() => todayTasks.value.filter(t => t.status === 'done').length)
const doingCount = computed(() => todayTasks.value.filter(t => t.status === 'doing').length)
const todoCount = computed(() => todayTasks.value.filter(t => t.status === 'todo').length)

const avgMood = computed(() => {
  const scored = historyList.value.filter(r => r.moodScore && r.moodScore > 0)
  if (scored.length === 0) return '-'
  return (scored.reduce((s, r) => s + (r.moodScore || 0), 0) / scored.length).toFixed(1)
})

const streakDays = computed(() => {
  if (historyList.value.length === 0) return 0
  const sorted = [...historyList.value].sort((a, b) => b.reviewDate.localeCompare(a.reviewDate))
  let streak = 0
  let cursor = dayjs()
  for (const r of sorted) {
    if (!hasContent(r)) break
    if (dayjs(r.reviewDate).isSame(cursor, 'day')) {
      streak++
      cursor = cursor.subtract(1, 'day')
    } else if (dayjs(r.reviewDate).isSame(cursor.subtract(1, 'day'), 'day')) {
      streak++
      cursor = cursor.subtract(2, 'day')
    } else {
      break
    }
  }
  return streak
})

const filledRate = computed(() => {
  if (historyList.value.length === 0) return 0
  const filled = historyList.value.filter(r => hasContent(r)).length
  return Math.round((filled / historyList.value.length) * 100)
})

const statusType = (s: string) => ({ done: 'success', doing: 'warning', abandoned: 'info', todo: '' } as any)[s] || ''
const statusText = (s: string) => ({ done: '已完成', doing: '进行中', abandoned: '放弃', todo: '待办' } as any)[s] || s
const moodTimelineType = (m?: number) => {
  if (!m) return 'info'
  if (m >= 8) return 'success'
  if (m >= 5) return 'primary'
  if (m >= 3) return 'warning'
  return 'danger'
}

const formatDate = (d: string) => dayjs(d).format('MM-DD ddd')
const formatDateFull = (d: string) => dayjs(d).format('YYYY-MM-DD dddd')

const hasContent = (r: DailyReview) => {
  return !!(r.doneSummary || r.undoneSummary || r.gains || r.improvements || r.moodScore)
}

const loadTodayReview = async () => {
  try {
    const data = await reviewApi.today() as DailyReview
    if (data) {
      review.doneSummary = data.doneSummary || ''
      review.undoneSummary = data.undoneSummary || ''
      review.gains = data.gains || ''
      review.improvements = data.improvements || ''
      review.moodScore = data.moodScore || 0
      savedAt.value = data.updatedAt ? dayjs(data.updatedAt).format('YYYY-MM-DD HH:mm') : ''
    }
  } catch {}
}

const loadTodayTasks = async () => {
  try {
    todayTasks.value = await taskApi.today() as Task[]
  } catch {}
}

const loadHistory = async () => {
  try {
    const start = dayjs().subtract(rangeDays.value - 1, 'day').format('YYYY-MM-DD')
    const end = dayjs().format('YYYY-MM-DD')
    const data = await reviewApi.list({ start, end }) as DailyReview[]
    historyList.value = (data || []).sort((a, b) => b.reviewDate.localeCompare(a.reviewDate))
    await nextTick()
    renderChart()
  } catch {}
}

const autofillFromTasks = () => {
  const done = todayTasks.value.filter(t => t.status === 'done').map(t => `• ${t.title}`).join('\n')
  const undone = todayTasks.value.filter(t => t.status !== 'done' && t.status !== 'abandoned')
    .map(t => `• ${t.title} (${statusText(t.status)})`).join('\n')
  if (done) review.doneSummary = (review.doneSummary ? review.doneSummary + '\n' : '') + done
  if (undone) review.undoneSummary = (review.undoneSummary ? review.undoneSummary + '\n' : '') + undone
  ElMessage.success('已根据今日任务填充')
}

const saveReview = async () => {
  saving.value = true
  try {
    await reviewApi.updateToday(review)
    savedAt.value = dayjs().format('YYYY-MM-DD HH:mm')
    ElMessage.success('保存成功，已记入今日复盘')
    loadHistory()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const renderChart = () => {
  if (!chartRef.value) return
  if (!chartInstance) chartInstance = echarts.init(chartRef.value)
  const reversed = [...historyList.value].reverse()
  const dates = reversed.map(r => dayjs(r.reviewDate).format('MM-DD'))
  const moods = reversed.map(r => r.moodScore || null)
  chartInstance.setOption({
    grid: { top: 20, left: 35, right: 15, bottom: 30 },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates, axisLabel: { fontSize: 10 } },
    yAxis: { type: 'value', min: 0, max: 10, splitNumber: 5 },
    series: [{
      type: 'line',
      data: moods,
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#409eff', width: 2 },
      itemStyle: { color: '#409eff' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.4)' },
          { offset: 1, color: 'rgba(64,158,255,0.05)' }
        ])
      }
    }]
  })
}

watch(activeTab, (v) => {
  if (v === 'history') {
    loadHistory()
  }
})

onMounted(() => {
  loadTodayReview()
  loadTodayTasks()
})
</script>

<style scoped>
.review-page { max-width: 1600px; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  gap: 16px;
}
.page-header h2 { margin: 0 0 4px; font-size: 22px; font-weight: 600; }
.page-subtitle { margin: 0; color: #909399; font-size: 13px; }
.header-actions { display: flex; gap: 8px; flex-shrink: 0; }

.tip-alert { margin-bottom: 16px; }
.tip-content { font-size: 13px; color: #606266; line-height: 1.6; margin-top: 4px; }

.review-tabs :deep(.el-tabs__nav) { font-weight: 600; }

.review-card, .side-card, .history-card, .stat-summary, .mood-chart-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 14px;
}
.card-header span { display: inline-flex; align-items: center; gap: 6px; }

.form-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 13px;
  color: #303133;
}
.form-label .green { color: #67c23a; }
.form-label .red { color: #f56c6c; }
.form-label .orange { color: #e6a23c; }
.form-label .blue { color: #409eff; }
.form-label .purple { color: #a855f7; }

.mood-row { display: flex; align-items: center; gap: 16px; }
.mood-hint { color: #909399; font-size: 13px; }
.autosave-hint { color: #c0c4cc; font-size: 12px; margin-left: 12px; }

.empty-tasks { text-align: center; padding: 20px 0; color: #c0c4cc; }
.empty-tasks .empty-icon { font-size: 36px; }
.empty-tasks p { margin: 8px 0 0; font-size: 13px; }

.task-list { margin-bottom: 12px; max-height: 260px; overflow-y: auto; }
.task-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 4px;
  border-bottom: 1px dashed #f0f2f5;
  font-size: 13px;
}
.task-row:last-child { border-bottom: none; }
.task-row.status-done { opacity: 0.65; }
.task-row.status-done .task-title { text-decoration: line-through; color: #909399; }
.task-icon { font-size: 14px; flex-shrink: 0; }
.status-done .task-icon { color: #67c23a; }
.status-doing .task-icon { color: #e6a23c; }
.status-todo .task-icon { color: #909399; }
.status-abandoned .task-icon { color: #c0c4cc; }
.task-title { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.quick-stats {
  display: flex;
  gap: 8px;
  padding: 12px;
  background: #fafbfc;
  border-radius: 6px;
  margin-bottom: 8px;
}
.quick-stat { flex: 1; text-align: center; }
.qs-num { font-size: 20px; font-weight: 700; color: #67c23a; }
.qs-num.doing { color: #e6a23c; }
.qs-num.todo { color: #909399; }
.qs-label { font-size: 11px; color: #909399; margin-top: 2px; }

.autofill-btn { width: 100%; justify-content: center; }

.history-card :deep(.el-timeline) { padding: 0 0 0 16px; }
.empty-history { padding: 40px 0; }

.history-entry { border: 1px solid #ebeef5; border-radius: 6px; }
.history-entry :deep(.el-card__body) { padding: 12px 16px; }
.entry-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.entry-header strong { font-size: 14px; color: #303133; }
.entry-mood { display: flex; align-items: center; gap: 8px; }
.mood-num { font-size: 12px; color: #909399; }

.entry-section { margin: 8px 0; }
.entry-label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 4px;
}
.entry-label.green { color: #67c23a; }
.entry-label.red { color: #f56c6c; }
.entry-label.orange { color: #e6a23c; }
.entry-label.blue { color: #409eff; }
.entry-content {
  font-size: 13px;
  color: #606266;
  line-height: 1.7;
  padding-left: 18px;
  white-space: pre-wrap;
}
.entry-empty {
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
  padding: 8px 0;
}

.summary-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.sg-item {
  text-align: center;
  padding: 14px;
  background: linear-gradient(135deg, #f5faff 0%, #ecf5ff 100%);
  border-radius: 6px;
  border: 1px solid #e6f1fc;
}
.sg-num { font-size: 22px; font-weight: 700; color: #409eff; }
.sg-label { font-size: 12px; color: #909399; margin-top: 4px; }

.mood-chart { width: 100%; height: 200px; }
</style>
