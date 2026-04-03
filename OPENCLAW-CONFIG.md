# OpenClaw 配置同步

> 不包含 API Key

## 模型配置

### MiniMax (minimax)
- **API**: https://api.minimaxi.com/v1
- **模型**:
  - MiniMax-M2.7 (推理, 256K context)
  - MiniMax-M2.5 (200K context)
  - MiniMax-M2.5-highspeed
  - MiniMax-M2.1
  - MiniMax-M2.1-lightning
  - MiniMax-M2

### Bailian (bailian)
- **API**: https://coding.dashscope.aliyuncs.com/v1
- **模型**:
  - qwen3.5-plus (推理, 1M context)
  - qwen3-coder-next (262K context)
  - kimi-k2.5 (262K context)

### Agent 默认模型
- **Primary**: bailian/qwen3.5-plus
- **Fallbacks**: minimax/MiniMax-M2.7, minimax/MiniMax-M2.5

## Skills (已安装)

| Skill | 说明 |
|-------|------|
| feishu-doc-manager | 飞书文档管理 |
| find-skills | 技能发现 |
| github | GitHub 操作 |
| knowledge | 本地知识库 |
| skillhub-preference | SkillHub 偏好 |
| summarize | 内容摘要 |
| tapd-skill | TAPD 项目管理 |
| tuanziguardianclaw | 安全防护 |
| video-download | 视频下载 |
| wechat-pack | 微信文章打包 |
| bilibili-analyze | Bilibili 分析 |
| image-handler | 图片处理 |
| web-content-fetcher | 网页内容抓取 |

## MCP Servers

| Server | 命令 |
|--------|------|
| tapd | /home/node/.openclaw/bin/uvx mcp-server-tapd |

## 运行时环境

- **Node**: v24.14.0
- **OS**: Linux 6.17.0-14-generic (x64)
- **工作区**: /home/node/.openclaw/workspace

## 更新于
2026-04-03T07:09:44Z
