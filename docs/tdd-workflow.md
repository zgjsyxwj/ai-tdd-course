# SDD + TDD 自主开发工作流 SOP

## 流程总览

```
用户输入业务需求（可以模糊）
         │
         ▼
┌─ SDD 阶段 ─────────────────────────────────┐
│                                             │
│  Step 1: Product Manager → spec.md          │
│  选择题提问，澄清需求，共创规格说明书           │
│           │ 用户批准                          │
│           ▼                                  │
│  Step 2: Chief Architect → plan.md          │
│  技术选型，系统架构，数据模型设计               │
│           │ 用户批准                          │
│           ▼                                  │
│  Step 3: Tech Lead → tasks.md               │
│  原子化任务分解，标注依赖关系和并行标记 [P]      │
│           │ 用户批准                          │
└───────────┼─────────────────────────────────┘
            ▼
┌─ TDD 阶段 ─────────────────────────────────┐
│                                             │
│  Step 4: TDD Execution                      │
│  按 tasks.md 顺序，逐个任务 Red→Green→Refactor│
│           │                                  │
│           ▼                                  │
│  Step 5: Spec Verification                  │
│  对照 spec.md 逐项验证实现完整性              │
│           │                                  │
│           ▼                                  │
│  Step 6: Summary                            │
│  汇报文件、测试、偏差                         │
└─────────────────────────────────────────────┘
```

**核心理念**：
- **SDD 3 步解决「做什么」和「怎么做」** — 3 个角色（PM → 架构师 → 技术组长）逐步细化
- **TDD 解决「做得对不对」** — Red-Green-Refactor 保证实现正确性
- **用户在 3 个节点介入**：批准 spec → 批准 plan → 批准 tasks

---

## Step 1: Product Manager → spec.md

### 触发方式

```
TDD 开发：[你的业务需求，可以很模糊]
```

### Claude 的行为

Claude 切换为**产品经理角色**，通过选择题提问帮助用户澄清需求：

- 每轮 3-5 个问题
- **所有问题必须是选择题**，每个选项列出优缺点
- 推荐选项后面加 **（推荐）**
- 多轮迭代，直到需求清晰

### 示例

```
用户：TDD 开发：做一个听书 APP，支持 AI 配音

Claude（PM 角色）：

**Q1: 目标平台？**
A) Android + iOS（推荐）
   优点：覆盖面广  缺点：开发成本高
B) 仅 Android
   优点：成本低  缺点：丢失 iOS 用户
C) Web 应用
   优点：跨平台  缺点：离线体验差

**Q2: TTS 引擎选型？**
A) 对接第三方 API（如 Azure TTS）（推荐）
   优点：音质好，开箱即用  缺点：有 API 成本
B) 本地推理（如 Coqui TTS）
   优点：无 API 成本  缺点：需要 GPU，音质一般
...
```

### 产出

问答完成后，Claude 生成 `spec.md` 并保存到 `docs/specs/[项目名].md`。

**用户审查并批准后**，进入 Step 2。

---

## Step 2: Chief Architect → plan.md

### Claude 的行为

Claude 切换为**首席架构师角色**，阅读已批准的 `spec.md`，输出技术实现方案 `plan.md`，保存在 `spec.md` 同一目录下。

### plan.md 包含

| 章节 | 内容 |
|------|------|
| 技术栈选型 | 语言、框架、数据库、第三方服务，附理由 |
| 系统架构 | 模块划分、分层结构、数据流 |
| 数据模型 | 表结构、关系、索引 |
| API 设计 | 路由、中间件、认证 |
| 关键技术决策 | 每个决策列出备选方案和取舍 |
| 风险评估 | 技术风险和应对措施 |

### 关键点

- plan.md 是 spec.md 的技术翻译——spec 说「做什么」，plan 说「怎么做」
- 技术选型必须给出理由，不能拍脑袋
- 如果 spec 中有技术上不可行的需求，在 plan 中标注并建议替代方案

**用户审查并批准后**，进入 Step 3。

---

## Step 3: Tech Lead → tasks.md

### Claude 的行为

Claude 切换为**技术组长角色**，阅读 `spec.md` 和 `plan.md`，将技术方案分解为**原子化任务列表** `tasks.md`，保存在项目根目录。

