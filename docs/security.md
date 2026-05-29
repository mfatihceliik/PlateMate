---
type: backend-doc
area: security
tags: [backend, spring-boot]
updated: 2026-05-28
---

# Security

## Purpose

Documents authentication, authorization, JWT behavior, password handling, and security conventions.

## Architecture

| Area | Class | Behavior |
| --- | --- | --- |
| Password encoder | `SecurityConfig` | `BCryptPasswordEncoder` bean |
| Security chain | `SecurityConfig` | Stateless, CSRF/form/basic disabled, all requests permitted |
| API auth | `JwtAuthenticationInterceptor` | Bearer token validation for `/api/**` with exclusions |
| Registration | `WebMvcConfig` | Interceptor path includes/excludes |
| Token logic | `JwtTokenProvider` | Access/refresh token generation and validation |
| Refresh tokens | `RefreshTokenManager` | Issue, refresh, revoke |


## Auth Flow

1. Auth endpoints issue access + refresh tokens
2. Client sends `Authorization: Bearer <token>`
3. `JwtAuthenticationInterceptor` validates token via `JwtTokenProvider`
4. Invalid/missing → 401 with JSON `{"success":false,"message":"..."}`
5. Valid → sets request attributes `userId`, `username`
6. Controllers use `@RequestAttribute("userId")`

## Protected vs Public Paths

**Interceptor exclusions (public):** `/api/auth/**`, `/api/cities/**`, `/ws/**`
**Swagger (permit-all):** `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`
**All other `/api/**`:** requires valid bearer token.

## Authorization Rules

| Rule | Where | Behavior |
| --- | --- | --- |
| Admin-only | Admin controllers | `IAdminAccessService.checkAdmin` → 403 |
| Self-only settings/update | `UserSettingsController`, `UserController.update` | Path id ≠ token id → 403 |
| Self or admin delete | `UserController.delete` | Self may delete; otherwise admin required |
| Review owner | `PlateManager` | Service checks owner before mutation |
| Friendship participant | `FriendshipManager` | Addressee accepts/rejects; participants remove |
| Social link owner | `SocialMediaLinkManager` | Must own profile link |
| Chat participant | `ChatMessageManager` | Token user must be room participant |
| FCM token owner | `FcmTokenManager` | Token unregister requires ownership match |

## Token & Refresh

* `AuthManager` coordinates all auth flows, attaching tokens to `UserDto`.
* Refresh failure → `RefreshTokenServiceException` → 401 with code (`REFRESH_EXPIRED`/`REVOKED`/`INVALID`).
* Logout revokes refresh token via `IRefreshTokenService`.
* Config: `jwt.secret`, `jwt.access-expiration-ms`, `jwt.refresh-expiration-ms`.

## Password Handling

* Registration uses `passwordEncoder.encode`.
* Login uses `passwordEncoder.matches`.
* `UserDto` excludes password. `UpdateUserRequest` allows password update (`@Size(min=6)`).

## Open Questions

* No explicit CORS configuration visible.
