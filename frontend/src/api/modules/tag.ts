import { get, post, put, del } from '../request'

export const tagApi = {
  list: () => get<Tag[]>('/tags'),
  create: (data: any) => post<Tag>('/tags', data),
  update: (id: number, data: any) => put<Tag>(`/tags/${id}`, data),
  delete: (id: number) => del(`/tags/${id}`)
}

export interface Tag {
  id: number
  name: string
  color: string
  createdAt: string
}