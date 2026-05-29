---
type: backend-doc
area: agent-workflow
updated: 2026-05-30
---

# Agent Workflow

## Purpose

Documents the detailed workflow, context budget policies, source inspection strategies, and completion checklists for AI agents working on this project.

## Core Principles

* Follow SOLID principles with practical expectations.
* Keep changes small and focused; do not refactor unrelated code.
* Do not change existing business behavior unless the task explicitly requires it.
* Do not introduce new abstractions unless they clearly reduce duplication or isolate a real concern.
* Do not duplicate logic if an existing service, mapper, validator, policy, or utility already handles it.
* Respect the existing layered architecture.
* Follow existing naming, response, validation, and error patterns.

## Implementation Rules

* Prefer private helper methods inside the manager for local logic.
* Extract a separate validator/policy/domain service only when logic is reused, complex, or separately testable.
* Keep write methods transactional at the public service method level.
* Do not add `@Transactional` to private helper methods for proxy-based transaction behavior.
* Return `Result` / `DataResult<T>` according to existing conventions.
* Use `IMessageService` message keys instead of hardcoded user-facing text.
* Use request DTO validation annotations for client input.
* Avoid exposing raw exception messages to clients.
* Avoid broad formatting-only changes.
* Avoid Turkish comments in source/docs; use clear English comments only when comments add value.

## Context Budget Policy

Use the smallest context that can safely solve the task.

* Do not scan the full repository for focused tasks.
* Do not open unrelated managers, controllers, entities, or repositories.
* Do not read every file in `/docs`; use `docs/index.md` to select docs.
* Prefer search before opening large files.
* Open the closest existing implementation of the same pattern before creating new code.
* For large files, search for the target method/class first, then read the surrounding area.
* Read only the **Recent Changes Summary** in `docs/changelog.md` for normal tasks.
* Read `docs/changelog-archive.md` ONLY for major refactors that require deep historical context.

## Source Inspection Strategy

Before editing source code:

1. Identify the task type.
2. Search for the relevant route, DTO, service method, entity, or repository method.
3. Inspect only the smallest necessary file set.
4. Compare with the nearest existing pattern.
5. Make the smallest correct change.

### Common File Sets

| Task | Inspect first |
| --- | --- |
| New/changed endpoint | controller interface, concrete controller, request DTO, response DTO, service interface, manager, related tests |
| Business rule change | service interface, manager, related DAO, entity/DTO, mapper, manager tests |
| Repository/query change | DAO, projection, entity, affected manager, database/data-access docs |
| DTO/contract change | request/response DTO, mapper, controller, `api-contracts`, client contract docs |
| Auth/security change | `SecurityConfig`, `WebMvcConfig`, `JwtAuthenticationInterceptor`, `JwtTokenProvider`, auth manager/tests |
| Socket/chat change | socket handler/module, chat service/manager, participant service, socket docs/tests |
| FCM/notification change | notification manager, FCM token/service, socket event constants, notification docs/tests |
| Refactor/cleanup | target class, direct interface, direct dependencies, related tests only |

## Documentation Rules

* `/docs` is the canonical documentation folder.
* Use Obsidian-style internal links: `[[architecture]]`, `[[controllers]]`, etc.
* Document actual source behavior, not intended behavior.
* If behavior is unclear, put it in an `Open Questions` section.
* Do not copy large code blocks into docs.
* Update only affected docs.
* Update `docs/changelog.md` only when behavior, API, database, security, or client contract behavior changes.
* Refactor, architecture veya bug fix tasklarında `docs/known-violations.md` kontrol edilmeli.
* Yeni violation eklenirse bu dosya güncellenmeli.
* Çözülen violation status olarak güncellenmeli.

## Completion Checklist

Before finishing, verify:

* The change is limited to the requested behavior.
* Controllers remain thin.
* Business logic remains in managers/services.
* Persistence logic remains in repositories.
* Core does not depend on business concrete classes or API packages.
* Existing mappers, validators, services, and utilities are reused where appropriate.
* API response wrappers follow the existing convention.
* Request DTOs use validation annotations where needed.
* Sensitive/internal exception details are not exposed to clients.
* Affected tests are added or updated when behavior changes.
* Affected docs are updated.
* Changelog is updated only when required.

## Final Response Format

When code is changed, summarize:

* Changed files
* Why the change was made
* Architecture impact
* Behavior/API impact
* Tests run or tests to run
