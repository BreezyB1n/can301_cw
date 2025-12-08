# AI 智能备忘录与日程助手 PRD

## 1. 产品概述
本项目是一个基于 Android 平台的智能备忘录与日程管理应用。
- **核心价值**：用户上传截图/图片或输入文本，调用大模型生成标准化摘要与日程；系统基于摘要自动安排本地提醒通知，并提供集中的日程管理功能。
- **平台**：Android（Kotlin），本地 SQLite (Room) 持久化；AI 调用通过 HTTP 请求大模型服务（供应商可配置）。

## 2. 功能范围
本迭代聚焦于核心闭环体验：
- **备忘录 (Memo)**: 图片/文本上传 → AI 解析 → 结构化存储 → 关联日程。
- **日程 (Schedule)**: 集中展示所有任务，支持按日期分组、状态管理（完成/忽略）。
- **通知 (Notification)**: 基于解析时间的本地精确提醒（AlarmManager）。
- **个人中心 (Profile)**: 历史记录、AI 配置（API Key/Endpoint）、缓存管理。

## 3. 用户流程与功能详解

### 3.1 备忘录管理 (Memo CRUD)
用户创建和管理作为日程来源的备忘录数据。
- **新建**:
  - 输入方式：拍摄/选择图片（如会议截图、海报）、文本输入。
  - **AI 处理**: 图片压缩转 Base64，连同文本发送至 AI 服务。
  - **结果处理**: 接收 AI 返回的 JSON，解析为 `ApiResponse`，提取 `Schedule` 和 `Information` 并存储。
- **查看**:
  - 列表页：按创建时间倒序展示。
  - 详情页：展示原图、OCR 文本、AI 摘要、关联的任务列表。
- **删除**: 删除备忘录时，级联删除其关联的所有日程任务及本地提醒。

### 3.2 日程集中显示 (Centralized Schedule)
提供 `ScheduleScreen` 用于统一管理时间安排。
- **分组展示**: 任务按日期（如 "Today", "Tomorrow", "2024-12-12"）自动分组。
- **视觉呈现**:
  - 每个任务以卡片形式展示主题 (`theme`)、时间段 (`startTime` - `endTime`)。
  - 视觉区分不同状态的任务（如已完成任务置灰/划线）。
- **交互**: 点击卡片可展开查看详情或跳转至原备忘录。

### 3.3 完成状态管理 (Status Management)
支持对任务状态的精细控制：
- **状态流转**:
  - `PENDING` (待办): 默认状态。
  - `COMPLETED` (已完成): 用户勾选后，文本增加删除线，视觉弱化。
  - `IGNORED` (已忽略): 用户标记忽略，不再提醒。
- **操作方式**: 在日程列表直接点击 Checkbox 切换状态。

### 3.4 本地通知提醒 (Local Notifications)
- **机制**: 使用 `AlarmManager` 设置精确闹钟 (`setExactAndAllowWhileIdle`)。
- **触发**: 在任务开始前（或 AI 建议的时间）触发系统通知。
- **通知渠道**: 必须创建高优先级的 NotificationChannel，确保横幅弹出。
- **交互**: 点击通知自动跳转至应用详情页。

## 4. 数据模型 (本地 SQLite/Room)

基于现有代码实现 (`MemoItem.kt`, `AppDatabase.kt`) 的数据结构：

### 4.1 表 `memos` (Entity: MemoItem)
| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | String (UUID) | 主键 |
| `imagePath` | String? | 本地图片路径 |
| `recognizedText` | String | OCR 识别文本 |
| `userInputText` | String | 用户备注 |
| `title` | String | 标题 |
| `tags` | List<String> | 标签列表 (Converter存储) |
| `createdAt` | Date | 创建时间 |
| `scheduledDate` | Date? | 计划时间 |
| `apiResponse` | ApiResponse? | 完整 AI 响应 JSON (Converter存储) |
| `isAPIProcessing` | Boolean | 是否正在处理中 |

### 4.2 结构化对象 (Stored as JSON in DB)
- **ApiResponse**:
  - `mostPossibleCategory`: String
  - `information`: Information (摘要, 关联项)
  - `schedule`: Schedule (包含 `tasks` 列表)

- **ScheduleTask**:
  - `id`: String (UUID)
  - `startTime`: String
  - `endTime`: String
  - `theme`: String (任务主题)
  - `taskStatus`: Enum (`PENDING`, `COMPLETED`, `IGNORED`)
  - `tags`: List<String>

### 4.3 表 `user_profile` (配置)
- `api_key`: TEXT
- `endpoint`: TEXT
- `notify_lead_minutes`: INTEGER (默认提前通知时间)
- `wifi_only`: BOOLEAN

## 5. 网络/AI 接口约定

### 5.1 请求 (POST)
```json
{
  "trace_id": "<uuid>",
  "image_base64": "<base64_string>",
  "user_input": "<optional_text>",
  "expected_schema": "ComplexScheduleResponse"
}
```

### 5.2 响应 (JSON)
AI 模型需返回符合 `ApiResponse` 结构的 JSON 字符串：
```json
{
  "mostPossibleCategory": "Work",
  "information": {
    "title": "项目会议",
    "summary": "讨论Q4 roadmap",
    "tags": ["meeting", "work"],
    "relatedItems": [],
    "informationItems": []
  },
  "schedule": {
    "title": "会议安排",
    "category": "Work",
    "tasks": [
      {
        "id": "uuid-gen-by-ai-or-local",
        "startTime": "2024-12-08 14:00",
        "endTime": "2024-12-08 15:00",
        "theme": "Q4 Planning",
        "people": ["Alice", "Bob"],
        "taskStatus": "pending"
      }
    ]
  }
}
```

## 6. 业务规则与边界
- **上传限制**：单张图片建议 < 5MB，Base64 编码前需压缩。
- **网络策略**：
  - 超时/失败需重试（建议指数退避）。
- **隐私**：图片仅本地存储，上传仅用于 AI 分析，分析后不留存（依赖 AI 供应商隐私协议）。
- **容错**：若 AI 返回 JSON 格式错误，标记状态为 Failed，允许用户在历史记录中重试。

## 7. 性能与质量要求
- **响应速度**：AI 请求超时设定为 10-30s，UI 需显示加载状态。
- **后台稳定**：确保 `AlarmManager` 在 Doze 模式下仍能准确唤醒（申请精确闹钟权限）。
- **主线程保护**：数据库 IO 与网络请求必须在 IO 线程执行 (`Dispatchers.IO`)。

## 8. 未来规划 (Roadmap)
- **手动编辑**: 允许修正 AI 识别错误的日期/主题。
- **多视图**: 增加日历月视图。
- **云同步**: 跨设备数据同步（OneDrive、Google Calendar）。
