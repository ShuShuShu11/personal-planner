import { get, put } from '../request'

export const reviewApi = {
  today: () => get<DailyReview>('/reviews/today'),
  list: (params?: any) => get<DailyReview[]>('/reviews', { params }),
  updateToday: (data: any) => put<DailyReview>('/reviews/today', data),
  week: () => get<any>('/reviews/week'),
  month: (year: number, month: number) => get<any>('/reviews/month', { params: { year, month } })
}

export interface DailyReview {
  id: number
  reviewDate: string
  doneSummary?: string
  undoneSummary?: string
  gains?: string
  improvements?: string
  moodScore?: number
  createdAt: string
  updatedAt: string
}