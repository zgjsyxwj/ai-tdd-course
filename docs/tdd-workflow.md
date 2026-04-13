# SDD + TDD 自主开发工作流 SOP

## 流程总览

```
用户输入业务需求（可以模糊）
         │
         ▼
┌──────────────────────┐
│  Phase 1: Spec        │  Claude 起草需求规格说明书
│  (规格编写)            │  实体模型 + API 合约 + 校验 + 业务规则
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  Phase 2: Review      │  用户审查规格，批准/修改
│  (规格审查)            │  这是唯一的质量门禁
└──────────┬───────────┘
           │ 用户批准
           ▼
┌──────────────────────┐
│  Phase 3: Tasking     │  从规格机械推导 Task + AC
│  (任务分解)            │  用户轻量确认
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  Phase 4: TDD         │  逐个 AC 执行 Red → Green → Refactor
│  (TDD 执行)           │  无需用户干预
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  Phase 5: Verify      │  逐项对照规格检查实现完整性
│  (规格验证)            │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  Phase 6: Summary     │  汇报文件、测试、偏差
│  (完成汇报)            │
└──────────────────────┘
```

**核心理念**：
- **SDD 解决「做什么」** — 规格说明书是唯一真相源
- **TDD 解决「怎么做」** — Red-Green-Refactor 保证实现正确性
- **用户只在两个节点介入**：审查规格 + 确认 Tasking

---

## 一、环境准备（一次性配置）

### 1.1 安装依赖

```bash
# Java 21
brew install openjdk@21
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@21"' >> ~/.zshrc
source ~/.zshrc

# Docker (Testcontainers 需要)
brew install --cask docker
```

### 1.2 项目配置

1. 项目根目录放置 `CLAUDE.md`（SDD + TDD 协议）
2. 准备测试基类 `IntegrationTest.java`（Testcontainers + REST Assured）
3. 创建 `docs/specs/` 目录用于存放规格说明书

### 1.3 Zsh 快捷命令（可选）

```bash
# SDD + TDD 自动驾驶
tdd() {
  if [ -z "$1" ]; then
    echo "用法: tdd '你的需求'"
    return 1
  fi
  claude -p "TDD 开发：$*"
}
```

---

## 二、Phase 1: Specification（规格编写）

### 触发方式

```
TDD 开发：实现用户管理系统，支持注册、查询、列表、修改密码、删除。
```

### Claude 的行为

Claude **不会逐个追问细节**，而是直接输出一份完整的规格说明书：

```markdown
# 用户管理系统 需求规格说明

## 1. 概述
- 业务背景：系统需要基础的用户管理模块
- 目标：提供用户 CRUD 的 REST API
- 范围：包含注册/查询/列表/改密/删除，不含登录认证

## 2. 实体模型

### User
| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | int | PK, 自增 | 主键 |
| username | varchar(50) | 必填, 唯一 | 用户名 |
| pwd | varchar(100) | 必填, 最小6位 | 密码 |

## 3. API 规格

### 3.1 POST /api/users — 用户注册
...（完整的 Request/Response/Error 定义）

## 4. 业务规则
- BR1: username 全局唯一
- BR2: 响应不返回 pwd

## 待确认问题
1. username 的最大长度？
2. 删除是硬删除还是软删除？
```

### 关键点

- 规格覆盖所有细节：字段、类型、约束、每个状态码的触发条件
- 如果需求信息不足，在规格末尾列出**待确认问题清单**，一次性向用户确认
- 模板参见 `docs/spec-template.md`

---

## 三、Phase 2: Spec Review（规格审查）

### 用户操作

用户审查规格说明书，可以：

- **批准**：回复"确认"或"批准"，进入 Tasking
- **修改**：指出需要修改的部分，Claude 更新规格后重新提交
- **补充**：添加遗漏的需求

### 为什么这是唯一的质量门禁

| 阶段 | 旧流程 | 新流程 |
|------|--------|--------|
| 需求理解 | 碎片化追问，容易遗漏 | 一次性输出完整规格 |
| Tasking | 需要用户仔细审查每个 AC | 从规格自动推导，轻量确认 |
| TDD 执行 | 可能因需求不清导致返工 | 规格已批准，无歧义 |

**规格批准后，后续所有工作都有据可依。**

---

## 四、Phase 3: Tasking（任务分解）

### 推导规则

从规格机械推导，不需要创造性判断：

| 规格内容 | 推导为 |
|----------|--------|
| 每个 API 端点 | 1 个 Task |
| 每个 Success Response | 1 个 AC |
| 每个 Error Response | 1 个 AC |
| 每个独立的业务规则 | 1 个 AC（如未被上述覆盖）|

### 示例输出

```markdown
## Tasking 分解（基于规格 v1.0）

### Task 1: POST /api/users — 用户注册
- [ ] AC1: 有效数据注册成功返回 201 + {id, username} → should_register_user_successfully
- [ ] AC2: username 为空返回 400 → should_return_400_when_username_is_blank
- [ ] AC3: pwd 为空返回 400 → should_return_400_when_pwd_is_blank
- [ ] AC4: pwd 少于6位返回 400 → should_return_400_when_pwd_too_short
- [ ] AC5: username 已存在返回 409 → should_return_409_when_username_exists

### Task 2: GET /api/users/{id} — 查询用户
- [ ] AC1: 查询存在的用户返回 200 → should_get_user_by_id
- [ ] AC2: 查询不存在的用户返回 404 → should_return_404_when_user_not_found
...
```

