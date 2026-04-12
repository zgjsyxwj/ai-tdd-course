# Claude Code x Spring Boot 自主 TDD 工作流 SOP

## 流程总览

```
用户输入业务需求
       │
       ▼
┌─────────────────┐
│  Phase 1: Tasking │  Claude 自动分解任务，输出 AC 清单
│  (任务分解)       │  用户确认后进入下一阶段
└────────┬────────┘
         │ 用户确认
         ▼
┌─────────────────┐
│  Phase 2: TDD    │  逐个 AC 执行 Red → Green → Refactor
│  (循环执行)       │  每完成一个 AC 打勾，无需用户干预
└────────┬────────┘
         │ 全部 AC 完成
         ▼
┌─────────────────┐
│  Phase 3: Summary│  汇报修改文件、测试覆盖、决策记录
│  (完成汇报)       │
└─────────────────┘
```

**核心理念**：用户只负责「说什么」，Claude 负责「怎么做」。

---

## 阶段一：环境准备（一次性配置）

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

### 1.2 配置 CLAUDE.md

在项目根目录放置 `CLAUDE.md` 文件，包含自主 TDD 协议。详见项目根目录的 `CLAUDE.md`。

这是控制 Claude Code 行为的核心文件，定义了：
- 角色（Java + TDD 架构专家）
- 自主 TDD 三阶段协议（Tasking → TDD Execution → Summary）
- 技术栈约定
- 测试规范

### 1.3 测试基类 IntegrationTest.java

所有集成测试继承此基类，自动获得：
- Testcontainers 管理的 MySQL + Redis 实例
- REST Assured 自动配置（端口、Content-Type、JWT 认证）
- `@ActiveProfiles("integration")` 激活测试配置

```java
@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class IntegrationTest {

    @LocalServerPort
    private int port;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Container
    @ServiceConnection
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379);

    @BeforeEach
    public void setUpRestAssured() {
        RestAssured.port = port;
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
    }
}
```

### 1.4 终端配置（推荐）

使用分屏终端（Ghostty / iTerm2）：
- **左屏**：运行 `claude` — AI 驾驶舱
- **右屏**：Git 操作、Docker 监控、手动命令

### 1.5 Zsh 快捷命令（可选）

在 `~/.zshrc` 中添加：

```bash
# 一键启动 TDD 自动驾驶
tdd() {
  if [ -z "$1" ]; then
    echo "用法: tdd '你的需求'"
    return 1
  fi
  claude -p "TDD 开发：$*"
}

# 实体 CRUD 快速生成
tdd-crud() {
  if [ -z "$1" ]; then
    echo "用法: tdd-crud '实体名称'"
    return 1
  fi
  claude -p "TDD 开发：为 $1 生成完整的 CRUD（Controller/Service/Repository）及集成测试。"
}
```

---

## 阶段二：自主 TDD 协议详解

### 2.1 触发方式

在 Claude Code 中输入以 **"TDD 开发："** 开头的需求：

```
TDD 开发：实现用户管理系统，支持注册、查询、列表、更新、删除。
用户名和邮箱必填，用户名不可重复。
```

### 2.2 Phase 1: Tasking（Claude 自动执行）

Claude 分析需求后，输出结构化的任务清单：

```markdown
## Tasking 分解

### Task 1: 用户注册 POST /api/users
- [ ] AC1: 有效数据注册成功，返回 201 + 用户信息 → 测试: should_register_user_successfully
- [ ] AC2: 用户名为空时返回 400 → 测试: should_return_400_when_username_is_blank
- [ ] AC3: 邮箱为空时返回 400 → 测试: should_return_400_when_email_is_blank
- [ ] AC4: 用户名已存在时返回 409 → 测试: should_return_409_when_username_exists

### Task 2: 用户查询 GET /api/users/{id}
- [ ] AC1: 查询存在的用户返回 200 + 用户信息 → 测试: should_get_user_by_id
- [ ] AC2: 查询不存在的用户返回 404 → 测试: should_return_404_when_user_not_found

### Task 3: 用户列表 GET /api/users
- [ ] AC1: 返回所有用户列表 200 → 测试: should_list_all_users

### Task 4: 用户更新 PUT /api/users/{id}
- [ ] AC1: 有效数据更新成功返回 200 → 测试: should_update_user_successfully
- [ ] AC2: 更新不存在的用户返回 404 → 测试: should_return_404_when_updating_nonexistent_user

### Task 5: 用户删除 DELETE /api/users/{id}
- [ ] AC1: 删除成功返回 204 → 测试: should_delete_user_successfully
- [ ] AC2: 删除不存在的用户返回 404 → 测试: should_return_404_when_deleting_nonexistent_user

请确认是否开始执行，或调整 Task/AC。
```

**用户操作**：检查清单，回复「确认」或提出修改。

### 2.3 Phase 2: TDD Execution（Claude 自动执行）

用户确认后，Claude 对每个 AC 严格执行 Red-Green-Refactor：

#### 示例：AC1 — 用户注册成功

**Step 1 - Red（写测试，确认失败）**

