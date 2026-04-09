# OpenClaw 2026.04.09 版本升级计划（完整版）

**创建日期**: 2026-04-09  
**当前版本**: 2026.3.13 → **目标版本**: 2026.4.8+  
**执行环境**: Docker 容器 (openclaw-fast) on Ubuntu  
**文档来源**: 飞书文档《OpenClaw 服务器升级指南（跨版本）》

---

## ⚠️ 升级前必做：备份三件事

不管从哪个版本升，先做这三步：

```bash
# 1. 备份配置文件
cp ~/.openclaw/openclaw.json ~/.openclaw/backup/openclaw.json.bak.$(date +%Y%m%d)

# 2. 备份 workspace（包含 MEMORY.md、Skills、自定义脚本）
tar -czvf ~/.openclaw/backup/workspace.bak.$(date +%Y%m%d).tar.gz ~/.openclaw/workspace/

# 3. 记录已安装插件
docker exec openclaw-fast bash -c "openclaw plugins list" > ~/.openclaw/backup/plugins-list.$(date +%Y%m%d).txt

# 截图存档，升级后逐一验证
```

---

## 📋 跨版本踩坑汇总（10 大坑）

### 坑 1: exec 命令突然需要审批 ⭐⭐⭐⭐⭐（最影响使用）

**症状**: 之前可直接运行的脚本/命令，升级后弹出审批提示，定时任务执行失败

**原因**: 3.31 版本引入 exec 安全机制，默认给 exec 工具上锁

**解决方案**: 修改 `~/.openclaw/exec_approvals.json`

```json
{
  "version": 1,
  "defaults": {
    "security": "full",
    "ask": "off"
  }
}
```

**或者设置为 allowlist 模式**（推荐生产环境）:

```json
{
  "version": 1,
  "defaults": {
    "security": "allowlist",
    "ask": "on-miss"
  },
  "allowlist": [
    "git clone",
    "git pull",
    "docker build",
    "docker run",
    "npm install",
    "tar -czvf",
    "cp -r",
    "mkdir -p"
  ]
}
```

---

### 坑 2: 旧配置字段直接失效 ⭐⭐⭐⭐

**症状**: 升级后某些配置不生效，报未知字段错误

**原因**: 配置 schema 更新，旧字段被废弃

**常见废弃字段**:

| 旧字段 | 新字段 | 说明 |
|--------|--------|------|
| `agents.defaults.model` | `agents.defaults.models.primary` | 模型选择结构变化 |
| `channels.feishu.accounts.*.enabled` | 移到 `groupPolicy` | 群组策略调整 |
| `plugins.load.paths` | `plugins.entries` | 插件加载机制重构 |

**解决方案**: 对照新版 `openclaw.json` 模板逐项检查

---

### 坑 3: 插件加载失败 ⭐⭐⭐⭐

**症状**: 升级后插件不加载，报错 "Cannot find module" 或 "Plugin not found"

**原因**: 插件路径变化或依赖不兼容

**解决方案**:

```bash
# 1. 检查插件目录
ls -la ~/.openclaw/extensions/
ls -la ~/.openclaw/workspace/skills/

# 2. 重新安装插件
docker exec openclaw-fast bash -c "openclaw plugins install <plugin-name>"

# 3. 检查 openclaw.json 中的 plugins.entries 配置
```

---

### 坑 4: web_fetch / web_search 全部报错 ⭐⭐⭐

**症状**: 所有外网访问工具超时或返回错误

**原因**: 
- 内网服务器无法访问外网
- API Key 失效或未配置

**解决方案**:

```json
// openclaw.json
{
  "env": {
    "BRAVE_API_KEY": "你的 Brave API key",
    "PERPLEXITY_API_KEY": "你的 Perplexity API key"
  }
}
```

**内网服务器替代方案**:
- 使用 browser 工具（需要 Windows 节点）
- 通过@小发 转交外网任务

---

### 坑 5: 记忆系统重置或数据丢失 ⭐⭐⭐⭐⭐

