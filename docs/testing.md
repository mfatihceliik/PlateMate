---
type: backend-doc
area: testing
tags: [backend, spring-boot, testing]
updated: 2026-05-28
---

# Testing

## Purpose

Documents test structure, styles, commands, and coverage.

## Test Areas

| Package | Focus | Style |
| --- | --- | --- |
| `api/controllers` | Controller behavior, auth guards, pagination | Unit with mocked services |
| `business/concrete` | Manager rules, status transitions | Mockito `@ExtendWith(MockitoExtension.class)` |
| `business/discovery` | Time-window helpers | Unit |
| `business/scheduling` | Retention scheduler cutoff | Unit |
| `business/utilities/moderation` | Content moderation rules | Unit |
| `config/jwt` | JWT provider behavior | Unit |
| `core/utilities` | Mappers, pagination | Unit |
| `entities/dto` | JSON serialization field names | Unit |
| Root `PlatemateApplicationTests` | Spring context smoke test | Integration |

## Common Patterns

* Direct instantiation with mocked dependencies (no full Spring context for most tests)
* `ArgumentCaptor` for persisted entities
* `ReflectionTestUtils.setField(...)` for `@Value`-backed fields
* `MockMvcBuilders.standaloneSetup(...)` for auth interceptor tests

## Commands

```powershell
.\gradlew.bat test                                                    # all tests
.\gradlew.bat test --tests "com.mefy.platemate.business.concrete.PlateManagerTest"  # specific
.\gradlew.bat compileJava                                             # compile check
```

## What to Run After Changes

| Change type | Tests |
| --- | --- |
| Auth/JWT | `AuthControllerTest`, `AuthManagerTest`, `RefreshTokenManagerTest`, `JwtTokenProviderTest` |
| Controller/API | touched controller tests + `PaginationValidationUnitTest` |
| Plate/review | `PlateManagerTest`, moderation/report manager tests |
| Moderation/compliance | `CommentReportManagerTest`, `ModerationAdminManagerTest`, `PlateRemovalRequestManagerTest` |
| Discovery | `DiscoveryManagerTest`, `DiscoveryTimeWindowServiceTest`, `DiscoveryDtoSerializationTest` |
| Core utilities | `PaginationRequestTest`, mapper tests + impacted manager tests |
| FCM/notifications | full `.\gradlew.bat test` (no focused tests) |

## Coverage Gaps

No tests for: `SocketModule`, `ChatSocketHandler`, `FcmTokenManager`, `FcmManager`, `NotificationManager`, `ComplianceRetentionScheduler`.
