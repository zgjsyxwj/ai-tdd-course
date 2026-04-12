# Lessons Learned

## 1. TDD 流程：必须先澄清需求再 Tasking

**问题**：直接根据模糊需求拆解 AC 并开始写测试，字段、格式、校验规则全靠猜。
**教训**：在 Tasking 之前新增 Phase 0（需求澄清），主动向用户确认实体字段、Request/Response 格式、校验规则等。没有明确的输入输出定义，TDD 的测试用例就是无根之木。
**规则**：永远不要假设业务细节，哪怕看起来很"显然"。

## 2. TDD 流程：Tasking 需要用户确认才能执行

**问题**：第一次跳过了 Tasking 确认步骤，直接开始写测试代码。
**教训**：Tasking 确认是质量门禁，用户可能会补充遗漏的边界条件或调整优先级。自主 TDD 的"自主"是指执行阶段自主，不是跳过确认。
**规则**：输出 Tasking 清单后，必须等用户明确回复"确认"或类似指令。

## 3. 测试数据：注意字段长度约束

**问题**：测试中使用了 `shortupdate`（11字符）作为用户名，超过了 `varchar(10)` 的限制，导致 Data Truncation 500 错误。
**教训**：测试数据必须符合实体的约束定义。写测试时要时刻记住字段长度、格式等限制。
**规则**：测试用户名保持简短（如 `user1`, `short2`），避免超过数据库字段长度。

## 4. 测试构造参数：Map.of 优于 text block JSON

**问题**：最初用 text block 拼 JSON 字符串作为请求体。
**教训**：`Map.of("key", "value")` 更简洁、类型安全、不会出现 JSON 语法错误，且 IDE 可以重构。
**规则**：REST Assured 测试中，优先使用 `Map.of` 构造请求体。

## 5. Testcontainers 2.x 迁移注意事项

**问题**：直接把 `<testcontainers.version>` 从 1.21.1 改成 2.0.2，导致 artifact 找不到。
**教训**：Testcontainers 2.x 的 artifact 名称变了：
- `junit-jupiter` → `testcontainers-junit-jupiter`
- `mysql` → `testcontainers-mysql`
且 Spring Boot parent 不再管理 2.x 版本，需要显式指定 `<version>`。
**规则**：升级 Testcontainers 大版本时，查阅迁移指南，不要只改版本号。
