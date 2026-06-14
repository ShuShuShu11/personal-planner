import { get } from '../request'

export const analyticsApi = {
  heatmap: (days?: number) => get<HeatmapItem[]>('/analytics/heatmap', { params: { days } }),
  timeByTag: (days?: number) => get<any>('/analytics/time-by-tag', { params: { days } }),
  completionTrend: (days?: number) => get<TrendItem[]>('/analytics/completion-trend', { params: { days } }),
  today: () => get<TodayOverview>('/analytics/today'),
  range: (days?: number) => get<RangeOverview>('/analytics/range', { params: { days } })
}

export interface HeatmapItem {
  date: string
  count: number
}

export interface TrendItem {
  date: string
  total: number
  done: number
  rate: number
}

export interface TodayOverview {
  totalTasks: number
  todoCount: number
  doingCount: number
  doneCount: number
  todayMinutes: number
}

export interface RangeOverview {
  totalTasks: number
  doneTasks: number
  completionRate: number
  taskMinutes: number
}