用户轻量确认后，进入 TDD 执行。

---

## 五、Phase 4: TDD Execution（TDD 执行）

### 对每个 AC 执行 Red-Green-Refactor

**Step 1 - Red**

Claude 根据规格编写测试，使用 `Map.of` 构造参数：

```java
@Test
void should_register_user_successfully() {
    given()
        .body(Map.of("username", "john", "pwd", "123456"))
    .when()
        .post("/api/users")
    .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("username", equalTo("john"))
        .body("pwd", equalTo(null));
}
```

运行测试，确认失败。

**Step 2 - Green**

编写最小生产代码让测试通过。

**Step 3 - Refactor**

有代码异味则重构，确保测试仍然通过。

**Step 4 - 打勾，继续下一个 AC**

### 关键规则

- 一次只做一个 AC，不跳步
- 测试失败时自动读日志修复，不打断用户
- 每完成一个 Task 简要汇报

---

## 六、Phase 5: Spec Verification（规格验证）

所有 AC 完成后，Claude 逐项对照规格检查：

```markdown
## 规格验证

### 实体模型
- [x] User 表字段与规格一致（id, username, pwd）

### API 端点
- [x] POST /api/users — 5 个场景全部覆盖
- [x] GET /api/users/{id} — 2 个场景全部覆盖
- [x] GET /api/users — 1 个场景覆盖
- [x] PUT /api/users/{id} — 3 个场景全部覆盖
- [x] DELETE /api/users/{id} — 2 个场景全部覆盖

### 业务规则
- [x] BR1: username 唯一 — 通过 UNIQUE 约束 + Service 检查
- [x] BR2: 响应不返回 pwd — UserResponse 只含 id, username
- [x] BR3: 硬删除 — deleteById

### 偏差记录
- 无
```

---

## 七、Phase 6: Summary（完成汇报）

```markdown
## 完成汇报

### 修改/创建的文件
- src/main/java/.../User.java (新建)
- src/main/java/.../UserController.java (新建)
- ...

### 测试结果
- 总计 13 个测试，全部通过
- 覆盖 5 个 API 端点，13 个验收条件

### 规格验证
- 全部通过，无偏差

### 规格说明书
- docs/specs/user-management.md
```

---

## 八、实战演示

### 用户输入

```
TDD 开发：实现用户管理系统，支持注册、查询、列表、修改密码、删除。
```

### 完整流程

| 阶段 | 用户操作 | Claude 操作 | 产出 |
|------|----------|-------------|------|
| Phase 1 | 等待 | 起草规格说明书 | `docs/specs/user-management.md` |
| Phase 2 | 审查规格，回复"username varchar(10), pwd varchar(30)" | 更新规格 | 规格 v1.1 |
| Phase 2 | 批准 | - | 规格锁定 |
| Phase 3 | 确认 Tasking | 从规格推导 13 个 AC | Tasking 清单 |
| Phase 4 | 喝茶 | 逐个 AC 执行 TDD | 13 个绿灯测试 |
| Phase 5 | - | 对照规格验证 | 验证报告 |
| Phase 6 | Code Review | 输出汇报 | 完成 |

### 参考项目

`example/` 目录是使用此工作流生成的完整用户管理系统。
`docs/specs/user-management.md` 是对应的规格说明书。

---

## 九、最佳实践

### Do

- **需求可以模糊**：SDD 阶段会帮你补全细节，不需要写得很精确
- **认真审查规格**：这是唯一的质量门禁，在这里花时间比在代码里花时间值
- **分功能出规格**：每个独立功能一份规格，不要把所有功能塞进一个规格
- **规格存档**：批准的规格保存在 `docs/specs/`，作为文档长期保留

### Don't

- **不要跳过规格**：没有规格就没有 Tasking 的依据
- **不要在 TDD 阶段改需求**：改需求应该回到规格阶段
- **不要一次做太多**：单次建议不超过 5 个 Task / 15 个 AC
- **不要忽略规格验证**：Phase 5 是确保没有遗漏的最后一道防线

---

## 十、踩坑记录

### 10.1 没有规格就开始 Tasking

**后果**：AC 的字段、格式、校验规则全靠猜测，测试写了也白写。
**正确做法**：Phase 1 起草规格 → Phase 2 用户审查批准 → 再 Tasking。

### 10.2 测试数据超过字段长度

**后果**：`username varchar(10)` 但测试用了 11 字符的用户名，报 Data Truncation。
**正确做法**：测试数据必须符合规格中定义的字段约束。

### 10.3 用 text block JSON 构造请求体

**后果**：容易出现格式错误，不可重构。
**正确做法**：使用 `Map.of("key", "value")`。

### 10.4 Testcontainers 版本兼容性

**后果**：1.x 的 artifact 名称与 2.x 不同，直接改版本号会报错。
**正确做法**：升级大版本时查阅迁移指南。
