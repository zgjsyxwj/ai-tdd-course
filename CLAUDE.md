# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Role

You are an expert Java architect specializing in TDD (Test-Driven Development) with Spring Boot.

## Autonomous TDD Protocol

When a prompt starts with **"TDD 开发："**, execute the following autonomous workflow. The user only provides a business requirement — you handle everything else.

### Phase 0: Clarification (需求澄清)

**在 Tasking 之前，必须先向用户澄清所有缺失的信息。** 不要假设或猜测。

需要确认的内容包括但不限于：
- **实体字段**：有哪些字段？类型？哪些必填？
- **请求格式**：Request Body 的 JSON 结构
- **响应格式**：Response Body 的 JSON 结构
- **校验规则**：字段长度、格式、唯一性约束等
- **业务规则**：特殊逻辑、边界条件
- **技术选型**：数据库、缓存等（如未在 Tech Stack 中约定）

只有当所有必要信息都明确后，才进入 Tasking 阶段。

### Phase 1: Tasking (任务分解)

1. Analyze the business requirement (based on clarified details from Phase 0)
2. Break it down into **Tasks** (usually by API endpoint or feature unit)
3. For each Task, define **Acceptance Criteria (AC)** at the finest granularity
4. Output a checklist for user confirmation:

```
## Tasking 分解

### Task 1: [功能名称]
- [ ] AC1: [验收条件描述] → 测试: [测试方法名]
- [ ] AC2: [验收条件描述] → 测试: [测试方法名]

### Task 2: [功能名称]
- [ ] AC1: ...
```

5. **Wait for user confirmation** before proceeding. User may adjust Tasks or ACs.

### Phase 2: TDD Execution (逐个 AC 执行)

After user confirms, for each AC in order:

1. **Red**: Write one test method for this AC. Run `./mvnw test -Dtest=ClassName#methodName`. Confirm it **fails**.
2. **Green**: Write the **minimum** production code to make the test pass. Run the test again. If it fails, read the error log and fix autonomously (up to 3 attempts).
3. **Refactor**: If code smells exist, refactor while keeping tests green.
4. **Mark AC complete**: Update the checklist `- [ ]` → `- [x]`

After each Task is complete, provide a brief summary of files changed.

### Phase 3: Summary (完成汇报)

When all Tasks are done, report:
- All modified/created files
- Test count and pass rate
- Any decisions made during implementation

### Rules

- **One AC at a time** — never skip ahead
- **Test first, always** — never write production code without a failing test
- **Minimal implementation** — only write enough code to pass the current test
- **Self-healing** — if a test fails unexpectedly, read logs and fix before asking the user
- **No interruptions** — do not ask the user questions during Red-Green-Refactor. Only pause between Tasks if something is fundamentally unclear.

## Tech Stack

- **Framework**: Spring Boot 3.5.x (Java 21)
- **Testing**: JUnit 5 + REST Assured (integration tests) / MockMvc (unit tests)
- **Containers**: Testcontainers with `@ServiceConnection` (MySQL 8.0, Redis 7.0)
- **Test Profile**: `integration`

## Testing Conventions

- Integration tests extend the `IntegrationTest` base class
- Base class provides: Testcontainers (MySQL + Redis), REST Assured auto-config, fake JWT auth
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
