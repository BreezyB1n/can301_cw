# AI 图片摘要提醒 App PRD（后端/本地逻辑聚焦）

## 1. 产品概述
- 目标：用户上传截图/图片，调用大模型生成标准化 JSON 摘要；系统基于摘要安排本地提醒通知，并提供个人中心管理上传记录与偏好。
- 平台：Android（Kotlin），本地 SQLite 持久化；AI 调用通过 HTTP 请求大模型服务（供应商可配置）。

## 2. 范围
- 本迭代聚焦核心功能与个人中心：
  - 图片上传 → AI 摘要 → 存储 → 通知。
  - 个人中心：历史记录、偏好、API Key/Endpoint 管理、本地缓存清理。
  - 通知：前台/计划提醒（AlarmManager/WorkManager），通知渠道。

## 3. 用户流程（后端视角）
1) 上传流程  
   - 选择/拍摄图片 → 压缩&Base64 → 调用 AI → 得到标准 JSON → 入库 → 根据 JSON 中的提醒信息创建通知 → 回显摘要。  
2) 历史查看  
   - 查询 SQLite 任务表，按创建时间倒序，展示摘要与状态（待通知/已通知/失败）。  
3) 个人中心  
   - 维护本地配置（API Key、Endpoint、默认通知提前量、网络/仅 Wi‑Fi、存储上限）。  
   - 清理缓存（图片临时文件、本地 DB 过期数据）。  

## 4. 数据模型（SQLite 本地）
- 表 `image_tasks`
  - `id` TEXT PRIMARY KEY (UUID)，`image_path` TEXT，`request_trace_id` TEXT，`summary_json` TEXT，`status` TEXT ENUM('pending','scheduled','notified','failed')，`notify_at` INTEGER (epoch ms)，`created_at` INTEGER，`updated_at` INTEGER，`error` TEXT NULL。
- 表 `user_profile`
  - `id` INTEGER PRIMARY KEY (1 row)，`api_key` TEXT，`endpoint` TEXT，`notify_lead_minutes` INTEGER DEFAULT 10，`wifi_only` INTEGER(0/1) DEFAULT 0，`max_cache_mb` INTEGER DEFAULT 200，`updated_at` INTEGER。
- 表 `audit_logs`（轻量日志可选）
  - `id` INTEGER PRIMARY KEY AUTOINCREMENT，`task_id` TEXT，`event` TEXT，`detail` TEXT，`created_at` INTEGER。

## 5. 网络/AI 接口约定
- 请求（POST JSON，需携带 `Authorization: Bearer <apiKey>` 等自定义头）：  
```json
{
  "trace_id": "<uuid>",
  "image_base64": "<base64>",
  "language": "zh-CN",
  "expected_schema": "ImageSummaryV1"
}
```
- 模型输出必须是 JSON 字符串，格式 `ImageSummaryV1`：  
```json
{
  "task_id": "<uuid>",
  "title": "摘要标题",
  "summary": "一句话说明图片包含的事项与意图",
  "labels": ["日程","会议","账单"],
  "schedule": {
    "start_time": "2024-11-07T10:00:00Z",
    "end_time": "2024-11-07T11:00:00Z",
    "remind_minutes_before": 15
  },
  "actions": [
    {"type": "notify", "time": "2024-11-07T09:45:00Z", "message": "会议提醒"}
  ],
  "source_confidence": 0.87,
  "trace_id": "<echo trace_id>"
}
```
- 后端逻辑要求：严格校验 JSON 字段完整性与类型；缺失/不合法时标记任务为 `failed` 并记录 `error`。

## 6. 核心模块职责
- `AiClient`：封装 HTTP 请求与超时、Header、错误处理，返回 JSON 字符串；调用方负责解析/校验。
- `Repository`：协调图片缓存、AI 请求、DB 存储、通知创建。
- `Db` 层：SQLiteOpenHelper/Room（后续可换 Room）管理三张表。
- `Scheduler`：基于 `actions`/`schedule` 创建/取消通知（AlarmManager + NotificationManager）；前台立即提醒与计划提醒统一入口。

## 7. 业务规则与边界
- 上传限制：单张图片 < 5MB，Base64 长度校验，非空检查。
- 网络：遵守用户偏好 `wifi_only`；超时/非 2xx 视为失败并重试（指数退避，最多 3 次）。
- 幂等：`trace_id`/`task_id` 关联，重复响应以 `task_id` 去重更新。
- 隐私：图片仅本地暂存，上传前可用户开关“仅结构化摘要”（只传 OCR 文本）。
- 通知：必须创建 NotificationChannel；多提醒合并去重。
- 错误路径：AI 返回非 JSON/缺字段 → 记录 error、状态 `failed`，可在历史页重试。

## 8. 性能与质量
- 目标：AI 请求超时 10s，重试后总耗时 < 30s；主线程无阻塞。
- 可观测性：`audit_logs` 记录关键事件；埋点 trace_id 贯穿请求/响应/通知。

## 9. 交付物（本阶段）
- PRD（本文）。
- Kotlin 后端工具函数：封装 AI 请求返回 JSON 字符串，易于替换 Endpoint/Key。
- 基础 DB 表定义与存储接口（后续迭代 Room 化）。

## 10. 打开问题
- AI 供应商是否固定（影响鉴权 Header）？  
- 是否需要离线降级（仅 OCR，不调模型）？  
- 是否需要多语言摘要/多时区日程？  
