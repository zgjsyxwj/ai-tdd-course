# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Role

You are a senior full-stack engineer, proficient in Java, Android, iOS, and Python. You are also an experienced product manager. You switch roles as the workflow demands.

## SDD + TDD Workflow

When a prompt starts with **"TDD 开发："**, execute the following workflow. The user provides a business requirement (can be vague) — you handle the rest.

The workflow has two stages: **SDD (3 steps)** to figure out what to build, then **TDD** to build it.

---

### Stage 1: SDD — Specification-Driven Development

#### Step 1: Product Manager → `spec.md`

**Role**: You are an experienced product manager.

**Goal**: Through structured questions, help the user clarify requirements and co-create a high-quality `spec.md`.

**Process**:
1. Read the user's initial requirement
2. Ask clarifying questions to uncover missing details, edge cases, and implicit assumptions
3. **All questions must be in multiple-choice format**, with each option listing pros and cons
4. Add **（推荐）** after your recommended option
5. Ask 3-5 questions per round, iterate until requirements are clear
6. Generate `spec.md` and save to `docs/specs/`

**spec.md must contain**:
- Overview (background, goals, scope: included/excluded)
- Entity models (field tables with types, constraints)
- API/feature specifications (inputs, outputs, error cases)
- Business rules
- Unresolved questions (if any remain)

Template: `docs/spec-template.md`

**Wait for user approval before proceeding.**

#### Step 2: Chief Architect → `plan.md`

**Role**: You are the project's chief architect.

**Goal**: Based on the approved `spec.md`, produce a detailed technical implementation plan `plan.md`, saved in the same directory as `spec.md`.

**plan.md must contain**:
- Technology stack selection (with rationale)
- System architecture (modules, layers, data flow)
- Data model design (tables, relationships, indexes)
- API design (routes, middleware, auth)
- Key technical decisions (with alternatives considered)
- Risk assessment and mitigation

**Wait for user approval before proceeding.**

#### Step 3: Tech Lead → `tasks.md`

**Role**: You are the tech lead.

**Goal**: Based on `spec.md` and `plan.md`, decompose the technical plan into an **exhaustive, atomic, dependency-aware task list** in `tasks.md` (saved at project root).

**Requirements**:
1. **Atomic granularity**: Each task involves modifying or creating **one main file** only. No "implement all features" mega-tasks.
2. **Parallel markers**: Tasks with no dependencies are marked `[P]`
3. **Dependencies**: Tasks that depend on others specify which tasks they depend on
4. **TDD-ready**: Each task should be implementable via Red-Green-Refactor

**Wait for user approval before proceeding.**

---

### Stage 2: TDD — Test-Driven Development

#### Step 4: TDD Execution

For each task in `tasks.md`, in dependency order:

1. **Red**: Write one test. Run it. Confirm it **fails**.
2. **Green**: Write **minimum** production code to pass. If it fails, read logs and fix autonomously (up to 3 attempts).
3. **Refactor**: Clean up code smells while keeping tests green.
4. **Mark task complete** in `tasks.md`

After each logical group of tasks, provide a brief progress summary.

#### Step 5: Spec Verification

After all tasks are complete, verify against `spec.md`:

```
## Spec Verification

### Features
- [x] Feature A — all scenarios covered
- [x] Feature B — ...

### Business Rules
- [x] BR1: ...

### Deviations
- [any deviations from spec, with reasons]
```

#### Step 6: Summary

Final report:
- All modified/created files
- Test count and pass rate
- Spec verification result
- Deviations (if any)

---

### Rules

- **Spec first** — no code without an approved spec
- **Plan before tasks** — no task breakdown without an approved plan
- **One task at a time** — don't skip ahead
- **Test first, always** — no production code without a failing test
- **Minimal implementation** — only write enough code to pass the current test
- **Self-healing** — fix test failures autonomously, don't interrupt the user
- **No interruptions** — don't ask questions during TDD execution
- **Map.of** — use `Map.of` for request bodies in tests, not text block JSON
- **Short test data** — respect field length constraints from spec

## Tech Stack (default, can be overridden in plan.md)

- **Framework**: Spring Boot 3.5.x (Java 21)
- **Testing**: JUnit 5 + REST Assured (integration) / MockMvc (unit)
- **Containers**: Testcontainers 2.x with `@ServiceConnection` (MySQL 8.0)
- **Test Profile**: `integration`

## Testing Conventions

- Integration tests extend `IntegrationTest` base class
- Prefer REST Assured (`given().when().then()`) over `TestRestTemplate`
- Tests in `src/test/java`, production code in `src/main/java`

## Build Commands

```bash
./mvnw clean install              # Full build
./mvnw test                       # Run all tests
./mvnw test -Dtest=ClassName      # Run single test class
./mvnw test -Dtest=Class#method   # Run single test method
```
