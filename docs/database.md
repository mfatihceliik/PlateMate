---
type: backend-doc
area: database
tags: [backend, spring-boot]
updated: 2026-05-28
---

# Database

## Purpose

Documents database config, schema, migrations, indexes, constraints, and Docker setup.

## Config

| Key | Value |
| --- | --- |
| JDBC default | `jdbc:postgresql://localhost:5432/PlateMate` |
| Docker JDBC | `jdbc:postgresql://db:5432/PlateMate` |
| Hibernate default | `validate` (Docker: `update`) |
| Flyway | enabled, baseline on migrate, baseline version 0 |
| Docker image | `postgis/postgis:15-3.3` |
| Ports | app 8080, socket 9092, DB 5433→5432 |

## Migration Timeline

| Migration | Behavior |
| --- | --- |
| V1 | Plate/review safety columns, status strings, audit fields, indexes |
| V2 | User active/deleted, comment reports, removal requests, audit logs |
| V3 | Legacy null backfill, status defaults |
| V4 | Dropped audit logs |
| V5 | Seeded report types, softened plate report data |
| V6 | Moderation event table, profile/review indexes, dropped plate columns |
| V7 | Unique user/plate review constraint |
| V8 | Plate review status → lookup ids |
| V9 | Dropped location tables and `location_sharing_enabled` |
| V10 | Friendship status → lookup ids, active pair unique index |
| V11 | Created lookup tables, nullable id columns for enum-like fields |
| V12 | Lookup ids authoritative, dropped legacy columns/`premium_until`/profile aggregates |
| V23 | `social_platforms` gained `icon_url`, `background_color_hex`, `icon_tint_color_hex`, `created_at`, `updated_at`, converted `id` to an identity column, and seed data fixed (added `GITHUB`, deactivated `SNAPCHAT`) — turns the previously-dead lookup table into the source of truth for the Android social-link platform picker |
| V24 | Populated `icon_url` for seeded platforms (Simple Icons via jsDelivr) — V23 left it null, so the client rendered every platform with the generic fallback icon |
| V25 | `participants` gained `hidden_at` — supports per-user "delete conversation" (self-only, auto-revives when the other participant messages again) without touching shared `chat_rooms`/`chat_messages` content |

## Lookup Tables

`plate_review_statuses`, `friendship_request_statuses`, `user_subscription_statuses`, `plate_statuses`, `comment_report_reasons`, `comment_report_statuses`, `plate_removal_request_reasons`, `plate_removal_request_statuses`, `plate_report_severities`, `social_platforms`, `plate_review_moderation_action_types`, `user_role_codes`

Code uses helper classes (`PlateReviewStatus`, `UserRoleCode`) to resolve ids/codes.

`social_platforms` is the exception: `id, code, label, sort_order, active` (from V11/V12) plus `icon_url, background_color_hex, icon_tint_color_hex, created_at, updated_at` (from V23). No enum helper — `SocialMediaLinkManager`/`SocialPlatformManager` resolve rows directly via `ISocialPlatformLookupDao`, and the table is admin-manageable through `/api/admin/social-platforms`.

## Important Constraints & Indexes

* Unique `plates.plate_code`
* Unique `plate_reviews(user_id, plate_id)`
* Status/date indexes on review moderation and public review queries
* Moderation event indexes by review, to-status, actor, created date
* Friendship active pair unique expression index (direction-independent)
* Status id indexes on lookup-backed columns
* Unique user role code id

## Docker Setup

`docker-compose.yml`: `app` (Spring Boot, ports 8080/9092), `db` (PostGIS 15, named volume `postgres_data`). Init script: `docker/initdb/001_bootstrap_minimal_legacy_schema.sql`. Healthcheck: `pg_isready`.

## Open Questions

* Docker uses `ddl-auto=update` vs default `validate`.
* Some JPQL uses fixed lookup ids — seed id stability matters.