Claude 编写测试：
```java
@Test
void should_register_user_successfully() {
    given()
        .body("""
            {"username": "john", "email": "john@test.com"}
            """)
    .when()
        .post("/api/users")
    .then()
        .statusCode(201)
        .body("username", equalTo("john"))
        .body("email", equalTo("john@test.com"))
        .body("id", notNullValue());
}
```

Claude 运行 `./mvnw test -Dtest=UserControllerTest#should_register_user_successfully`，确认编译失败或测试失败（Red）。

**Step 2 - Green（最小实现，测试通过）**

Claude 创建/修改必要的生产代码：
- `User.java` (Entity)
- `UserRepository.java` (JPA Repository)
- `UserService.java` (Service)
- `UserController.java` (Controller)
- `UserRequest.java` / `UserResponse.java` (DTO)

只写让这一个测试通过的最小代码。

Claude 再次运行测试，确认通过（Green）。

**Step 3 - Refactor（可选）**

如果有明显的代码异味，Claude 进行重构并确保测试仍然通过。

**Step 4 - 打勾**

```markdown
- [x] AC1: 有效数据注册成功，返回 201 + 用户信息 ✅
```

然后继续 AC2...

### 2.4 Phase 3: Summary（Claude 自动输出）

全部完成后，Claude 汇报：

```
## 完成汇报

### 修改/创建的文件
- src/main/java/.../User.java (新建)
- src/main/java/.../UserController.java (新建)
- src/main/java/.../UserService.java (新建)
- src/main/java/.../UserRepository.java (新建)
- src/main/java/.../dto/UserRequest.java (新建)
- src/main/java/.../dto/UserResponse.java (新建)
- src/test/java/.../UserControllerTest.java (新建)

### 测试结果
- 总计 11 个测试，全部通过 ✅
- 覆盖 5 个 API 端点，11 个验收条件

### 关键决策
- 使用 JPA @Entity 映射 users 表
- 用户名唯一性通过数据库 UNIQUE 约束 + Service 层检查双重保障
- 删除采用硬删除
```

---

## 阶段三：实战演示

### 完整 Prompt

```
TDD 开发：实现用户管理系统，支持以下功能：
1. 用户注册 POST /api/users — 用户名和邮箱必填，用户名不可重复
2. 用户查询 GET /api/users/{id}
3. 用户列表 GET /api/users
4. 用户更新 PUT /api/users/{id}
5. 用户删除 DELETE /api/users/{id}
```

### 预期行为

1. Claude 输出 Tasking 分解（约 11 个 AC）
2. 用户确认
3. Claude 自动创建项目结构（如果不存在）
4. Claude 逐个 AC 执行 TDD，每完成一个打勾
5. 全部完成后输出汇报

### 示例项目

参见 `example/` 目录，这是使用此 TDD 工作流生成的完整用户管理系统。

---

## 阶段四：最佳实践

### Do

- **需求写清楚**：包含接口路径、必填字段、校验规则、异常情况
- **先看 Tasking 再确认**：Claude 的分解可能遗漏边界条件，自己补充
- **分批执行**：大需求可以分多次 TDD，不要一次扔太多
- **Code Review**：TDD 完成后在 IDE 中审查代码质量

### Don't

- **不要中途打断**：TDD 执行阶段不要频繁干预，让 Claude 自动完成
- **不要跳过确认**：Tasking 阶段的确认是质量门禁
- **不要忽略失败**：如果 Claude 报告 3 次修复失败，手动介入排查
- **不要一次做太多**：单次 TDD 建议不超过 5 个 Task / 15 个 AC

---

## 阶段五：踩坑记录与经验教训

### 5.1 需求澄清必须在 Tasking 之前

不要让 Claude 猜测实体字段、请求格式、校验规则。Phase 0（需求澄清）是协议的关键环节。没有明确的输入输出定义，测试用例就是无根之木。

### 5.2 测试数据要符合字段约束

如果 `username varchar(10)`，测试中就不能用 `shortupdate`（11字符）这样的用户名。MySQL 会报 Data Truncation 错误。**测试用户名保持简短**。

### 5.3 用 Map.of 代替 text block JSON

```java
// 推荐
given().body(Map.of("username", "john", "pwd", "123456"))

// 不推荐
given().body("""
    {"username": "john", "pwd": "123456"}
    """)
```

`Map.of` 更简洁、类型安全、IDE 可重构。

### 5.4 Testcontainers 2.x 迁移

升级 Testcontainers 大版本时注意 artifact 名称变了：
- `junit-jupiter` → `testcontainers-junit-jupiter`
- `mysql` → `testcontainers-mysql`

且需要显式指定版本号，Spring Boot parent 不再管理 2.x 版本。不要只改版本号，要查阅迁移指南。

---

## 附录：完整 Zsh 配置

```bash
# ===== Claude Code TDD 工作流 =====

# 自主 TDD 驾驶
tdd() {
  if [ -z "$1" ]; then
    echo "用法: tdd '你的需求'"
    return 1
  fi
  claude -p "TDD 开发：$*"
}

# CRUD 快速生成
tdd-crud() {
  if [ -z "$1" ]; then
    echo "用法: tdd-crud '实体名称'"
    return 1
  fi
  claude -p "TDD 开发：为 $1 生成完整的 CRUD（Controller/Service/Repository）及集成测试。"
}
```
