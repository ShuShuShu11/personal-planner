import { defineStore } from 'pinia'
import { ref } from 'vue'
import { post, get } from '@/api/request'

export const useUserStore = defineStore('user', () => {
  const user = ref<UserInfo | null>(null)
  const accessToken = ref(localStorage.getItem('accessToken') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')

  async function login(username: string, password: string) {
    const res = await post<LoginResponse>('/auth/login', { username, password })
    setTokens(res.accessToken, res.refreshToken)
    user.value = res.user
    return res
  }

  async function register(params: { username: string; password: string; nickname?: string }) {
    const res = await post<LoginResponse>('/auth/register', params)
    setTokens(res.accessToken, res.refreshToken)
    user.value = res.user
    return res
  }

  async function fetchMe() {
    const res = await get<UserInfo>('/auth/me')
    user.value = res
    return res
  }

  function setTokens(access: string, refresh: string) {
    accessToken.value = access
    refreshToken.value = refresh
    localStorage.setItem('accessToken', access)
    localStorage.setItem('refreshToken', refresh)
  }

  function logout() {
    accessToken.value = ''
    refreshToken.value = ''
    user.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  return { user, accessToken, refreshToken, login, register, fetchMe, logout, setTokens }
})

export interface UserInfo {
  id: number
  username: string
  nickname: string
  email?: string
  phone?: string
  avatar?: string
  createdAt: string
}

interface LoginResponse {
  accessToken: string
  refreshToken: string
  user: UserInfo
}