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
      <div class="stat-group-label">
        <span class="dot dot-task"></span>
        <span>任务表现</span>
      </div>
      <el-row :gutter="16">
        <el-col :span="12">
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
        <el-col :span="12">
          <div class="stat-card">
            <div class="stat-num">{{ today?.todayMinutes ?? 0 }}</div>
            <div class="stat-label">今日任务专注分钟</div>
            <div class="stat-sub">基于已完成任务的 actualMinutes 累加</div>
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
      <div class="stat-group-label">
        <span class="dot dot-task"></span>
        <span>任务表现</span>
      </div>
      <el-row :gutter="16">
        <el-col :span="8">
          <div class="stat-card highlight">
            <div class="stat-num big">{{ range?.completionRate ?? 0 }}%</div>
            <div class="stat-label">30天完成率</div>
            <div class="stat-sub">{{ range?.doneTasks ?? 0 }} / {{ range?.totalTasks ?? 0 }} 任务</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-num">{{ range?.doneTasks ?? 0 }}</div>
            <div class="stat-label">累计完成任务</div>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="stat-card">
            <div class="stat-num">{{ range?.taskMinutes ?? 0 }}</div>
            <div class="stat-label">任务专注分钟</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="never" class="section-card">
      <template #header>
        <div class="card-header">
          <span><el-icon color="#67c23a"><TrendCharts /></el-icon> 任务工作量与完成率（30天）</span>
        </div>
      </template>
      <div class="stat-group-label">
        <span class="dot dot-task"></span>
        <span>任务表现 · 柱状=总任务数(工作量) / 折线=完成率%</span>
      </div>
      <div ref="chartRef" style="height: 360px"></div>
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
const chartRef = ref<HTMLElement>()

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
    renderChart()
  } catch (e) {
    console.error('analytics load failed', e)
  }
}

const renderChart = () => {
  if (!chartRef.value || trendData.value.length === 0) return
  const chart = echarts.init(chartRef.value)
  const dates = trendData.value.map(d => d.date?.slice(5, 10) || '')
  const totals = trendData.value.map(d => d.total || 0)
  const rates = trendData.value.map(d => Math.round(d.rate || 0))
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const idx = params[0].dataIndex
        const d = trendData.value[idx]
        return `<b>${d.date}</b><br/>` +
               `总任务: <b>${d.total}</b><br/>` +
               `已完成: <b>${d.done}</b><br/>` +
               `完成率: <b>${Math.round(d.rate)}%</b>`
      }
    },
    legend: { data: ['总任务数', '完成率'], top: 0, left: 'center' },
    grid: { top: 50, left: 60, right: 70, bottom: 50 },
    xAxis: { type: 'category', data: dates, axisLabel: { rotate: 45 } },
    yAxis: [
      { type: 'value', name: '任务数', position: 'left' },
      { type: 'value', name: '完成率%', position: 'right', min: 0, max: 100 }
    ],
    series: [
      {
        name: '总任务数',
        type: 'bar',
        data: totals,
        yAxisIndex: 0,
        itemStyle: { color: '#67c23a', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 28
      },
      {
        name: '完成率',
        type: 'line',
        yAxisIndex: 1,
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        data: rates,
        itemStyle: { color: '#409eff' },
        lineStyle: { width: 2 },
        areaStyle: { opacity: 0.12 },
        markLine: {
          silent: true,
          symbol: 'none',
          data: [{
            yAxis: 80,
            lineStyle: { color: '#e6a23c', type: 'dashed' },
            label: { formatter: '目标 80%', color: '#e6a23c', position: 'start', distance: 8 }
          }]
        }
      }
    ]
  })
}

onMounted(loadData)
</script>

<style scoped>
.analytics-page { max-width: 1200px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { margin: 0; font-size: 22px; font-weight: 600; }

.section-card { margin-bottom: 16px; border-radius: 8px; }

.stat-group-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #5e6d82;
  margin-bottom: 10px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}
.dot-task { background: #67c23a; }
.dot-learn { background: #409eff; }
.mt-16 { margin-top: 16px; }

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
