---
type: backend-doc
area: configuration-infrastructure
tags: [backend, spring-boot]
updated: 2026-05-28
---

# Configuration Infrastructure

## Purpose

Documents config classes, property keys, seed runners, scheduling, and Docker environment.

## Config Classes

| Class | Responsibility |
| --- | --- |
| `SecurityConfig` | Security chain + `PasswordEncoder` bean |
| `WebMvcConfig` | JWT interceptor for `/api/**` with exclusions (`/api/auth/**`, `/api/cities/**`, `/ws/**`) |
| `JwtAuthenticationInterceptor` | Bearer token → validates → sets `userId`/`username` attrs |
| `JwtTokenProvider` | Token generation/validation |
| `SocketIOConfig` | `SocketIOServer` bean, handshake token auth via query param |
| `FirebaseConfig` | Firebase Admin SDK from `serviceAccountKey.json` |
| `I18nConfig` | MessageSource (basename `messages`, UTF-8), locale `tr`, validator binding |
| `OpenApiGroupingConfig` | Swagger groups: `admin` → `/api/admin/**`, `client` → rest |
| `PlateReportTypeSeedConfig` | Seeds default report types at startup |
| `UserRoleSeedConfig` | Seeds roles (`NORMAL`/`PREMIUM`/`ADMIN`), syncs users |

## Bootstrap

`PlatemateApplication`: `@SpringBootApplication` + `@EnableScheduling`. `SocketServerRunner` starts/stops Socket.io server.

## Key Properties

**Data/JPA/Flyway:** `spring.datasource.url/username/password`, `spring.jpa.hibernate.ddl-auto` (default `validate`), `spring.flyway.enabled/baseline-on-migrate/baseline-version/locations`

**JWT:** `jwt.secret`, `jwt.access-expiration-ms`, `jwt.refresh-expiration-ms`

**Socket:** `socket.host`, `socket.port`

**Moderation:** `moderation.accepted-responsibility-legacy-fallback`, `moderation.comment-report-threshold`, `moderation.hide-plate-on-removal-request`, `platemate.moderation-retention-days`, `platemate.compliance-retention.cron`

**Other:** `application-db-reset.properties` sets `ddl-auto=create`

## Docker Environment

`SPRING_DATASOURCE_URL/USERNAME/PASSWORD`, `SPRING_JPA_HIBERNATE_DDL_AUTO`, `JWT_SECRET`, `POSTGRES_DB/USER/PASSWORD`

Ports: app 8080, socket 9092, postgres 5433→5432. Image: `postgis/postgis:15-3.3`.
