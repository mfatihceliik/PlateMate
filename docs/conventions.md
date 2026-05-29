---
type: backend-doc
area: conventions
tags: [backend, spring-boot]
updated: 2026-05-29
---

# Conventions

## Package Layout

| Concern | Path |
| --- | --- |
| Controller interfaces | `api/controllers/abstracts/I*Controller.java` |
| Controller implementations | `api/controllers/concrete/*Controller.java` |
| Service interfaces | `business/abstracts/I*Service.java` |
| Service implementations | `business/concrete/*Manager.java` |
| Repositories | `dataAccess/abstracts/I*Dao.java` |
| Entities / lookups | `entities/concrete/*.java` |
| Response DTOs | `entities/dto/*Dto.java` |
| Request DTOs | `entities/dto/request/*Request.java` |
| Mappers | `core/utilities/mappers/*Mapper.java` |
| Result wrappers | `core/utilities/results` |
| Pagination | `core/utilities/pagination` |
| Messages | `core/utilities/messages` |
| Shared constants | `business/utilities/constants` |

## Naming

* Interfaces: `I` prefix (`IUserService`, `IPlateDao`, `IPlateController`)
* Implementations: `Manager` suffix (business), `Dao` suffix (repos)
* DTOs: `Dto` suffix, admin variants: `AdminDto`
* Requests: `Request` suffix
* Lookups: `Lookup` suffix for JPA entities, enum-like helpers keep domain names (`PlateReviewStatus`, `SocialPlatform`)

## Controller Patterns

* Thin controllers, delegate to service interfaces via final fields.
* Routes on abstract interfaces, logic in concrete classes.
* Auth user from `@RequestAttribute("userId")`.
* Admin: `IAdminAccessService.checkAdmin` → 403. Self-only: path id vs token id → 403.
* Service failures → `badRequest`. Creates → 201.
* **Always** use `@Valid` with `@RequestBody`.
* Controllers must not import `entities.concrete` or `dataAccess`.

## Service/Manager Patterns

* Controllers depend on `I*Service`, never on `*Manager`.
* Managers return `Result` / `DataResult<T>`.
* Managers use repos, mappers, `IMessageService`, and `BusinessRules.run(...)`.
* `@Transactional` on public write methods. Never on private helpers.
* Service interfaces: method signatures only, no nested classes, no default methods, no entity parameters.
* Small local rules → well-named private helpers. Shared/complex rules → extract to validator/mapper/policy/domain service.

## Message Key Convention

All user-facing messages must use `Messages.*` constants with `IMessageService`:

```java
// Correct
return new ErrorResult(messageService.getMessage(Messages.USER_NOT_FOUND));

// Wrong — string literal key
return new ErrorResult(messageService.getMessage("user.not.found"));
```

New message keys: add constant to `Messages.java`, add value to `messages.properties`.

## Repository Patterns

* Extend `JpaRepository`. Derived queries for simple access, `@Query` JPQL for complex.
* Projection interfaces for aggregates/discovery.
* Lookup-backed fields queried by id.
* Pagination: Spring `Pageable` → `PaginationMapper.fromPage(...)`.

## DTO / Entity Patterns

* Request DTOs with Jakarta validation (`@NotBlank`, `@Min`, `@Max`, `@Size`, `@Email`, `@Pattern`).
* Response DTOs hide sensitive fields, expose `id + code` for lookup-backed domains.
* Entities map JPA relationships; API uses DTOs.
* Service interfaces should not accept or return JPA entities — use DTOs or primitives.

## Error / Response

* Result wrappers are the primary response style.
* Messages resolved by key via `IMessageService` with `Messages.*` constants.
* Auth refresh errors include data map with `code` (`REFRESH_EXPIRED`, `REFRESH_REVOKED`, `REFRESH_INVALID`).
* JWT interceptor writes JSON error body for missing/invalid tokens.
* `GlobalExceptionHandler`: `MethodArgumentNotValidException` → 400, `InvalidPaginationException` → 400, type mismatch → 400, unhandled → 500. Logs raw exception details server-side.
* **Never** expose raw `exception.getMessage()` to clients. Use generic localized messages instead.

## New Endpoint Checklist

When creating a new endpoint, you MUST follow this sequence and verify all architectural rules:

1. **Controller Interface:** Create `api/controllers/abstracts/I{Feature}Controller.java`
2. **Concrete Controller:** Create `api/controllers/concrete/{Feature}Controller.java`
3. **Service Interface:** Create `business/abstracts/I{Feature}Service.java`
4. **Manager:** Create `business/concrete/{Feature}Manager.java`
5. **Request DTO:** Create `entities/dto/request/{Action}{Feature}Request.java`
6. **Response DTO:** Create `entities/dto/{Feature}Dto.java`
7. **Mapper:** Add a new mapper or use an existing one following project conventions in `core/utilities/mappers`
8. **Validation:** Add Jakarta validation annotations to the Request DTO
9. **Message Keys:** Define message keys via `Messages.java` constants and add to `messages.properties`
10. **Controller Messages:** Do NOT use raw string message keys in controllers
11. **Service Interface Integrity:** Do NOT expose JPA entities in service interfaces
12. **Controller Isolation:** Do NOT inject or call DAO/Repository from a controller
13. **Transaction Boundary:** If the manager method is a write operation, verify the `@Transactional` boundary
14. **API Contracts:** Update `docs/api-contracts.md`
15. **Controller Docs:** Update `docs/controllers.md`
16. **Changelog:** Update `docs/changelog.md`
17. **Violations:** If any known violation occurred, update `docs/known-violations.md`

## Build / Run

* Java 21 via Gradle toolchain. `gradlew` / `gradlew.bat`.
* Tests: JUnit via `spring-boot-starter-test`.
* Flyway migrations: `src/main/resources/db/migration`.
* Docker: app 8080/9092, PostGIS PostgreSQL 5433→5432.

## Documentation

* `/docs` is canonical. Obsidian-compatible markdown with `[[internal-links]]`.
* Document from actual source. Unclear → `Open Questions`.
* **English only** in source comments and docs. Turkish comments are strictly prohibited.
