# OpenClaw 2062.04.09 版本升级计划

**创建日期**: 2026-04-09  
**当前版本**: 2026.3.13 → **目标版本**: 2062.04.09  
**执行环境**: Docker 容器 (openclaw-fast)

---

## ⚠️ 注意事项

由于提供的两篇参考文章（微信公众号和飞书文档）需要登录访问，本计划基于 OpenClaw 标准升级流程编写。

**请提供文章核心内容后，我会完善此计划。**

---

## 📋 升级清单

### 阶段 1: 准备工作

- [ ] 备份当前配置和数据
- [ ] 确认 Docker 环境正常
- [ ] 检查磁盘空间
- [ ] 记录当前版本号

### 阶段 2: openclaw.json 配置修改

- [ ] 更新 `meta.lastTouchedVersion` 和 `meta.lastTouchedAt`
- [ ] 更新 `wizard.lastRunVersion`
- [ ] 检查并更新 `models.providers` 配置
- [ ] 检查并更新 `agents.defaults` 配置
- [ ] 检查并更新 `plugins.installs` 版本
- [ ] 验证配置语法

### 阶段 3: Git 源码拉取

- [ ] Clone OpenClaw 官方仓库
- [ ] Checkout 到 2062.04.09 标签/分支
- [ ] 安装依赖
- [ ] 运行构建前检查

### 阶段 4: Docker 镜像编译

- [ ] 准备 Dockerfile
- [ ] 构建新镜像
- [ ] 标记镜像版本
- [ ] 测试镜像

### 阶段 5: 部署和验证

- [ ] 停止旧容器
- [ ] 启动新容器
- [ ] 验证服务正常
- [ ] 回滚计划（如有问题）

---

## 🔧 详细步骤

### 步骤 1: 备份当前配置

```bash
# 进入工作目录
cd /root/.openclaw

# 创建备份目录
mkdir -p /root/.openclaw/backup/2062.04.09-pre-upgrade

# 备份配置文件
cp openclaw.json backup/2062.04.09-pre-upgrade/
cp -r workspace backup/2062.04.09-pre-upgrade/

# 备份容器配置
docker inspect openclaw-fast > backup/2062.04.09-pre-upgrade/container-inspect.json

# 验证备份
ls -lh backup/2062.04.09-pre-upgrade/
```

### 步骤 2: 检查当前状态

```bash
# 检查 Docker 容器状态
docker ps | grep openclaw

# 检查磁盘空间
df -h

# 检查当前版本
docker exec openclaw-fast bash -c "openclaw --version"

# 查看当前配置
docker exec openclaw-fast bash -c "cat /root/.openclaw/openclaw.json | head -50"
```

### 步骤 3: 更新 openclaw.json

```bash
# 编辑配置文件
cd /root/.openclaw
cp openclaw.json openclaw.json.bak

# 使用 sed 或编辑器更新版本信息
# 或者创建新的配置文件
cat > openclaw.json.new << 'EOF'
{
  "meta": {
    "lastTouchedVersion": "2062.04.09",
    "lastTouchedAt": "2026-04-09T00:00:00.000Z"
  },
  "wizard": {
    "lastRunAt": "2026-04-09T00:00:00.000Z",
    "lastRunVersion": "2062.04.09",
    "lastRunCommand": "upgrade",
    "lastRunMode": "local"
  },
  // ... 其他配置保持不变或根据需求更新
}
EOF

# 验证 JSON 语法
python3 -m json.tool openclaw.json.new > /dev/null && echo "JSON 语法正确"

# 替换原文件（确认无误后）
mv openclaw.json.new openclaw.json
```

### 步骤 4: 拉取 Git 源码

```bash
# 创建源码目录
mkdir -p /root/.openclaw/src
cd /root/.openclaw/src

# Clone OpenClaw 仓库（替换为实际仓库地址）
git clone https://github.com/openclaw/openclaw.git
cd openclaw

# 查看可用标签
git tag | grep "2062" | sort -V

# Checkout 到目标版本
git checkout v2062.04.09
# 或
git checkout 2062.04.09

# 验证版本
git describe --tags

# 安装依赖
npm install
# 或
pnpm install
# 或
yarn install
```

