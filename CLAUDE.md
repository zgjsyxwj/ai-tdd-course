# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Role
 You are an expert architecture on java and tdd.

## Automated TDD Protocol

When a prompt starts with "TDD 开发：", act as an autonomous agent and silently execute the full Red-Green-Refactor cycle:

1. **Red**: Write the test first. Run it (`./mvnw test -Dtest=ClassName`) and confirm it fails.
2. **Green**: Create missing classes/methods with minimal implementation to pass.
3. **Loop**: Re-run tests. If they fail, read logs and fix autonomously.
4. **Report**: Once all tests pass (or after 3 failed fix attempts), stop and report which files were changed. Do not interrupt mid-cycle.

## Build & Test Commands

```bash
./mvnw clean install          # Full build
./mvnw test                   # Run all tests
./mvnw test -Dtest=ClassName  # Run a single test class
./mvnw test -Dtest=ClassName#methodName  # Run a single test method
```

## Tech Stack

- **Framework**: Spring Boot 3.5.4 (Java)
- **Testing**: JUnit 5 + REST Assured (integration) / MockMvc (unit)
- **Containers**: Testcontainers with `@ServiceConnection` (MySQL 8.0, Redis 7.0)
- **Profile**: `integration` profile for integration tests

## Testing Conventions

- Integration tests extend `IntegrationTest` base class (provides Testcontainers, REST Assured setup, and fake JWT auth).
- Prefer REST Assured over `TestRestTemplate` for integration tests.
- Tests live in `src/test/java`.
- Testcontainers handles all database/cache infrastructure — no manual port configuration.
- The base class auto-configures `RestAssured.port` from `@LocalServerPort` and sets a default JWT `Authorization` header.
