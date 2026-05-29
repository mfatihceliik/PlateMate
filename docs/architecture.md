---
type: backend-doc
area: architecture
tags: [backend, spring-boot]
updated: 2026-05-29
---

# Architecture

## Purpose

Documents backend layout, layers, request flow, dependency graph, and architectural conventions.

## Tech Stack

Java 21, Spring Boot 4.0.6, Spring MVC, Spring Data JPA, Spring Security, Flyway, PostgreSQL/PostGIS, Socket.io (netty-socketio), Firebase Admin SDK, Lombok, Springdoc OpenAPI. Build: Gradle.

## Layer Map

| Layer | Package | Responsibility |
| --- | --- | --- |
| Controllers | `api/controllers` | HTTP endpoints, request attributes, status mapping, validation |
| Socket API | `api/socket` | Socket.io server, socket auth, chat handlers, events. Dependency inverted: `NotificationManager` depends on `ISocketPushService`, which is implemented by `SocketPushManager` in API. |
| Business services | `business` | Use cases, validation, domain rules (interfaces + managers) |
| Data access | `dataAccess` | Spring Data repositories, projection interfaces |
| Entities/DTOs | `entities` | JPA entities, lookups, request/response DTOs, constants |
| Core utilities | `core` | Result wrappers, mappers, pagination, messages, exceptions |
| Configuration | `config` | Security, JWT, MVC, Firebase, socket, seed, scheduling |
| Migrations | `resources/db/migration` | Flyway V1–V12 |

Base package: `com.mefy.platemate`

## Package Scale

| Package | File Count | Notes |
| --- | --- | --- |
| `api/controllers/concrete` | 19 | 18 business endpoints + 1 `SwaggerRedirectController`. Interface pattern fully implemented (V15 resolved). |
| `api/controllers/abstracts` | 18 | Interfaces for all business controllers. |
| `api/socket` | 6 | Includes `SocketModule`, abstracts, and concrete socket handlers. |
| `business/concrete` | 23 | Manager implementations. `PlateManager` is the heaviest with exactly 15 dependencies. |
| `business/abstracts` | 24 | Service interfaces. |
| `business/utilities` | 11 | Validators, moderation, rules, time, constants. |
| `dataAccess/abstracts` | 21 | Spring Data JPA Repositories. |
| `entities/concrete` | 46 | JPA entities and lookup enum constants. |
| `entities/dto` | 55 | 33 Response DTOs, 22 Request DTOs. |
| `core/utilities` | 23 | Mappers (11), Results (6), Pagination (4), Messages (2). |
| `config` (security/jwt) | 10 | `SecurityConfig`, JWT (2), MVC, Firebase, Socket, Seeds. |

## Request Flow

1. HTTP → Spring MVC controller
2. `JwtAuthenticationInterceptor` validates bearer token for `/api/**` (except `/api/auth/**`, `/api/cities/**`, `/ws/**`)
3. Controller reads `@RequestAttribute("userId")`
4. Controller delegates to service interface
5. Manager validates, persists, maps, returns `Result`/`DataResult<T>`
6. Controller maps result to `ResponseEntity` (200/201/400/401/403)

## Dependency Direction

`Controller → Service Interface → Manager → Repository/DataAccess`

Controllers inject interfaces, not managers. Managers may call other service interfaces for cross-feature workflows.

## Cross-Manager Dependencies

For the complete cross-manager dependency graph and specific notes on inter-manager method calls, refer to the **Cross-Manager Call Graph** section in **[[services-business]]**.

## Security Architecture

`SecurityConfig` permits all requests at filter-chain level. Actual auth enforced by `JwtAuthenticationInterceptor` via `WebMvcConfig`. Details in [[security]].

## Important Classes

| Class | Responsibility |
| --- | --- |
| `PlatemateApplication` | Entry point, `@EnableScheduling` |
| `SecurityConfig` | Password encoder, stateless security chain |
| `WebMvcConfig` | JWT interceptor registration |
| `JwtAuthenticationInterceptor` | Bearer token validation, request attribute setup |
| `JwtTokenProvider` | Token generation and validation |
| `SocketServerRunner` | Socket.io lifecycle |
| `FirebaseConfig` | Firebase Admin SDK init |

## Key Conventions

* Controllers thin, delegate to services.
* API responses use result wrappers, not raw DTOs.
* Admin: `IAdminAccessService.checkAdmin(...)`. Self-only: path id vs token id.
* Lookup-backed domains use id + code helpers.
* DTO mapping centralized in `core/utilities/mappers`.
* Messages via `IMessageService` with `Messages.*` constants.

## Open Questions

* `SecurityConfig` permits all while interceptor enforces JWT — intentional current behavior.
* `/api/cities/**` excluded in MVC config but no city controller exists. `CityManager` + `ICityService` exist; controller may need to be added.
* No explicit CORS configuration visible.
