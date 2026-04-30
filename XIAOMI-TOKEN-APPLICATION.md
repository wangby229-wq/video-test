# 小米 MiMo 100万亿Token激励计划 - 申请资料

> 申请地址：https://100t.xiaomimimo.com  
> 申请时间：2026年4月30日  
> 申请人：wangby229  

---

## 一、个人简介

我是一名全栈开发者，长期使用AI编程工具进行项目开发。在过去一年中，我利用Kimi Code、Claude Code、MiniMax等AI Agent工具完成了多个商业级项目的开发、部署和运维，积累了丰富的AI辅助开发经验。

---

## 二、常用AI开发/Agent工具

| 工具名称 | 用途 | 使用时长 |
|---------|------|---------|
| **Kimi Code** | 代码生成、项目重构、Bug修复 | 12个月 |
| **Claude Code** | 复杂逻辑开发、架构设计 | 8个月 |
| **MiniMax** | 多模态开发（文本+图像） | 6个月 |
| **OpenClaw** | 多Agent协作系统（自研/定制） | 4个月 |
| **Cursor** | 前端页面快速开发 | 3个月 |

---

## 三、主要使用的模型

| 模型 | 平台 | 应用场景 |
|-----|------|---------|
| **MiMo-V2.5** | 小米MiMo | 通用代码生成、项目维护 |
| **Claude 3.5 Sonnet** | Anthropic | 复杂业务逻辑设计 |
| **Kimi k1.5** | Moonshot | 长文本处理、文档分析 |
| **MiniMax-Text-01** | MiniMax | 多模态内容生成 |
| **通义千问** | 阿里云 | 中文场景优化 |

---

## 四、使用Agent及AI工具的成果

### 4.1 会议录音转文字系统（wechat_meeting）

**项目概述**：基于Spring Boot + Vue 3 + MySQL的企业级会议录音转文字系统，集成企业微信登录、设备绑定管理和录音文件自动推送功能。

**技术栈**：
- 后端：Spring Boot 2.7.18、Spring Security、JWT、JPA、MySQL 8.0
- 前端：Vue 3、Vue Router、Element Plus、Axios、Vite
- AI集成：百度AI语音转文字SDK

**核心功能**：
1. 企业微信免登录 & 扫码登录双模式
2. 设备注册、绑定、解绑全生命周期管理
3. 会议录音上传、转写、管理
4. 录音完成自动推送至绑定用户
5. 管理员后台（用户/设备/会议管理）

**项目规模**：
- 后端代码：约8,000行Java
- 前端代码：约5,000行Vue/JS
- API接口：20+个RESTful接口
- 数据库表：10+张业务表

**GitHub地址**：https://github.com/wangby229-wq/video-test

---

### 4.2 OpenClaw 多Agent协作系统

**项目概述**：基于OpenClaw框架搭建的多Agent协作平台，实现Master-Agent分派、子Agent并行处理、飞书群聊Bot-to-Bot通信的完整工作流。

**系统架构**：
```
Master Agent (主控)
  ├── Analyst Agent (文章分析)
  ├── Researcher Agent (资料调研)
  ├── Coder Agent (代码开发)
  └── DocUploader Agent (文档上传)
```

**核心成果**：
1. **飞书Bot通信修复**：新增`before_tool_call` Hook，修复`@BotName`到`<at>`标签的转换，实现Master-子Agent无缝通信
2. **消息回传保障**：强化SOUL.md约束，确保子Agent完成任务后必发消息通知，新增验证步骤
3. **Whitelist修复**：解决Feishu openID视角差异问题，统一使用接收方视角ID
4. **IMA笔记映射系统**：建立IMA笔记与本地MD文件的统一映射表，实现链接反向查找
5. **微信文章提取修复**：修复硬编码链接Bug，实现动态URL提取

**部署环境**：腾讯云海外服务器 + Hostwinds VPS双节点

---

### 4.3 Token Plan 对比分析报告

完成4大AI平台（小米MiMo、Kimi Code、MiniMax、阿里云百炼）的Token套餐深度对比分析，基于实际项目（wechat_meeting）测算各平台处理能力和成本效益。

**关键发现**：
- 小米MiMo Standard：2亿Token/月，单价最低（¥0.00000495/Token）
- 同等项目处理能力：小米MiMo可处理400个项目/月，远超阿里云百炼（15-20个）

---

## 五、附件证明

### 5.1 项目代码仓库
- **主仓库**：https://github.com/wangby229-wq/video-test
- 包含：完整源码、部署脚本、配置文件、文档

### 5.2 部署证明
- **阿里云演示平台**：47.96.21.34（yunpindemo）
- **腾讯云OpenClaw节点**：43.155.206.116
- **Hostwinds海外VPS**：192.255.198.252

### 5.3 技术文档
- `README.md`：项目完整说明
- `DEPLOYMENT.md`：生产环境部署指南
- `DATABASE_INIT.md`：数据库初始化文档
- `develop_task.md`：开发任务规划（P0/P1/P2优先级）

---

## 六、申请理由

1. **真实开发者**：拥有完整的全栈项目开发和部署经验，非"薅羊毛"用户
2. **活跃使用者**：每月使用AI工具处理多个项目，Token消耗量大
3. **生态贡献者**：积极参与开源项目，修复Bug并回馈社区（OpenClaw多Agent协作修复）
4. **小米MiMo忠实用户**：已在实际开发中使用MiMo-V2.5，体验良好
5. **长期规划**：计划基于MiMo模型开发更多AI应用，包括智能客服、文档自动处理等

---

## 七、联系方式

- GitHub：https://github.com/wangby229-wq
- 邮箱：wangby229@163.com

---

*本申请资料由AI Agent辅助整理生成*
