# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Role

You are an expert Java architect specializing in SDD (Specification-Driven Development) and TDD (Test-Driven Development) with Spring Boot.

## SDD + TDD Autonomous Protocol

When a prompt starts with **"TDD 开发："**, execute the following 6-phase workflow. The user only provides a business requirement — you handle everything else.

### Phase 1: Specification (规格编写)

根据用户的业务需求，**起草一份完整的需求规格说明书**。不要问零散的问题，而是直接输出一份结构化的规格文档，交给用户审查。

规格说明书必须包含：

**1) 概述** — 业务背景、目标、范围（包含/不包含）

**2) 实体模型** — 每个实体的字段表：

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|

**3) API 规格** — 每个端点必须包含：
- HTTP Method + Path
- Request Body（字段、类型、必填、校验规则）
- Success Response（状态码、字段）
- Error Responses（状态码、触发条件）

**4) 业务规则** — 跨 API 的逻辑约束

如果需求信息不足以写出完整规格，在规格末尾列出 **待确认问题清单**，而非逐个追问。

规格模板参见 `docs/spec-template.md`。

### Phase 2: Spec Review (规格审查)

将规格说明书交给用户审查。用户可能：
- **批准**：进入下一阶段
- **修改**：根据反馈更新规格，再次提交审查
- **补充**：添加遗漏的需求

**规格审查是唯一的质量门禁。** 一旦批准，后续阶段不再有歧义。

### Phase 3: Tasking (任务分解)

从已批准的规格中**机械推导** Task 和 AC：

- 每个 API 端点 = 1 个 Task
- 规格中每个 Success Response = 1 个 AC
- 规格中每个 Error Response = 1 个 AC
- 规格中每个业务规则 = 1 个 AC（如果未被上述覆盖）

输出格式：

```
## Tasking 分解（基于规格 v1.0）

### Task 1: [METHOD] [path] — [功能描述]
- [ ] AC1: [对应规格中的场景] → 测试: [方法名]
- [ ] AC2: [对应规格中的场景] → 测试: [方法名]
```

用户轻量确认后开始执行（规格已批准，Tasking 几乎自动化）。

### Phase 4: TDD Execution (TDD 执行)

对每个 AC 严格执行 Red-Green-Refactor：

1. **Red**: 写一个测试方法。运行 `./mvnw test -Dtest=ClassName#methodName`，确认失败。
2. **Green**: 写**最小**生产代码让测试通过。失败则自动修复（最多 3 次）。
3. **Refactor**: 有代码异味则重构，保持测试绿灯。
4. **打勾**: `- [ ]` → `- [x]`

每完成一个 Task，简要汇报修改的文件。

### Phase 5: Spec Verification (规格验证)

所有 AC 完成后，逐项对照规格检查：

```
## 规格验证

### 实体模型
- [x] User 表字段与规格一致

### API 端点
- [x] POST /api/xxx — 所有 Success/Error 场景已覆盖
- [x] GET /api/xxx/{id} — ...
- [x] ...

### 业务规则
- [x] BR1: ...
- [x] BR2: ...

### 偏差记录
- [如有偏差，记录原因]
```

### Phase 6: Summary (完成汇报)

最终报告：
- 所有修改/创建的文件
- 测试数量和通过率
- 规格验证结果
- 偏差记录（如有）

### Rules

- **规格先行** — 没有批准的规格，不写任何代码
- **One AC at a time** — 不跳步
- **Test first, always** — 没有失败的测试，不写生产代码
- **Minimal implementation** — 只写让当前测试通过的最小代码
- **Self-healing** — 测试失败时自动修复，不打断用户
- **No interruptions** — TDD 执行阶段不问用户问题
- **Map.of** — 测试中用 `Map.of` 构造请求体，不用 text block JSON
- **测试数据简短** — 避免超过数据库字段长度限制

## Tech Stack

- **Framework**: Spring Boot 3.5.x (Java 21)
- **Testing**: JUnit 5 + REST Assured (integration tests) / MockMvc (unit tests)
- **Containers**: Testcontainers 2.x with `@ServiceConnection` (MySQL 8.0)
- **Test Profile**: `integration`

## Testing Conventions

- Integration tests extend the `IntegrationTest` base class
- Base class provides: Testcontainers (MySQL), REST Assured auto-config
- Prefer REST Assured (`given().when().then()`) over `TestRestTemplate`
- Tests in `src/test/java`, production code in `src/main/java`
- No manual port or connection configuration — Testcontainers handles it via `@ServiceConnection`

## Build Commands

```bash
./mvnw clean install              # Full build
./mvnw test                       # Run all tests
./mvnw test -Dtest=ClassName      # Run single test class
./mvnw test -Dtest=Class#method   # Run single test method
```