### 步骤 5: 编译 Docker 镜像

```bash
# 进入源码目录
cd /root/.openclaw/src/openclaw

# 查看 Dockerfile
cat Dockerfile

# 构建镜像
docker build -t openclaw:2062.04.09 .

# 或者使用多阶段构建
docker build --build-arg VERSION=2062.04.09 \
             --build-arg BUILD_DATE=$(date -u +"%Y-%m-%dT%H:%M:%SZ") \
             -t openclaw:2062.04.09 \
             -t openclaw:latest \
             .

# 验证镜像
docker images | grep openclaw
```

### 步骤 6: 停止旧容器并启动新容器

```bash
# 停止旧容器
docker stop openclaw-fast
docker rm openclaw-fast

# 记录原容器配置（用于重建）
# docker inspect openclaw-fast > container-config.json

# 启动新容器
docker run -d \
  --name openclaw-fast \
  --restart unless-stopped \
  --network host \
  -v /root/.openclaw:/root/.openclaw \
  -v /root/.openclaw/workspace:/root/.openclaw/workspace \
  -e TZ=Asia/Shanghai \
  openclaw:2062.04.09

# 或者使用 docker-compose
# cd /root/.openclaw/src/openclaw
# docker-compose up -d
```

### 步骤 7: 验证升级

```bash
# 检查容器状态
docker ps | grep openclaw

# 查看日志
docker logs openclaw-fast --tail 100

# 检查版本
docker exec openclaw-fast bash -c "openclaw --version"

# 检查 Gateway 状态
docker exec openclaw-fast bash -c "openclaw gateway status"

# 检查插件
docker exec openclaw-fast bash -c "openclaw plugin list"

# 测试飞书连接
docker exec openclaw-fast bash -c "openclaw message send --channel feishu --target \"chat:oc_8150d011724737c376f765bf3bfaf2a1\" --message \"升级完成测试\""
```

---

## 🔄 回滚计划

如果升级失败，执行回滚：

```bash
# 停止新容器
docker stop openclaw-fast
docker rm openclaw-fast

# 使用旧镜像启动
docker run -d \
  --name openclaw-fast \
  --restart unless-stopped \
  --network host \
  -v /root/.openclaw:/root/.openclaw \
  -v /root/.openclaw/workspace:/root/.openclaw/workspace \
  -e TZ=Asia/Shanghai \
  openclaw:2026.3.13

# 恢复配置文件
cd /root/.openclaw
cp backup/2062.04.09-pre-upgrade/openclaw.json .

# 验证回滚
docker logs openclaw-fast --tail 50
```

---

## 📊 版本对比

| 配置项 | 当前版本 (2026.3.13) | 目标版本 (2062.04.09) |
|--------|---------------------|----------------------|
| meta.lastTouchedVersion | 2026.3.13 | 2062.04.09 |
| wizard.lastRunVersion | 2026.3.12 | 2062.04.09 |
| plugins.installs | 待确认 | 待确认 |

---

## ✅ 检查清单

升级完成后，请确认：

- [ ] 容器正常运行
- [ ] Gateway 状态正常
- [ ] 飞书机器人可响应
- [ ] 插件加载正常
- [ ] 模型调用正常
- [ ] 工作区文件完整
- [ ] 记忆系统正常
- [ ] 定时任务正常

---

## 📝 待确认事项

请提供两篇文章的核心内容，以便完善以下方面：

1. **openclaw.json 具体变更**
   - 新增配置项
   - 废弃配置项
   - 配置格式变化

2. **编译流程变化**
   - 新的构建参数
   - 依赖变化
   - 环境变量要求

3. **部署注意事项**
   -  Breaking Changes
   - 迁移脚本
   - 兼容性要求

---

**文档生成时间**: 2026-04-09 16:50  
**生成者**: 小发 🔧
