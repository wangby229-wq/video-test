# MiniMax Token Plan Skills 调研报告

**原始项目**: https://github.com/nmvr2600/minimax-token-plan-skills  
**调研日期**: 2026-04-09  
**调研人**: 小发 (OpenClaw-Dev)

---

## 📁 目录结构

```
minimax-github-sync/
├── minimax-token-plan-skills/    # 原始项目源码（clone）
├── ANALYSIS_REPORT.md            # 详细调研报告
└── README.md                     # 本文件
```

---

## 📊 核心发现

### 项目概况

| 维度 | 详情 |
|------|------|
| **项目名称** | MiniMax Token Plan Skills |
| **目标平台** | Claude Code / OpenClaw |
| **开发语言** | TypeScript + Bun |
| **技能数量** | 5 个 |
| **API 提供商** | MiniMax (国内大模型 API) |
| **许可证** | MIT |

### 技能列表

| 技能 | 功能 | 优先级 |
|------|------|--------|
| `minimax-search` | 联网搜索 | 🔴 高 |
| `minimax-speech` | 语音合成 (TTS) | 🔴 高 |
| `minimax-image` | 文生图 | 🟡 中 |
| `minimax-image-analysis` | 图片分析/OCR | 🟡 中 |
| `minimax-usage` | Token 用量查询 | 🟢 低 |

---

## ✅ OpenClaw 兼容性

| 维度 | 兼容性 | 说明 |
|------|--------|------|
| 技能定义格式 | ✅ 完全兼容 | YAML frontmatter + Markdown |
| 脚本语言 | ⚠️ 需 Bun 运行时 | TypeScript (Bun 原生支持) |
| 环境变量 | ✅ 兼容 | `MINIMAX_API_KEY` |
| 触发机制 | ✅ 兼容 | description 字段驱动 |

---

## 💡 适配建议

**推荐方案**: 最小改动适配（保持 TypeScript + Bun）

**预期工作量**: 3-5 天完成全部适配和测试

**实施步骤**:
1. 安装 Bun: `curl -fsSL https://bun.sh/install | bash`
2. 复制技能到 OpenClaw skills 目录
3. 设置 `MINIMAX_API_KEY` 环境变量
4. 测试并优化触发词

---

## 📄 详细报告

查看完整分析报告：[ANALYSIS_REPORT.md](./ANALYSIS_REPORT.md)

---

## 🔗 相关链接

- 原始项目：https://github.com/nmvr2600/minimax-token-plan-skills
- MiniMax 官网：https://www.minimaxi.com
- Bun 官网：https://bun.sh

---

**报告生成时间**: 2026-04-09  
**分析师**: 小发 🔧
