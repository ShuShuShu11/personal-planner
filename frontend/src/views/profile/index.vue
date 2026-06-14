<template>
  <div class="profile-page">
    <div class="page-header">
      <div>
        <h2>个人画像</h2>
        <p class="page-subtitle">上传简历/技术文档，AI 帮你生成结构化的个人技术画像，并支持随时调整</p>
      </div>
      <div class="header-actions">
        <el-button :icon="Document" @click="uploadExpanded = !uploadExpanded">
          {{ uploadExpanded ? '收起基础资料' : '添加基础资料' }}
        </el-button>
        <el-button
          type="primary"
          :icon="MagicStick"
          :loading="generating"
          @click="generateMdProfile"
        >
          {{ savedDoc ? '更新画像' : '生成画像' }}
        </el-button>
      </div>
    </div>

    <transition name="collapse">
      <el-card v-show="uploadExpanded" shadow="never" class="input-card">
        <template #header>
          <div class="card-header">
            <span>
              <el-icon><Document /></el-icon>
              基础资料
            </span>
            <el-tag v-if="savedDoc" size="small" type="success" effect="plain">v{{ savedDoc.version }}</el-tag>
          </div>
        </template>

        <el-alert
          v-if="hasUpload"
          type="info"
          :closable="false"
          show-icon
          class="upload-summary"
        >
          <template #title>
            <div class="upload-summary-title">已加载资料 ({{ uploadedSourceLabel }})</div>
          </template>
          <div class="upload-summary-content">{{ uploadedSourcePreview }}</div>
          <div class="upload-summary-actions">
            <el-button size="small" text type="danger" @click="clearUpload">清空</el-button>
          </div>
        </el-alert>

        <el-form v-else label-position="top" size="default">
          <el-form-item label="上传 Markdown 文档（简历 / 技术总结 / 项目经历）">
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :limit="1"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              accept=".md,.markdown,.txt"
              drag
            >
              <el-icon class="upload-icon"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                拖拽文件到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">支持 .md / .markdown / .txt，最大 1MB</div>
              </template>
            </el-upload>
          </el-form-item>

          <el-form-item label="或直接粘贴 Markdown 内容">
            <el-input
              v-model="mdContent"
              type="textarea"
              :rows="6"
              placeholder="可粘贴简历、技术总结、项目经历等 Markdown 内容"
              resize="none"
            />
          </el-form-item>
        </el-form>

        <el-form v-if="hasUpload" label-position="top" size="default">
        </el-form>
      </el-card>
    </transition>

    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :md="16" :lg="17">
        <el-card shadow="never" class="output-card" v-loading="generating" element-loading-text="AI 正在生成画像...">
          <template #header>
            <div class="card-header">
              <span>
                <el-icon><Reading /></el-icon>
                {{ isEditing ? '编辑画像' : 'AI 画像（Markdown）' }}
              </span>
              <div class="output-actions" v-if="mdResult">
                <el-button-group v-if="!isEditing">
                  <el-button :type="viewMode === 'preview' ? 'primary' : ''" size="small" @click="viewMode = 'preview'">
                    <el-icon><View /></el-icon> 预览
                  </el-button>
                  <el-button size="small" @click="enterEditMode">
                    <el-icon><Edit /></el-icon> 编辑
                  </el-button>
                </el-button-group>
                <el-button size="small" @click="copyMd" v-if="!isEditing">
                  <el-icon><CopyDocument /></el-icon> 复制
                </el-button>
                <el-button size="small" type="primary" @click="downloadMd" v-if="!isEditing">
                  <el-icon><Download /></el-icon> 下载
                </el-button>
                <el-button size="small" @click="cancelEdit" v-if="isEditing">取消</el-button>
                <el-button size="small" type="primary" :loading="saving" @click="saveEdit" v-if="isEditing">
                  <el-icon><Check /></el-icon> 保存
                </el-button>
              </div>
            </div>
          </template>

          <div v-if="!mdResult" class="empty-state">
            <el-empty description="尚未生成画像，请添加基础资料后点击右上角「生成画像」">
              <template #image>
                <el-icon class="empty-icon"><DataAnalysis /></el-icon>
              </template>
            </el-empty>
          </div>

          <div v-else>
            <div v-if="isEditing">
              <el-input
                v-model="editingMarkdown"
                type="textarea"
                :rows="22"
                resize="vertical"
                placeholder="直接编辑 Markdown 内容..."
                class="md-editor"
              />
              <div class="editor-hint">
                <el-icon><InfoFilled /></el-icon>
                支持标准 Markdown 语法，保存后立即生效
              </div>
            </div>
            <div v-else class="md-preview" v-html="renderedHtml"></div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="8" :lg="7">
        <div class="sidebar">
          <!-- 画像统计卡片 -->
          <el-card shadow="never" class="side-card stat-card">
            <template #header>
              <div class="card-header">
                <span><el-icon><DataAnalysis /></el-icon> 画像状态</span>
              </div>
            </template>
            <div v-if="savedDoc" class="stats-grid">
              <div class="stat-item">
                <div class="stat-label">版本</div>
                <div class="stat-value">v{{ savedDoc.version }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">字符数</div>
                <div class="stat-value">{{ savedDoc.markdown?.length || 0 }}</div>
              </div>
              <div class="stat-item full">
                <div class="stat-label">上次更新</div>
                <div class="stat-value-sm">{{ dayjs(savedDoc.updatedAt).format('YYYY-MM-DD HH:mm') }}</div>
              </div>
            </div>
            <el-empty v-else :image-size="60" description="尚未生成画像" />
          </el-card>

          <!-- 待补充技术 -->
          <el-card v-if="mdResult?.suggestedSkills?.length" shadow="never" class="side-card">
            <template #header>
              <div class="card-header">
                <span><el-icon><Aim /></el-icon> 待补充的技术</span>
                <el-tag size="small" type="warning" effect="plain">{{ mdResult.suggestedSkills.length }}</el-tag>
              </div>
            </template>
            <div class="suggested-skills">
              <el-tag
                v-for="(s, i) in mdResult.suggestedSkills"
                :key="i"
                type="warning"
                effect="plain"
                round
                class="suggested-tag"
              >
                {{ s }}
              </el-tag>
            </div>
            <div class="hint">
              <el-icon><InfoFilled /></el-icon>
              AI 建议关注以上技术方向
            </div>
          </el-card>

          <!-- 变更历史 -->
          <el-card v-if="history.length > 0" shadow="never" class="side-card">
            <template #header>
              <div class="card-header">
                <span><el-icon><Clock /></el-icon> 变更历史</span>
                <el-tag size="small" type="info" effect="plain">{{ history.length }}</el-tag>
              </div>
            </template>
            <div class="history-list">
              <div v-for="h in history.slice(0, 8)" :key="h.id" class="history-item">
                <div class="history-row">
                  <strong>{{ h.skillName }}</strong>
                  <el-tag size="small" :type="h.changeType === 'ai' ? 'primary' : 'success'" effect="plain">
                    {{ h.changeType === 'ai' ? 'AI' : h.changeType === 'new' ? '新增' : '手动' }}
                  </el-tag>
                </div>
                <div class="history-meta">
                  <span v-if="h.oldLevel">{{ h.oldLevel }} → <strong>{{ h.newLevel }}</strong></span>
                  <span v-else>新增 <strong>{{ h.newLevel }}</strong></span>
                  <span class="history-time">{{ dayjs(h.createdAt).format('MM-DD HH:mm') }}</span>
                </div>
                <div v-if="h.reason" class="history-reason">{{ h.reason }}</div>
              </div>
            </div>
          </el-card>

          <!-- 使用提示 -->
          <el-card shadow="never" class="side-card tips-card">
            <template #header>
              <div class="card-header">
                <span><el-icon><InfoFilled /></el-icon> 使用建议</span>
              </div>
            </template>
            <ul class="tips-list">
              <li>定期更新简历/技术总结，让 AI 持续追踪你的成长</li>
              <li>AI 会自动结合最近完成的任务（标题/描述/标签）来判断成长方向</li>
              <li>编辑画像可手动微调 AI 的输出内容</li>
              <li>下载 Markdown 用于求职/汇报场景</li>
            </ul>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import MarkdownIt from 'markdown-it'
import {
  profileApi,
  type ProfileChangeHistory,
  type ProfileMdResponse,
  type ProfileDocument
} from '@/api/modules/profile'
import { ElMessage } from 'element-plus'
import {
  Document,
  MagicStick
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const history = ref<ProfileChangeHistory[]>([])

const mdContent = ref('')
const generating = ref(false)
const saving = ref(false)
const mdResult = ref<ProfileMdResponse | null>(null)
const savedDoc = ref<ProfileDocument | null>(null)
const viewMode = ref<'preview' | 'edit'>('preview')
const isEditing = ref(false)
const editingMarkdown = ref('')
const uploadExpanded = ref(false)

const md = new MarkdownIt({ html: false, linkify: true, breaks: true })
const renderedHtml = computed(() => mdResult.value ? md.render(mdResult.value.markdown) : '')

const hasUpload = computed(() => mdContent.value.trim().length > 0)
const uploadedSourceLabel = computed(() => {
  const len = mdContent.value.length
  return len > 0 ? `${len} 字符` : ''
})
const uploadedSourcePreview = computed(() => {
  const c = mdContent.value.trim()
  if (!c) return ''
  return c.length > 100 ? c.slice(0, 100) + '...' : c
})

const loadHistory = async () => {
  try { history.value = await profileApi.history() as ProfileChangeHistory[] } catch {}
}

const loadDocument = async () => {
  try {
    const doc = await profileApi.getDocument() as ProfileDocument | null
    savedDoc.value = doc
    if (doc && doc.markdown) {
      mdResult.value = {
        markdown: doc.markdown,
        suggestedSkills: doc.suggestedSkills ? JSON.parse(doc.suggestedSkills) : []
      }
    }
  } catch {}
}

const handleFileChange = (file: any) => {
  const raw = file.raw
  if (!raw) return
  if (raw.size > 1024 * 1024) {
    ElMessage.error('文件超过 1MB')
    return
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    mdContent.value = (e.target?.result as string) || ''
    ElMessage.success('文件已读取')
  }
  reader.readAsText(raw, 'UTF-8')
}

const handleFileRemove = () => { mdContent.value = '' }

const clearUpload = () => {
  mdContent.value = ''
  ElMessage.success('已清空上传资料')
}

const generateMdProfile = async () => {
  if (!mdContent.value.trim()) {
    ElMessage.warning('请先添加基础资料（点击右上角"添加基础资料"）')
    uploadExpanded.value = true
    return
  }
  generating.value = true
  try {
    mdResult.value = await profileApi.aiGenerateMd({
      mdContent: mdContent.value
    }) as ProfileMdResponse
    ElMessage.success('画像生成完成')
    uploadExpanded.value = false
    await loadDocument()
    await loadHistory()
  } catch (e: any) {
    ElMessage.error(e?.message || '生成失败')
  } finally {
    generating.value = false
  }
}

const enterEditMode = () => {
  if (!mdResult.value) return
  editingMarkdown.value = mdResult.value.markdown
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
  editingMarkdown.value = ''
}

const saveEdit = async () => {
  if (!editingMarkdown.value.trim()) {
    ElMessage.warning('画像内容不能为空')
    return
  }
  saving.value = true
  try {
    mdResult.value = await profileApi.updateDocument({
      markdown: editingMarkdown.value,
      sourceMd: savedDoc.value?.sourceMd || mdContent.value,
      regenerate: false
    }) as ProfileMdResponse
    isEditing.value = false
    ElMessage.success('画像已保存')
    await loadDocument()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const copyMd = async () => {
  if (!mdResult.value) return
  try {
    await navigator.clipboard.writeText(mdResult.value.markdown)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败')
  }
}

const downloadMd = () => {
  if (!mdResult.value) return
  const blob = new Blob([mdResult.value.markdown], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `个人技术画像-${dayjs().format('YYYYMMDD-HHmm')}.md`
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(() => {
  loadHistory()
  loadDocument()
})
</script>

<style scoped>
.profile-page { max-width: 1600px; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  gap: 16px;
}
.page-header h2 { margin: 0 0 4px; font-size: 22px; font-weight: 600; }
.page-subtitle { margin: 0; color: #909399; font-size: 13px; }
.header-actions { display: flex; gap: 8px; flex-shrink: 0; }

.input-card { margin-bottom: 16px; border-radius: 8px; }
.content-row { margin-bottom: 16px; }
.output-card { border-radius: 8px; min-height: 600px; }

.sidebar { display: flex; flex-direction: column; gap: 12px; }
.side-card { border-radius: 8px; }
.side-card :deep(.el-card__header) { padding: 12px 16px; }
.side-card :deep(.el-card__body) { padding: 14px 16px; }

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 14px;
}
.card-header span { display: inline-flex; align-items: center; gap: 6px; }
.output-actions { display: flex; gap: 8px; }

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}
.stat-item {
  background: linear-gradient(135deg, #f5faff 0%, #ecf5ff 100%);
  border-radius: 6px;
  padding: 10px 12px;
  border: 1px solid #e6f1fc;
}
.stat-item.full { grid-column: 1 / -1; background: linear-gradient(135deg, #f5fff5 0%, #ecf9ec 100%); border-color: #e6f7e6; }
.stat-label { font-size: 12px; color: #909399; margin-bottom: 4px; }
.stat-value { font-size: 20px; font-weight: 700; color: #409eff; }
.stat-value-sm { font-size: 13px; font-weight: 600; color: #67c23a; font-family: 'JetBrains Mono', Consolas, monospace; }

.suggested-skills { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 10px; }
.suggested-tag { cursor: default; }

.hint {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
}

.history-list { max-height: 360px; overflow-y: auto; }
.history-item {
  padding: 10px 0;
  border-bottom: 1px dashed #ebeef5;
}
.history-item:last-child { border-bottom: none; }
.history-row { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.history-row strong { font-size: 13px; color: #303133; }
.history-meta {
  font-size: 12px;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.history-time { color: #909399; font-size: 11px; }
.history-reason {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.5;
  padding-left: 8px;
  border-left: 2px solid #ebeef5;
}

.tips-list {
  margin: 0;
  padding-left: 18px;
  font-size: 12px;
  color: #606266;
  line-height: 1.8;
}
.tips-list li { margin-bottom: 4px; }

.upload-icon { font-size: 48px; color: #409eff; }

.upload-summary { margin-bottom: 12px; }
.upload-summary-title { font-weight: 600; font-size: 13px; }
.upload-summary-content {
  margin-top: 6px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  max-height: 80px;
  overflow: auto;
  font-family: 'JetBrains Mono', Consolas, monospace;
  white-space: pre-wrap;
  word-break: break-all;
}
.upload-summary-actions {
  margin-top: 6px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.empty-state { padding: 80px 0; }
.empty-icon { font-size: 80px; color: #dcdfe6; }

.md-editor {
  font-family: 'JetBrains Mono', 'Cascadia Code', Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
}
.md-editor :deep(.el-textarea__inner) {
  background: #1e1e1e;
  color: #d4d4d4;
  border-radius: 6px;
  min-height: 500px;
}
.editor-hint {
  margin-top: 8px;
  padding: 6px 12px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.md-preview {
  font-size: 14px;
  line-height: 1.8;
  color: #303133;
}
.md-preview :deep(h1) {
  font-size: 24px;
  font-weight: 700;
  border-bottom: 2px solid #409eff;
  padding-bottom: 12px;
  margin: 0 0 20px;
  color: #1f2d3d;
}
.md-preview :deep(h2) {
  font-size: 18px;
  font-weight: 600;
  margin: 24px 0 12px;
  padding-left: 10px;
  border-left: 4px solid #409eff;
  color: #1f2d3d;
}
.md-preview :deep(h3) {
  font-size: 15px;
  font-weight: 600;
  margin: 16px 0 8px;
  color: #409eff;
}
.md-preview :deep(ul), .md-preview :deep(ol) { padding-left: 24px; margin: 8px 0; }
.md-preview :deep(li) { margin: 4px 0; }
.md-preview :deep(strong) { color: #409eff; font-weight: 600; }
.md-preview :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'JetBrains Mono', Consolas, monospace;
  font-size: 13px;
  color: #e6a23c;
}
.md-preview :deep(p) { margin: 8px 0; }
.md-preview :deep(hr) { margin: 24px 0; border: none; border-top: 1px dashed #dcdfe6; }
.md-preview :deep(blockquote) {
  margin: 12px 0;
  padding: 8px 16px;
  background: #ecf5ff;
  border-left: 4px solid #409eff;
  color: #5e6d82;
  border-radius: 4px;
}
.md-preview :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
  font-size: 13px;
}
.md-preview :deep(th), .md-preview :deep(td) {
  border: 1px solid #ebeef5;
  padding: 6px 10px;
  text-align: left;
}
.md-preview :deep(th) { background: #f5f7fa; font-weight: 600; }

.collapse-enter-active, .collapse-leave-active {
  transition: opacity 0.2s, max-height 0.3s;
  overflow: hidden;
}
.collapse-enter-from, .collapse-leave-to {
  opacity: 0;
  max-height: 0;
}
.collapse-enter-to, .collapse-leave-from {
  opacity: 1;
  max-height: 1000px;
}

@media (max-width: 768px) {
  .sidebar { margin-top: 16px; }
}
</style>
