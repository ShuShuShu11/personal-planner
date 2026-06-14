<template>
  <el-container class="layout-container">
    <el-aside :width="collapsed ? '64px' : '210px'" class="aside">
      <div class="logo">
        <el-icon class="logo-icon"><TrendCharts /></el-icon>
        <span v-if="!collapsed" class="logo-text">Planner</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        :collapse-transition="false"
        router
        class="aside-menu"
        background-color="transparent"
        text-color="#5a5e66"
        active-text-color="#409eff"
      >
        <el-menu-item index="/today">
          <el-icon><Calendar /></el-icon>
          <template #title>任务中心</template>
        </el-menu-item>
        <el-menu-item index="/review">
          <el-icon><Edit /></el-icon>
          <template #title>每日复盘</template>
        </el-menu-item>
        <el-menu-item index="/profile">
          <el-icon><User /></el-icon>
          <template #title>个人画像</template>
        </el-menu-item>
        <el-menu-item index="/analytics">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>数据分析</template>
        </el-menu-item>
        <el-menu-item index="aihot-external" :route="{ path: '/today' }" @click="openAihotExternal">
          <el-icon><ChromeFilled /></el-icon>
          <template #title>AI 实时热点</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="collapsed = !collapsed">
            <Fold v-if="!collapsed" /><Expand v-else />
          </el-icon>
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="header-right">
          <span class="welcome">Hi, </span>
          <span class="username">{{ userStore.user?.nickname || userStore.user?.username }}</span>
          <el-dropdown @command="handleCommand">
            <el-avatar :size="34" class="avatar">{{ (userStore.user?.nickname || 'U')[0] }}</el-avatar>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)

const activeMenu = computed(() => {
  if (route.path === '/aihot-external') return '/today'
  return route.path
})

const titleMap: Record<string, string> = {
  '/today': '任务中心',
  '/tasks': '任务中心',
  '/review': '每日复盘',
  '/profile': '个人画像',
  '/analytics': '数据分析'
}
const pageTitle = computed(() => titleMap[route.path] || '')

const openAihotExternal = () => {
  window.open('https://aihot.virxact.com/', '_blank', 'noopener,noreferrer')
  nextTick(() => {
    router.push('/today')
  })
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container { height: 100vh; }

.aside {
  background: #fafbfc;
  border-right: 1px solid #ebeef5;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: left;
  padding: 0 20px;
  color: #303133;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid #ebeef5;
}
.logo-icon {
  font-size: 22px;
  color: #409eff;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
.logo-text {
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.aside-menu {
  border-right: none;
  background: transparent;
  padding: 8px 0;
}
.aside-menu:not(.el-menu--collapse) { width: 210px; }
.aside-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 4px 12px;
  border-radius: 8px;
  padding: 0 16px !important;
  font-size: 14px;
  transition: all 0.2s;
}
.aside-menu :deep(.el-menu-item:hover) {
  background: #ecf5ff !important;
  color: #409eff !important;
}
.aside-menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%) !important;
  color: #fff !important;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.25);
}
.aside-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #fff !important;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
}
.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  padding: 6px;
  border-radius: 4px;
  transition: all 0.2s;
}
.collapse-btn:hover { background: #f5f7fa; color: #409eff; }
.page-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.header-right { display: flex; align-items: center; gap: 8px; }
.welcome { font-size: 13px; color: #909399; }
.username { font-size: 14px; color: #303133; font-weight: 500; }
.avatar {
  cursor: pointer;
  background: linear-gradient(135deg, #409eff 0%, #67c23a 100%);
  color: #fff;
  margin-left: 8px;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.3);
}

.main {
  background: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
}
</style>