### tasks.md 格式

```markdown
# Tasks

## Phase 1: 项目初始化
- [ ] [P] Task 1: 创建项目骨架和 pom.xml
- [ ] [P] Task 2: 创建 application.yml 配置文件
- [ ] Task 3: 创建 IntegrationTest 基类 (依赖: Task 1)

## Phase 2: 用户模块
- [ ] Task 4: 创建 User 实体类 (依赖: Task 1)
- [ ] Task 5: 创建 UserRepository (依赖: Task 4)
- [ ] [P] Task 6: 创建 UserRequest DTO (依赖: Task 4)
- [ ] [P] Task 7: 创建 UserResponse DTO (依赖: Task 4)
- [ ] Task 8: 编写用户注册测试 + 实现 (依赖: Task 3, 5, 6, 7)
...
```

### 关键要求

1. **原子化**：每个任务只涉及**一个主要文件**的创建或修改
2. **并行标记 [P]**：无依赖的任务标记 `[P]`，可并行执行
3. **依赖关系**：有依赖的任务注明依赖哪些前置任务
4. **TDD 友好**：涉及业务逻辑的任务应可通过 Red-Green-Refactor 实现

**用户审查并批准后**，进入 Step 4。

---

## Step 4: TDD Execution

### Claude 的行为

按 `tasks.md` 的顺序，对每个任务执行：

1. **Red**: 写测试，运行，确认失败
2. **Green**: 写最小实现让测试通过（失败则自动修复，最多 3 次）
3. **Refactor**: 重构代码，保持测试绿灯
4. 在 `tasks.md` 中标记 `- [x]`

### 规则

- 一次一个任务，不跳步
- TDD 执行阶段不打断用户
- 每完成一组相关任务（一个 Phase），简要汇报进度
- 用 `Map.of` 构造测试请求体
- 测试数据遵守 spec 中的字段约束

---

## Step 5: Spec Verification

所有任务完成后，逐项对照 `spec.md` 验证：

```markdown
## Spec Verification

### Features
- [x] 用户注册 — 5 个场景全部覆盖
- [x] 用户查询 — 2 个场景全部覆盖
...

### Business Rules
- [x] BR1: username 唯一
- [x] BR2: 响应不返回密码

### Deviations
- 无
```

---

## Step 6: Summary

```markdown
## Summary

### Files Created/Modified
- src/main/java/.../User.java (new)
- ...

### Test Results
- 13 tests, all passed

### Spec Verification
- All passed, no deviations

### Key Documents
- docs/specs/xxx-spec.md
- docs/specs/xxx-plan.md
- tasks.md
```

---

## 最佳实践

### Do

- **需求可以模糊**：PM 阶段会通过选择题帮你补全
- **认真审查 spec**：这是最重要的质量门禁
- **认真审查 plan**：技术选型错误比代码 bug 代价更大
- **分功能出 spec**：大项目拆成多个独立功能，每个功能走一遍完整流程
- **文档存档**：spec.md + plan.md + tasks.md 长期保留

### Don't

- **不要跳过 SDD**：没有 spec 和 plan 就没有任务分解的依据
- **不要在 TDD 阶段改需求**：改需求应该回到 spec 阶段
- **不要一次做太多**：单次建议不超过 20 个原子任务
- **不要忽略 plan**：spec 到 tasks 之间缺了 plan，会导致技术方向混乱

---

## 踩坑记录

### 1. 没有规格就开始写代码

**后果**：字段、格式、校验规则全靠猜，测试写了也白写。
**正确做法**：PM 阶段用选择题澄清，生成 spec.md 后再动手。

### 2. 跳过技术方案直接分任务

**后果**：任务分解缺乏架构指导，实现过程中频繁推翻重来。
**正确做法**：先出 plan.md（架构师视角），再出 tasks.md（技术组长视角）。

### 3. 测试数据超过字段长度

**后果**：Data Truncation 错误。
**正确做法**：测试数据必须符合 spec 中定义的字段约束。

### 4. 用 text block JSON 构造请求体

**后果**：容易出格式错误，不可重构。
**正确做法**：使用 `Map.of("key", "value")`。
