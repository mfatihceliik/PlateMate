# Project Instructions for Codex

## Bootstrap (ALWAYS DO THIS FIRST)

1. Read this file completely.
2. Open `docs/index.md` and classify the task using the task matrix.
3. Read only the task-specific docs listed for that task type.
4. Inspect only the source files directly related to the task.
5. If docs and source conflict, treat source code as truth and update the affected docs.

## Project Scope

Java 21 Spring Boot backend. Codex helps with backend development while preserving existing architecture, business rules, package structure, naming conventions, and coding style.

# Project Instructions for Codex

## Bootstrap (ALWAYS DO THIS FIRST)

1. Read this file completely.
2. Open `docs/index.md` and classify the task using the task matrix.
3. Read `docs/agent-workflow.md` for context budget, source inspection strategy, and completion checklist.
4. Read only the task-specific docs listed for that task type.
5. If docs and source conflict, treat source code as truth and update the affected docs.

## Project Scope

Java 21 Spring Boot backend. Codex helps with backend development while preserving existing architecture, business rules, package structure, naming conventions, and coding style.

## Critical Non-Negotiable Rules

* **Dependency Direction:** `Controller -> Service Interface -> Manager Implementation -> Repository/DataAccess`. Controllers must not inject DAOs.
* **Service Interfaces:** Must contain method signatures only (no nested classes, no implementation).
* **Controllers:** Must remain thin (routing, validation annotations, mapping).
* **Context Budget:** Use the smallest context that can safely solve the task. Do not scan the full repository.
* **Completion:** Verify the change is limited to the requested behavior, affected tests are run, and docs/changelog are updated before finalizing.

## Layered Architecture

* **Controllers:** `api/controllers`
* **Services:** `business`
* **Data Access:** `dataAccess`
* **Entities/DTOs:** `entities`
* **Core Utilities:** `core`

*See `docs/architecture.md` and `docs/conventions.md` for full architectural rules.*
