---
type: backend-doc
area: changelog-archive
tags: [backend, spring-boot]
updated: 2026-05-29
---

# Changelog Archive

Full historical change entries. Read this only if a large refactor requires deep historical context.

## Full Change Entries

### 2026-05-29 (GlobalExceptionHandler Fixes)

Fixed exception message leaks in `GlobalExceptionHandler.handleGeneralExceptions` and `handleTypeMismatchException`. Replaced hardcoded Turkish strings with `IMessageService` and `Messages.*` constants. Added server-side logging for unhandled exceptions.

### 2026-05-28 (Business Manager Refactor)

Refactored `CommentReportManager`, `FcmTokenManager`, `ParticipantManager`, `UserSettingsManager`, `PlateRemovalRequestManager`, `UserProfileManager`, `PlateManager` — `BusinessRules.run(...)` chaining, private helpers, safe lookups.

### 2026-05-28 (ChatMessageManager Refactor)

Unified `sendMessage` overloads via `processSendMessage(...)` internal flow. Extracted `validateSendMessage`, `persistMessageAndTouchRoom`, `notifyRecipientIfNeeded`.

### 2026-05-28 (Auth Refactor — ARCH-001 Complete)

`AuthController` → thin, delegates to `IAuthService`/`AuthManager`. Token attachment, DTO mapping, refresh error handling moved to business layer. JWT secret fallback length fixed.

### 2026-05-28 (Auth Refactor — ARCH-001 Step 1)

Introduced `IAuthService`/`AuthManager`, moved login workflow out of controller.

### 2026-05-28 (ARCH-010 ModerationAdminManager)

Extracted helper methods for approve/reject/remove comment flows.

### 2026-05-28 (ARCH-010 CommentReportManager)

Extracted validation, duplicate check, threshold evaluation into helpers.

### 2026-05-28 (ARCH-010 PlateManager.addReview)

Extracted input validation, existing review check, resubmission, new submission into helpers.

### 2026-05-28 (ARCH-004 AuthController.register)

Removed entity construction from register endpoint.

### 2026-05-28 (ARCH-004 UserController)

Removed entity construction from update endpoint.

### 2026-05-28 (ARCH-004 SocialMediaLinkController)

Removed entity construction and platform mapping from controller.

### 2026-05-28 (ARCH-004 ChatSocketHandler)

Removed entity construction from `handleSendMessage`.

### 2026-05-28 (ARCH-004 ChatController)

Removed entity construction from `sendMessage`.

### 2026-05-28 (ARCH-005/009 Repository Cleanup)

Removed default method wrappers and hardcoded status IDs from DAOs.

### 2026-05-28 (ARCH-003 ChatMessageManager)

Replaced `RuntimeException` with `ErrorDataResult` pattern.

### 2026-05-28 (ARCH-006 RefreshTokenServiceException)

Moved nested exception to standalone class. *Note: source may still have nested class — verify against [[known-violations]].*

### 2026-05-28 (ARCH-007 FCM Token Unregister)

User-scoped ownership check before delete.

### 2026-05-28 (ARCH-008 Controller Error Handling)

`FcmTokensController` and `ChatController` return 400 on service failure.

### 2026-05-28 (Chat Authorization)

Business-layer participant checks for all chat message operations.

### 2026-05-28 (Doc Expansion)

Created full documentation set: utilities, socket, notifications, moderation, infrastructure, contracts, testing, deviations.

### 2026-05-28 (Doc Update)

Added architecture deviation sections across docs, corrected `RefreshTokenServiceException` reference.

### 2026-05-28 (Initial Doc Set)

Expanded controller routes, service responsibilities, repository patterns, entity/DTO contracts, database migrations, security flow, API contracts, conventions.
