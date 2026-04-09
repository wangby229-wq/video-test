# MiniMax Token Plan Skills - OpenClaw 适配分析报告

**分析日期**: 2026-04-09  
**分析人**: 小发 (OpenClaw-Dev)  
**项目来源**: https://github.com/nmvr2600/minimax-token-plan-skills

---

## 📊 执行摘要

### 项目概况

| 维度 | 详情 |
|------|------|
| **项目名称** | MiniMax Token Plan Skills |
| **目标平台** | Claude Code / OpenClaw |
| **开发语言** | TypeScript + Bun |
| **技能数量** | 5 个 |
| **API 提供商** | MiniMax (国内大模型 API) |
| **许可证** | MIT |

### 核心功能

| 技能 | 功能 | 优先级 |
|------|------|--------|
| `minimax-search` | 联网搜索 | 🔴 高 |
| `minimax-speech` | 语音合成 (TTS) | 🔴 高 |
| `minimax-image` | 文生图 | 🟡 中 |
| `minimax-image-analysis` | 图片分析/OCR | 🟡 中 |
| `minimax-usage` | Token 用量查询 | 🟢 低 |

---

## 🏗️ 项目结构分析

```
minimax-token-plan-skills/
├── skills/                          # 技能定义目录
│   ├── minimax-image/               # 文生图技能
│   │   ├── SKILL.md                 # OpenClaw 技能定义
│   │   └── scripts/generate.ts      # 实现脚本
│   ├── minimax-speech/              # 语音合成技能
│   │   ├── SKILL.md                 # OpenClaw 技能定义
│   │   ├── scripts/tts.ts           # 主脚本
│   │   ├── scripts/voices.ts        # 音色查询
│   │   └── voices.md                # 音色列表
│   ├── minimax-search/              # 联网搜索技能
│   │   ├── SKILL.md                 # OpenClaw 技能定义
│   │   └── scripts/search.ts        # 实现脚本
│   ├── minimax-image-analysis/      # 图片分析技能
│   │   ├── SKILL.md                 # OpenClaw 技能定义
│   │   └── scripts/analyze.ts       # 实现脚本
│   └── minimax-usage/               # 用量查询技能
│       ├── SKILL.md                 # OpenClaw 技能定义
│       └── scripts/query.ts         # 实现脚本
├── scripts/vendor/minimax-core/     # 共享核心库
│   ├── index.ts                     # 导出入口
│   ├── api.ts                       # API 工具函数
│   ├── errors.ts                    # 错误类定义
│   └── types.ts                     # TypeScript 类型
├── README_CN.md                     # 中文文档
├── CLAUDE.md                        # 开发指南
└── .claude-plugin/                  # Claude 插件配置
```

---

## ✅ OpenClaw 兼容性评估

### 技能定义格式

**完全兼容** ✅

项目中的 `SKILL.md` 使用 YAML frontmatter + Markdown 格式，与 OpenClaw 技能系统完全一致：

```yaml
---
name: minimax-search
description: 当用户需要搜索网络信息、查找最新资料...时触发
---

# MiniMax Web Search
...
```

### 脚本语言

**需 Bun 运行时** ⚠️

- 脚本使用 TypeScript + Bun 原生 API
- OpenClaw 默认使用 Node.js
- **解决方案**: 安装 Bun 或转换为 Node.js

### 环境变量

**完全兼容** ✅

```bash
MINIMAX_API_KEY      # 必需
MINIMAX_API_HOST     # 可选，默认 https://api.minimaxi.com
```

### 触发机制

**完全兼容** ✅

SKILL.md 的 `description` 字段包含详细的触发词说明，OpenClaw 可直接使用。

---

## 💡 适配方案

### 方案 A: 最小改动（推荐）⭐

**保持 TypeScript + Bun 不变**

#### 步骤

1. **安装 Bun**（如果未安装）
   ```bash
   curl -fsSL https://bun.sh/install | bash
   ```

2. **复制技能到 OpenClaw**
   ```bash
   cp -r minimax-token-plan-skills/skills/* ~/.openclaw/workspace/skills/
   cp -r minimax-token-plan-skills/scripts/vendor/minimax-core \
       ~/.openclaw/workspace/skills/_shared/minimax-core/
   ```

3. **更新 SKILL.md metadata**（可选，添加 OpenClaw 扩展）
   ```yaml
   ---
   name: minimax-search
   description: ...
   metadata:
     openclaw:
       requires:
         anyBins: ["bun"]
       primaryEnv: "MINIMAX_API_KEY"
   ---
   ```

4. **测试技能**
   ```bash
   cd ~/.openclaw/workspace/skills/minimax-search
   bun run scripts/search.ts "测试搜索"
   ```

#### 优点
- 改动最小
- 保持代码原貌
- 易于后续同步更新

#### 缺点
- 需要安装 Bun 运行时

---

### 方案 B: 转换为 Node.js

**完全迁移到 Node.js 生态**

#### 步骤

1. 将 Bun API 替换为 Node.js API
2. 添加 `package.json` 和依赖
3. 使用 `ts-node` 或编译为 JS

#### 优点
- 无需额外运行时
- 与 OpenClaw 现有技能一致

#### 缺点
- 改动较大
- 后续同步困难

---

## 🔧 实施计划

