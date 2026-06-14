import { get, post, put, del } from '../request'

export const taskApi = {
  today: () => get<Task[]>('/tasks/today'),
  list: (params: any) => get<TaskPage>('/tasks', { params }),
  get: (id: number) => get<Task>(`/tasks/${id}`),
  create: (data: any) => post<Task>('/tasks', data),
  update: (id: number, data: any) => put<Task>(`/tasks/${id}`, data),
  updateStatus: (id: number, status: string) => put(`/tasks/${id}/status`, { status }),
  start: (id: number) => put(`/tasks/${id}/start`),
  finish: (id: number) => put(`/tasks/${id}/finish`),
  delete: (id: number) => del(`/tasks/${id}`),
  subTasks: (id: number) => get<SubTask[]>(`/tasks/${id}/subtasks`),
  updateSubTask: (subTaskId: number, done: boolean) => put(`/tasks/subtasks/${subTaskId}`, { done })
}

export interface Task {
  id: number
  title: string
  description?: string
  status: 'todo' | 'doing' | 'done' | 'abandoned'
  priority: 'low' | 'medium' | 'high' | 'urgent'
  plannedMinutes?: number
  actualMinutes?: number
  startAt?: string
  finishAt?: string
  dueAt?: string
  plannedDate?: string
  createdAt: string
  updatedAt: string
}

export interface SubTask {
  id: number
  taskId: number
  title: string
  done: boolean
}

export interface TaskPage {
  records: Task[]
  total: number
  page: number
  size: number
}