**症状**: 升级后 MEMORY.md 内容丢失，或记忆搜索不工作

**原因**: 
- 记忆插件未正确迁移
- 数据库路径变化

**解决方案**:

```bash
# 1. 备份记忆数据库
cp ~/.openclaw/memos-local/memos.db ~/.openclaw/backup/

# 2. 检查插件配置
cat ~/.openclaw/openclaw.json | grep -A10 '"plugins"'

# 3. 确保 memos-local 插件已安装
docker exec openclaw-fast bash -c "openclaw plugins list | grep memos"
```

---

### 坑 6: Cron 任务 ID 丢失 ⭐⭐⭐

**症状**: 定时任务不执行，或重复执行

**原因**: 任务 ID 生成逻辑变化

**解决方案**:

```bash
# 1. 检查 HEARTBEAT.md
cat ~/.openclaw/workspace/HEARTBEAT.md

# 2. 重启 Gateway 后验证
docker exec openclaw-fast bash -c "openclaw gateway status"

# 3. 手动触发一次任务
```

---

### 坑 7: Matrix DB 策略配置失效 ⭐⭐

**症状**: 数据库连接失败或查询缓慢

**原因**: 连接池配置变化

**解决方案**: 检查并更新数据库配置

---

### 坑 8: /reset 和 /new 后模型被意外锁定 ⭐⭐⭐

**症状**: 使用 /reset 或 /new 命令后，模型固定在某个型号无法切换

**原因**: 模型缓存未清除

**解决方案**:

```bash
# 清除模型缓存
docker exec openclaw-fast bash -c "rm -rf ~/.openclaw/cache/models"

# 重启 Gateway
docker exec openclaw-fast bash -c "openclaw gateway restart"
```

---

### 坑 9: Claude CLI 认证问题 ⭐⭐

**症状**: Claude Code 相关功能认证失败

**原因**: API Key 过期或权限变化

**解决方案**: 更新认证配置

---

### 坑 10: Windows 更新构建失败（容器不影响） ⭐⭐

**症状**: Windows 节点更新 OpenClaw 时构建失败

**原因**: 环境变量或路径问题

**解决方案**: 在容器内构建，或检查 Windows 环境变量

---

## 🚀 Ubuntu 服务器容器升级流程

### Step 1: 备份

```bash
cd /root/.openclaw
mkdir -p backup/upgrade-2026.04.09

# 备份配置
cp openclaw.json backup/upgrade-2026.04.09/

# 备份 workspace
tar -czvf backup/upgrade-2026.04.09/workspace.tar.gz workspace/

# 备份插件
tar -czvf backup/upgrade-2026.04.09/extensions.tar.gz extensions/ 2>/dev/null || true

# 备份记忆数据库
cp memos-local/memos.db backup/upgrade-2026.04.09/ 2>/dev/null || true

# 记录当前状态
docker inspect openclaw-fast > backup/upgrade-2026.04.09/container-inspect.json
docker exec openclaw-fast bash -c "openclaw --version" > backup/upgrade-2026.04.09/version.txt
docker exec openclaw-fast bash -c "openclaw plugins list" > backup/upgrade-2026.04.09/plugins.txt
```

### Step 2: 检查当前状态

```bash
# 容器状态
docker ps | grep openclaw

# 磁盘空间
df -h

# 内存使用
free -h

# 当前版本
docker exec openclaw-fast bash -c "openclaw --version"
```

### Step 3: 更新 Docker 容器

```bash
# 拉取最新镜像（或本地构建）
docker pull openclaw/openclaw:latest
# 或
docker build -t openclaw:2026.4.9 .

# 停止旧容器
docker stop openclaw-fast

# 删除旧容器（数据已备份，安全）
docker rm openclaw-fast

# 启动新容器
docker run -d \
  --name openclaw-fast \
  --restart unless-stopped \
  --network host \
  -v /root/.openclaw:/root/.openclaw \
  -v /root/.openclaw/workspace:/root/.openclaw/workspace \
  -e TZ=Asia/Shanghai \
  openclaw:2026.4.9
```