### 阶段 1: 环境准备（1 天）

- [ ] 确认 Bun 已安装
- [ ] 测试 Bun 版本 >= 1.0.0
- [ ] 设置 `MINIMAX_API_KEY` 环境变量

### 阶段 2: 技能部署（1-2 天）

- [ ] 复制技能到 OpenClaw skills 目录
- [ ] 复制共享核心库
- [ ] 更新 SKILL.md metadata

### 阶段 3: 测试验证（1-2 天）

- [ ] 测试 `minimax-search`（联网搜索）
- [ ] 测试 `minimax-speech`（语音合成）
- [ ] 测试 `minimax-image`（文生图）
- [ ] 测试 `minimax-image-analysis`（图片分析）
- [ ] 测试 `minimax-usage`（用量查询）

### 阶段 4: 集成优化（可选）

- [ ] 添加中文触发词优化
- [ ] 添加错误处理和本地化
- [ ] 创建使用文档

---

## ⚠️ 潜在问题

### 1. Bun 运行时依赖

**问题**: OpenClaw 默认使用 Node.js，脚本需要 Bun

**解决方案**:
```bash
# 安装 Bun
curl -fsSL https://bun.sh/install | bash

# 验证安装
bun --version
```

### 2. MiniMax API Key

**问题**: 需要有效的 MiniMax Token Plan 订阅

**解决方案**:
- 在 MiniMax 官网注册账户
- 完成实名认证
- 获取 API Key 并设置环境变量

### 3. 网络访问

**问题**: API 端点 `https://api.minimaxi.com` 需要网络访问

**解决方案**:
- 确认服务器可访问外网
- 如需代理，设置 `HTTP_PROXY` / `HTTPS_PROXY`

### 4. 路径引用

**问题**: 脚本中使用 `${CLAUDE_SKILL_DIR}` 环境变量

**解决方案**:
- 替换为 OpenClaw 技能路径
- 或使用相对路径

---

## 📋 技能详细分析

### minimax-search（联网搜索）⭐⭐⭐⭐⭐

**功能**: 通过 MiniMax 搜索 API 获取实时网络信息

**适配难度**: ⭐ 简单

**关键代码**:
```typescript
import { webSearch, getConfig } from "./scripts/search";

const { apiKey, apiHost } = getConfig();
const result = await webSearch(apiKey, apiHost, "搜索关键词");
```

**触发词**: "搜索一下"、"查一下"、"联网搜索"、"search for"

**输出格式**: JSON（搜索结果列表）

---

### minimax-speech（语音合成）⭐⭐⭐⭐⭐

**功能**: 文字转语音（TTS），支持 327 种音色

**适配难度**: ⭐⭐ 中等

**特性**:
- 自动选择同步/异步接口（3000 字阈值）
- 智能音色推荐
- 支持语速、音量、音调调节

**关键代码**:
```typescript
import { textToSpeech } from "./scripts/tts";

const output = await textToSpeech({
    text: "要合成的文本",
    voiceId: "female-tianmei",
    outputFile: "output.mp3"
});
```

**触发词**: "转成语音"、"生成音频"、"读这段文字"

---

### minimax-image（文生图）⭐⭐⭐⭐

**功能**: 根据文字描述生成高质量图片

**适配难度**: ⭐⭐ 中等

**特性**:
- 支持多种宽高比
- 支持图生图
- 批量生成

**触发词**: "生成图片"、"画一张图"、"create an image"

---

### minimax-image-analysis（图片分析）⭐⭐⭐⭐

**功能**: 分析图片内容，支持 OCR 文字提取

**适配难度**: ⭐⭐ 中等

**触发词**: "分析图片"、"提取文字"、"describe this image"

---

### minimax-usage（用量查询）⭐⭐

**功能**: 查询 MiniMax Token Plan 用量和余额

**适配难度**: ⭐ 简单

**触发词**: "查看用量"、"Token 余额"、"check usage"

---

## 🎯 最终建议

### 推荐方案：方案 A（最小改动）

**理由**:
1. 改动最小，风险低
2. 保持代码原貌，易于后续同步
3. Bun 安装简单，性能优秀
4. 5 个技能中 2 个高优先级，值得投入

### 实施优先级

1. **第一阶段**（立即实施）:
   - `minimax-search` - 联网搜索（高频需求）
   - `minimax-speech` - 语音合成（高频需求）

2. **第二阶段**（后续实施）:
   - `minimax-image` - 文生图
   - `minimax-image-analysis` - 图片分析

3. **第三阶段**（可选）:
   - `minimax-usage` - 用量查询

### 预期工作量

| 阶段 | 时间 | 内容 |
|------|------|------|
| 环境准备 | 1 天 | 安装 Bun、配置 API Key |
| 技能部署 | 1-2 天 | 复制、配置、测试 |
| 优化调整 | 1 天 | 触发词优化、错误处理 |
| **总计** | **3-5 天** | 完成全部适配 |

---

## 📝 下一步行动

如需开始适配，请确认：

1. ✅ 是否需要安装 Bun？
2. ✅ MiniMax API Key 是否已准备好？
3. ✅ 优先部署哪几个技能？

确认后我将开始实施适配工作。

---

**报告生成时间**: 2026-04-09 12:10  
**分析师**: 小发 🔧
