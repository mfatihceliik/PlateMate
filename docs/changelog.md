---
type: backend-doc
area: changelog
tags: [backend, spring-boot]
updated: 2026-05-29
---

# Changelog

## Reading Guide

**Agent Context Rule:** To preserve token budget, you must ONLY read the **Recent Changes Summary** below for normal tasks.
Do NOT read detailed history unless explicitly required by a large refactor. Older detailed entries are stored in `[[changelog-archive]]`.

## Recent Changes Summary

* **Feature – Plate Detail Screen Enhancement (2026-06-17):** Enhanced `PlateDetailDto` with rating distribution (5→1 star bars with count/percentage), aggregated tag summary (report type labels with user counts), and rich review list (`PlateDetailReviewDto` with displayName, profilePhotoUrl, per-review reportTags). Created `RatingDistributionDto`, `PlateTagSummaryDto`, `PlateDetailReviewDto`. Added `getRatingDistribution` query to `IPlateReviewDao`, `findByPlateIdWithReportType` (JOIN FETCH) to `IPlateReportDao`, `findRecentByPlateCodeWithUserProfile` (JOIN FETCH user+profile) to `IPlateReviewDao`. Removed `PlateReportTypeMapper`/`PlateReviewMapper` dependencies from `PlateSearchManager`. Replaced `recentReportTypes` field with `tagSummary`. Updated test.
* **Feature – Change Password Endpoint (2026-06-17):** Added `PUT /api/auth/change-password` (JWT-protected) for authenticated password change. Created `ChangePasswordRequest` DTO with Jakarta validation (min 8 chars, uppercase, digit). Added `changePassword` to `IAuthService`/`AuthManager` with current password verification and same-password guard. Added `getByIdForAuth` to `IUserService`/`UserManager` for entity-level auth lookups. Narrowed `WebMvcConfig` JWT exclusion from `/api/auth/**` wildcard to specific public paths so change-password requires JWT.
* **Feature – Edit Profile Endpoint (2026-06-17):** Added `PUT /api/profiles/{userId}` (self-only) for updating displayName, username, bio, profilePhotoUrl. Created `UpdateProfileRequest` DTO with Jakarta validation. Added `displayName` field to `UserProfile` entity and `UserProfileDto`. Username uniqueness validated in `UserProfileManager`. Migration: V14.
* **Feature – Follow System & Profile Enhancements (2026-06-17):** Added unidirectional follow system (`Follow` entity, `IFollowDao`, `IFollowService`/`FollowManager`, `IFollowController`/`FollowController`). Added `bio`, `profilePhotoUrl`, `verified` fields to `UserProfile` entity. Created `UserProfileReviewDto` with `cityName` and `reportTags` for profile screen reviews. Updated `UserProfileDto` with `followerCount`, `followingCount`, `isFollowing`, `bio`, `profilePhotoUrl`, `verified` fields. Updated `UserProfileManager` to aggregate follow data and enhanced review data with plate city and report type tags. Migration: V13.
* **Code Quality – V12 Closed (2026-05-30):** Translated all remaining Turkish comments in the source code (entities, DTOs, mappers, controllers, etc.) to English and marked V12 as **Resolved**.
* **Architecture Refactor – V13 Closed (2026-05-30):** Marked V13 (Excessive dependency count) as **Resolved**. Phase 4 (PlateDiscoveryManager) was determined unnecessary since `DiscoveryManager` already handles discovery/trending operations. The original monolithic `PlateManager` (15 deps) has been fully decomposed into `PlateSearchManager`, `PlateReviewManager`, and `PlateModerationManager`.
* **Architecture Refactor – V13 Phase 3 (2026-05-30):** Extracted moderation responsibilities from `PlateReviewManager` into a dedicated `PlateModerationManager` (`IPlateModerationService`). Moved `ContentModerationService`, `HashingService`, and `PlateReviewModerationEventService` dependencies out of `PlateReviewManager`, reducing its dependency count by 2. Created `IPlateModerationService` interface with `resolveModeration`, `applyModerationMetadata`, `logReviewSubmitted`, and `logReviewRemovedByUser`. Updated `PlateReviewManagerTest` to mock the new interface. All 97 tests pass.
* **Documentation (2026-05-30):** Performed a final consistency pass across all documentation files. Verified Task Matrix links, known-violations statuses, dependency direction rules, and endpoint checklists.
  - **Architectural Cleanup**: Resolved multiple known violations (`V05`, `V09`, `V11`, `V14`).
  - Fixed exception message leaking in `ChatSocketHandler` by mapping to `Messages.UNEXPECTED_ERROR` and logging internally.
  - Removed Entity exposure from `INotificationService`, `IParticipantService`, and `IPlateReportService` by passing primitive types (IDs/names) and resolving entities in managers.
  - Normalized all remaining literal message strings to constants in `Messages.java` across controllers and managers.
  - Implemented missing `ICityController` and `CityController` to map to existing `ICityService` implementation, satisfying the `WebMvcConfig` exclusion rule.
