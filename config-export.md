# OpenClaw-Dev 配置导出

**服务器**: 海外 Ubuntu 服务器  
**导出时间**: 2026-04-03  
**标识符**: `wangby-openclaw-overseas`

---

## 🤖 模型配置

### 默认模型
- **主模型**: `bailian/qwen3.5-plus` (别名: `qwen`)
- **代码模型**: `bailian/qwen3-coder-plus` (别名: `coder`)

### 可用模型列表

| 模型 ID | 名称 | 上下文窗口 | 最大 Tokens |
|---------|------|-----------|-------------|
| qwen3.5-plus | Qwen3.5-Plus | 1,000,000 | 65,536 |
| qwen3-max-2026-01-23 | Qwen3-Max-2026-01-23 | 262,144 | 65,536 |
| qwen3-coder-next | Qwen3-Coder-Next | 262,144 | 65,536 |
| qwen3-coder-plus | Qwen3-Coder-Plus | 1,000,000 | 65,536 |
| MiniMax-M2.5 | MiniMax-M2.5 | 1,000,000 | 65,536 |
| glm-5 | GLM-5 | 202,752 | 16,384 |
| glm-4.7 | GLM-4.7 | 202,752 | 16,384 |
| kimi-k2.5 | Kimi-K2.5 | 262,144 | 32,768 |

### 模型提供商
- **Provider**: `bailian` (阿里云百炼)
- **API**: `openai-compat`
- **Base URL**: `https://coding.dashscope.aliyuncs.com/v1`

> ⚠️ **注意**: API Key 已过滤，不在此文件中存储

---

## 📦 已安装 Skills

### 核心 Skills

| Skill 名称 | 说明 |
|-----------|------|
| `feishu-doc` | 飞书云文档操作 |
| `feishu-common` | 飞书通用工具 |
| `github` | GitHub 交互 (gh CLI) |
| `ima-skills` | 腾讯 IMA 云端 API |
| `ima-write-note` | IMA 笔记写入 |
| `memos-memory-guide` | 本地记忆系统 |
| `server-health` | 服务器健康监控 |
| `skill-creator` | 技能创建工具 |
| `skillnet` | 技能搜索引擎 (3130+ skills) |
| `summarize` | 内容总结 (web/PDF/图片/音频) |
| `web-content-fetcher` | 网页正文提取 |

### 脚本 Skills

| 文件 | 说明 |
|------|------|
| `analyze-file.js` | 文件分析脚本 |
| `auto-analyze.js` | 自动分析脚本 |

---

## 🔧 Agent 配置

### 默认设置
- **工作目录**: `/root/.openclaw/workspace`
- **上下文 Tokens**: 100,000
- **最大并发**: 4
- **子 Agent 并发**: 8

### 心跳配置
- **频率**: 每 30 分钟
- **活跃时间**: 08:00 - 23:00

### 上下文管理
- **模式**: `cache-ttl`
- **TTL**: 4 小时
- **压缩阈值**: 4,000 tokens

---

## 🔌 插件配置

### 已启用插件
| 插件 | 说明 |
|------|------|
| `feishu` | 飞书机器人 |
| `memos-local-openclaw-plugin` | 本地记忆插件 |

### 飞书配置
- **Bot 名称**: `OpenClaw-Dev`
- **Bot OpenId**: `ou_9962c0b4978758530cd1fae5d3374077`
- **群策略**: `allowlist`
- **需要提及**: `false`

> ⚠️ **注意**: App Secret 已过滤

---

## 🌐 Gateway 配置

- **模式**: `local`
- **认证**: `token`
- **可信代理**: `127.0.0.1`, `::1`

> ⚠️ **注意**: Gateway Token 已过滤

---

## 📝 使用说明

### 恢复配置
1. 复制此文件作为参考
2. 在目标环境的 `openclaw.json` 中填入对应配置
3. **重要**: 需要单独填入 API Key 和 Secret

### 安全提醒
- ❌ 不要将 API Key、Secret、Token 提交到 Git
- ✅ 使用环境变量或加密存储敏感信息
- ✅ 定期轮换密钥

---

**导出命令**:
```bash
# 导出配置 (过滤敏感信息)
cat /root/.openclaw/openclaw.json | jq 'del(.models.providers.bailian.apiKey, .channels.feishu.accounts.personal.appSecret, .gateway.auth.token)'

# 列出已安装 skills
ls -la /root/.openclaw/workspace/skills/
```
