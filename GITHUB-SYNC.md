# GitHub 任务同步配置

## Agent 标识符

| Agent | 标识符 | 说明 |
|-------|--------|------|
| Trae (本机) | `wangby-trae` | Windows本地Trae AI |
| OpenClaw-Browser | `wangby-openclaw-win` | Windows上的OpenClaw (通过v2ray访问GitHub) |
| OpenClaw-Dev | `wangby-openclaw-overseas` | 海外服务器Ubuntu上的OpenClaw |

## GitHub 仓库

- **仓库**: https://github.com/wangby229-wq/video-test
- **任务文件**: `tasks/backlog.json`

## Git 操作命令

```bash
# 1. 拉取最新代码
git pull

# 2. 编辑任务文件
# 编辑 tasks/backlog.json

# 3. 提交 (必须带agent标识)
git add .
git commit -m "wangby-trae: [任务描述]"

# 4. 推送到GitHub
git push
```

## 任务文件格式

```json
{
  "tasks": [
    {
      "id": "001",
      "title": "任务标题",
      "description": "任务描述",
      "status": "todo|in_progress|done",
      "assignee": "wangby-trae",
      "created": "2026-04-03",
      "updated": "2026-04-03"
    }
  ],
  "meta": {
    "version": "1.0",
    "last_sync": "2026-04-03T10:45:00Z",
    "agents": ["wangby-trae", "wangby-openclaw-win", "wangby-openclaw-overseas"]
  }
}
```

## 状态说明

- `todo` - 待处理
- `in_progress` - 进行中
- `done` - 已完成

## 多Agent协作流程

1. Agent执行任务前先 `git pull` 拉取最新
2. 修改任务状态和内容
3. `git commit -m "agent标识: 操作内容"`
4. `git push`
5. 其他Agent通过 `git pull` 同步更新