* **Documentation (2026-05-30):** Documented a long-term refactor plan for `PlateManager` in `services-business.md` to address its excessive dependency count (V13). Proposed splitting responsibilities into `PlateSearchManager`, `PlateReviewManager`, `PlateModerationManager`, and `PlateDiscoveryManager`. Updated `known-violations.md` (V13) to point to this new plan.
* **Documentation (2026-05-30):** Updated `New Endpoint Checklist` in `conventions.md` to include comprehensive architectural verification steps (e.g., interface isolation, transaction boundaries, strict DTO usage, proper message key handling). Added a routing rule to `index.md` enforcing this checklist for all new endpoint and service tasks.
* **Documentation (2026-05-30):** Optimized `AGENTS.md` for token efficiency by extracting detailed workflows, context budget policies, source inspection strategies, and completion checklists into a new `docs/agent-workflow.md` file. Deduplicated source inspection matrices between `index.md` and `AGENTS.md`. `AGENTS.md` now acts strictly as a lightweight entry point with critical routing and non-negotiable architectural rules.
* **Documentation (2026-05-29):** Updated `Package Scale` metadata in `architecture.md` with exact counts from the source tree. Updated `index.md` to recommend using these exact metrics instead of running broad full-scans. Added note for `PlateManager`'s exact dependency count (15) and updated controller interface metrics.
* **Documentation (2026-05-29):** Expanded `Cross-Manager Dependencies` table in `services-business.md` with explicit notes for `AuthManager`, `PlateManager`, `FriendshipManager`, `ChatMessageManager`, and others. Updated `index.md` to specify when this graph must be consulted.
* **Documentation (2026-05-29):** Investigated `/api/cities/**` exclude rule in `WebMvcConfig`. Confirmed it is required for planned endpoints documented in API contracts and supported by `CityManager`, but `CityController` is missing (V14 clarified).
* **Code Cleanup (2026-05-29):** Removed unused `sendMessage(ChatMessage, Long)` method and its helper methods from `IChatMessageService` and `ChatMessageManager` to prevent entity leakage in service interface (V07 resolved).
* **Architecture Refactor (2026-05-29):** Extracted `SocketIOServer` usage in `NotificationManager` behind `ISocketPushService` to eliminate business layer dependency on the API/socket infrastructure (V01, V02 resolved).
* **Architecture Refactor (2026-05-29):** Created `IFcmTokensController` interface and refactored `FcmTokensController` to implement it, aligning with the standard controller interface pattern (V15).
* **Architecture Refactor (2026-05-29):** Added `@Transactional` to public state-changing (write) methods in `ParticipantManager`, `SocialMediaLinkManager`, `UserManager`, and `UserSettingsManager` to ensure transaction boundary consistency (V10).
* **Code Cleanup (2026-05-29):** Refactored string literal message keys to `Messages.*` constants in `AuthManager`, `ModerationAdminManager`, `PlateRemovalRequestManager`, and `CommentReportManager` (V11 partially resolved).
* **Architecture Refactor (2026-05-29):** Created `CityDto` and `CityMapper`, and updated `ICityService` to return DTOs instead of entities to prevent entity leaking (V08).
* **Architecture Refactor (2026-05-29):** Extracted `RefreshTokenServiceException` from `IRefreshTokenService` into `business.exceptions` to enforce the "method signatures only" rule for service interfaces (V06).
* **Architecture Refactor (2026-05-29):** Removed `IParticipantDao` from `ChatSocketHandler` and introduced `isRoomMember` in `IParticipantService` to enforce API-to-Service dependency rules (V04).
* **Architecture Refactor (2026-05-29):** Moved `SocketEvents` to `business.utilities.constants` to resolve API-to-business cross-layer dependency (V03).
* **Docs Consistency (2026-05-29):** Clarified in `notifications-fcm.md` that `DELETE /api/fcm-tokens/unregister` reads `userId` from the request attribute.
* **Docs Maintenance (2026-05-29):** Fixed incorrect Spring Boot version (updated from 3.x to 4.0.6) in `architecture.md`.
* **Comment Cleanup (2026-05-29):** Translated remaining Turkish source code comments to English.
* **ChatController Cleanup (2026-05-29):** Removed unused entity imports.
* **FcmTokensController Validation (2026-05-29):** Added `@Valid` to `FcmTokensController.register` and added localized message keys to `RegisterTokenRequest`.
* **GlobalExceptionHandler Fixes (2026-05-29):** Fixed exception leaks and replaced hardcoded messages with `IMessageService` constants.
* **Auth refactor (ARCH-001):** `AuthController` made thin; all auth logic moved to `AuthManager`/`IAuthService`. Login/register/refresh/logout endpoints standardized.
* **Entity construction cleanup (ARCH-004):** Removed JPA entity construction from controllers and socket handlers.
* **Manager readability (ARCH-010):** Refactored `PlateManager`, `CommentReportManager`, `ModerationAdminManager`, `ChatMessageManager` — extracted private helpers, `BusinessRules.run(...)`.
* **Repository cleanup (ARCH-005/009):** Removed hardcoded status IDs from JPQL, removed default method wrappers.
* **FCM/Chat fixes:** FCM unregister user-scoped, controller error handling standardized.
* **Chat authorization:** Business-layer participant checks for message read/send/mark-read.

### Notes
Some architectural fixes recorded here may not match the current source state. For the authoritative list of current architectural violations, always check `[[known-violations]]`.
