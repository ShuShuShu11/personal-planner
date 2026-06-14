<template>
  <div class="analytics-page">
    <div class="page-header">
      <h2>数据分析</h2>
    </div>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <span><el-icon color="#409eff"><Sunny /></el-icon> 今日概览</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-num">{{ today?.totalTasks ?? '—' }}</div>
            <div class="stat-label">今日任务总数</div>
            <div class="stat-sub">
              <el-tag size="small" type="info">待办 {{ today?.todoCount ?? 0 }}</el-tag>
              <el-tag size="small" type="warning">进行 {{ today?.doingCount ?? 0 }}</el-tag>
              <el-tag size="small" type="success">完成 {{ today?.doneCount ?? 0 }}</el-tag>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-num">{{ today?.todayMinutes ?? 0 }}</div>
            <div class="stat-label">今日任务专注分钟</div>
            <div class="stat-sub">基于已完成任务的 actualMinutes 累加</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-num">{{ today?.todayLearnings ?? 0 }}</div>
            <div class="stat-label">今日学习记录</div>
            <div class="stat-sub">今日新建的学习条目数</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-num">{{ range?.learningMinutes ?? 0 }}</div>
            <div class="stat-label">近30天学习分钟</div>
            <div class="stat-sub">{{ range?.learningCount ?? 0 }} 条记录 · {{ range?.activeDays ?? 0 }} 个活跃日</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <span><el-icon color="#67c23a"><DataLine /></el-icon> 近30天统计</span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="6">
          <div class="stat-card highlight">
            <div class="stat-num big">{{ range?.completionRate ?? 0 }}%</div>
            <div class="stat-label">30天完成率</div>
            <div class="stat-sub">{{ range?.doneTasks ?? 0 }} / {{ range?.totalTasks ?? 0 }} 任务</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-num">{{ range?.doneTasks ?? 0 }}</div>
            <div class="stat-label">累计完成任务</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-num">{{ range?.taskMinutes ?? 0 }}</div>
            <div class="stat-label">任务专注分钟</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-num">{{ range?.activeDays ?? 0 }}</div>
            <div class="stat-label">有学习的天数</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <span><el-icon color="#e6a23c"><TrendCharts /></el-icon> 完成率趋势（30天）</span>
        </div>
      </template>
      <div ref="trendChartRef" style="height: 280px"></div>
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <span><el-icon color="#909399"><Histogram /></el-icon> 每日完成任务数（30天）</span>
        </div>
      </template>
      <div ref="barChartRef" style="height: 240px"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { analyticsApi } from '@/api/modules/analytics'

const today = ref<any>(null)
const range = ref<any>(null)
const trendData = ref<any[]>([])
const trendChartRef = ref<HTMLElement>()
const barChartRef = ref<HTMLElement>()

const loadData = async () => {
  try {
    const [todayRes, rangeRes, trendRes] = await Promise.all([
      analyticsApi.today() as Promise<any>,
      analyticsApi.range(30) as Promise<any>,
      analyticsApi.completionTrend(30) as Promise<any[]>
    ])
    today.value = todayRes
    range.value = rangeRes
    trendData.value = trendRes || []
    renderTrendChart()
    renderBarChart()
  } catch (e) {
    console.error('analytics load failed', e)
  }
}

const renderTrendChart = () => {
  if (!trendChartRef.value || trendData.value.length === 0) return
  const chart = echarts.init(trendChartRef.value)
  const dates = trendData.value.map(d => d.date?.slice(5, 10) || '')
  const rates = trendData.value.map(d => Math.round(d.rate || 0))
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: 45 } },
    yAxis: { type: 'value', max: 100, name: '完成率%' },
    series: [{
      data: rates,
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#67c23a' }
    }]
  })
}

const renderBarChart = () => {
  if (!barChartRef.value || trendData.value.length === 0) return
  const chart = echarts.init(barChartRef.value)
  const dates = trendData.value.map(d => d.date?.slice(5, 10) || '')
  const counts = trendData.value.map(d => d.done || 0)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: 45 } },
    yAxis: { type: 'value', name: '完成任务数' },
    series: [{
      data: counts,
      type: 'bar',
      itemStyle: { color: '#409eff' }
    }]
  })
}

onMounted(loadData)
</script>

<style scoped>
.analytics-page { max-width: 1200px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 22px; font-weight: 600; }

.section-card { margin-bottom: 16px; border-radius: 8px; }
.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 15px;
}

.stat-card {
  text-align: center;
  padding: 16px 12px;
  border-radius: 8px;
  background: #fafbfc;
  height: 100%;
}
.stat-card.highlight { background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%); }
.stat-num { font-size: 28px; font-weight: 700; color: #303133; line-height: 1.2; }
.stat-num.big { font-size: 36px; color: #67c23a; }
.stat-label { color: #5e6d82; margin-top: 6px; font-size: 13px; }
.stat-sub {
  color: #909399;
  font-size: 12px;
  margin-top: 6px;
  display: flex;
  justify-content: center;
  gap: 4px;
  flex-wrap: wrap;
}
</style>