### Step 4: 修改配置

```bash
# 1. 更新 openclaw.json 版本号
cd /root/.openclaw
cp openclaw.json openclaw.json.bak

# 编辑配置文件，更新以下字段：
# - meta.lastTouchedVersion: "2026.4.9"
# - meta.lastTouchedAt: "2026-04-09T00:00:00.000Z"
# - wizard.lastRunVersion: "2026.4.9"

# 2. 配置 exec 审批（关键！）
cat > exec_approvals.json << 'EOF'
{
  "version": 1,
  "defaults": {
    "security": "full",
    "ask": "off"
  }
}
EOF

# 3. 验证配置
python3 -m json.tool openclaw.json > /dev/null && echo "JSON 语法正确"
python3 -m json.tool exec_approvals.json > /dev/null && echo "exec_approvals.json 语法正确"
```

### Step 5: 验证

```bash
# 容器状态
docker ps | grep openclaw

# 查看日志
docker logs openclaw-fast --tail 100

# 检查版本
docker exec openclaw-fast bash -c "openclaw --version"

# 检查 Gateway
docker exec openclaw-fast bash -c "openclaw gateway status"

# 检查插件
docker exec openclaw-fast bash -c "openclaw plugins list"

# 测试 exec（关键！）
docker exec openclaw-fast bash -c "echo 'test'"

# 测试飞书
docker exec openclaw-fast bash -c "openclaw message send --channel feishu --target 'chat:oc_8150d011724737c376f765bf3bfaf2a1' --message '升级完成测试'"

# 检查记忆系统
docker exec openclaw-fast bash -c "ls -la ~/.openclaw/memos-local/"
```

---

## 🔄 回滚计划

如果升级失败，执行回滚：

```bash
# 停止新容器
docker stop openclaw-fast
docker rm openclaw-fast

# 恢复配置文件
cd /root/.openclaw
cp backup/upgrade-2026.04.09/openclaw.json .
cp backup/upgrade-2026.04.09/exec_approvals.json . 2>/dev/null || true

# 使用旧镜像启动
docker run -d \
  --name openclaw-fast \
  --restart unless-stopped \
  --network host \
  -v /root/.openclaw:/root/.openclaw \
  -v /root/.openclaw/workspace:/root/.openclaw/workspace \
  -e TZ=Asia/Shanghai \
  openclaw:2026.3.13

# 验证回滚
docker logs openclaw-fast --tail 50
```

---

## ✅ 检查清单

升级完成后，请确认：

- [ ] 容器正常运行
- [ ] Gateway 状态正常 (`openclaw gateway status`)
- [ ] 飞书机器人可响应（@机器人测试）
- [ ] exec 命令无需审批（测试 `echo test`）
- [ ] 插件加载正常 (`openclaw plugins list`)
- [ ] 模型调用正常（测试对话）
- [ ] 工作区文件完整 (`ls workspace/`)
- [ ] 记忆系统正常 (检查 `memos-local/`)
- [ ] 定时任务正常 (检查 `HEARTBEAT.md`)
- [ ] web_fetch/web_search 可用（如需要）

---

## 📝 openclaw.json 关键配置变更

### 2026.4.x 版本主要变化

```json
{
  "meta": {
    "lastTouchedVersion": "2026.4.9",
    "lastTouchedAt": "2026-04-09T00:00:00.000Z"
  },
  "agents": {
    "defaults": {
      "model": {
        "primary": "bailian/qwen3.5-plus"
      },
      "models": {
        "bailian/qwen3.5-plus": {
          "alias": "qwen"
        }
      }
    }
  },
  "plugins": {
    "allow": ["feishu", "memos-local-openclaw-plugin"],
    "entries": {
      "feishu": {"enabled": true},
      "memos-local-openclaw-plugin": {"enabled": true}
    }
  }
}
```

---

**文档生成时间**: 2026-04-09 17:00  
**生成者**: 小发 🔧  
**参考文档**: 飞书《OpenClaw 服务器升级指南（跨版本）》
