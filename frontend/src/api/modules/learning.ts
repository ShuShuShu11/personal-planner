import { get, post, put, del } from '../request'

export const learningApi = {
  list: (params?: any) => get<LearningPage>('/learnings', { params }),
  get: (id: number) => get<LearningRecord>(`/learnings/${id}`),
  create: (data: any) => post<LearningRecord>('/learnings', data),
  update: (id: number, data: any) => put<LearningRecord>(`/learnings/${id}`, data),
  delete: (id: number) => del(`/learnings/${id}`),
  stats: () => get<LearningStats>('/learnings/stats')
}

export interface LearningRecord {
  id: number
  title: string
  content?: string
  durationMinutes?: number
  tags?: string
  source?: string
  createdAt: string
  updatedAt: string
}

export interface LearningPage {
  records: LearningRecord[]
  total: number
  page: number
  size: number
}

export interface LearningStats {
  recentCount: number
  recentMinutes: number
  tagDistribution: Record<string, number>
}