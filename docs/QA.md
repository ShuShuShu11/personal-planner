# 问题档案

## [2026-06-14] today/index.vue "添加任务" 按钮点击无响应

- **问题**：任务中心页面点击"添加任务"按钮，弹窗不打开
- **原因**：按钮的 `@click="showAddDialog = true"` 引用了不存在的变量 `showAddDialog`，正确的函数是 `openAddDialog()`（第 383 行），该函数会重置表单状态后再打开弹窗
- **解决办法**：修改为 `@click="openAddDialog"` — `frontend/src/views/today/index.vue` 第 5 行
- **是否解决**：✅ 已解决

---

## [2026-06-14] AI 热点菜单点击后，左侧高亮未切换到"任务中心"，URL 停留在 /aihot-external

- **问题**：点击"AI 实时热点"菜单项后，新窗口打开正常，但原页面 URL 变为 `/aihot-external`（一个不存在的路由），左侧菜单高亮停留在 AI 热点项上，而非"任务中心"
- **原因**：el-menu 在 router 模式下，`index="aihot-external"` 被当作路由 path 执行 `router.push`。`:route="{ path: '/today' }"` 未能覆盖此行为（Element Plus 2.7.0 实测 `:route` 不生效）
- **解决办法**：
  1. `activeMenu` computed 对 `/aihot-external` 做特殊处理，返回 `/today`，确保"任务中心"高亮
  2. `openAihotExternal` 中用 `nextTick(() => router.push('/today'))` 在 el-menu 默认导航后覆盖 URL
  3. 涉及文件：`frontend/src/layouts/MainLayout.vue`
- **是否解决**：✅ 已解决

---

## [2026-06-14] MyBatis-Plus `updateById` 跳过 null 字段，导致任务状态回退时无法清空时间字段

- **问题**：将任务从"已完成"改回"待办"时，`start_at`、`finish_at`、`actual_minutes` 字段未被清空，数据库仍保留旧值
- **原因**：MyBatis-Plus 默认字段更新策略，`updateById` 会跳过值为 `null` 的字段（`FieldStrategy.NOT_NULL`），直接 set null 后调用 `updateById` 不会生成对应的 SET 子句
- **解决办法**：使用 `UpdateWrapper`，通过 `.set("start_at", null)` 显式指定需要置空的列。在 `TaskService.changeStatus()` 中，针对不同状态转换用 switch 精确设置哪些字段需要 null，哪些需要更新为当前时间。涉及文件：`backend/.../service/TaskService.java`
- **是否解决**：✅ 已解决

---

## [2026-06-14] 前端空字符串 filter 参数导致 MyBatis-Plus 查询返回 0 条

- **问题**：前端"全部任务"列表为空，后端实际有数据
- **原因**：前端请求 `GET /api/tasks?status=&priority=` — `status` 和 `priority` 为空字符串而非 null。MyBatis-Plus 的 `LambdaQueryWrapper.eq(condition, col, val)` 中 condition 为 `true`（因为空字符串不是 null），于是执行 `.eq(Task::getStatus, "")`，数据库中无匹配记录
- **解决办法**：前端构建查询参数时，只在 filter 有非空值时才加入 params 对象。涉及文件：`frontend/src/views/today/index.vue` 的 `loadAllTasks()`
- **是否解决**：✅ 已解决

---

## [2026-06-14] Backend RestTemplate 代理 aihot 返回 403

- **问题**：后端调用 `https://aihot.virxact.com/api/entries` 返回 403 Forbidden
- **原因**：上游 Nginx 对非浏览器 User-Agent（Java 默认的 `Java/21.0.x`）做了拦截
- **解决办法**：设置请求头 `User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/131.0.0.0` + `Referer: https://aihot.virxact.com/`
- **是否解决**：✅ 已解决（该功能后续改为前端直接 window.open，不再使用后端代理；但该经验可复用到其他 RestTemplate 代外部 API 的场景）

---

## [2026-06-14] Element Plus el-menu `default-active` 不响应式更新

- **问题**：通过程序化 `router.push` 切换路由后，el-menu 的 `default-active` 属性值虽然通过 computed 正确计算，但菜单 UI 高亮未更新
- **原因**：el-menu 在初始化时读取 `default-active`，但内部维护了自己的 activeIndex 状态；程序化路由变化不会触发 el-menu 内部状态的重新计算
- **解决办法**：在 activeMenu computed 中对特殊路径做映射（如 `/aihot-external` → `/today`）。此方案对 el-menu 内部状态有一定影响，但结合 nextTick 程序化导航可保证高亮和 URL 一致。涉及文件：`frontend/src/layouts/MainLayout.vue`
- **是否解决**：✅ 已解决
