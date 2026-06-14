import { get, post, put } from '../request'

export const profileApi = {
  get: () => get<SkillProfile[]>('/profile'),
  addSkill: (data: any) => post<SkillProfile>('/profile/skills', data),
  updateSkill: (id: number, data: any) => put<SkillProfile>(`/profile/skills/${id}`, data),
  history: () => get<ProfileChangeHistory[]>('/profile/history'),
  aiUpdate: (data: any) => post<ProfileAiUpdateResponse>('/profile/ai-update', data),
  aiGenerateMd: (data: ProfileMdGenerateRequest) => post<ProfileMdResponse>('/profile/ai-generate-md', data),
  getDocument: () => get<ProfileDocument | null>('/profile/document'),
  updateDocument: (data: ProfileDocumentUpdateRequest) => put<ProfileMdResponse>('/profile/document', data)
}

export interface SkillProfile {
  id: number
  category: string
  skillName: string
  level: 'S' | 'A' | 'B' | 'C' | 'D'
  keywords?: string
  notes?: string
  updatedAt: string
}

export interface ProfileChangeHistory {
  id: number
  userId: number
  skillName: string
  oldLevel?: string
  newLevel: string
  changeType: 'increase' | 'decrease' | 'new' | 'manual' | 'ai'
  reason?: string
  createdAt: string
}

export interface ProfileAiUpdateResponse {
  updates: Array<{ skill: string; oldLevel: string; newLevel: string; reason: string }>
  summary: string
}

export interface ProfileMdGenerateRequest {
  mdContent?: string
  includeLearning?: boolean
}

export interface ProfileMdResponse {
  markdown: string
  suggestedSkills: string[]
}

export interface ProfileDocument {
  id: number
  userId: number
  markdown: string
  sourceMd?: string
  suggestedSkills?: string
  version: number
  createdAt: string
  updatedAt: string
}

export interface ProfileDocumentUpdateRequest {
  markdown?: string
  sourceMd?: string
  regenerate?: boolean
  includeLearning?: boolean
}