---
type: backend-doc
area: services-business
tags: [backend, spring-boot]
updated: 2026-05-29
---

# Services Business

## Purpose

Documents service interfaces, manager implementations, business rules, validation flow, and domain logic.

## Structure

Interfaces: `business/abstracts/I*Service`. Implementations: `business/concrete/*Manager`.
Flow: `Controller → Service Interface → Manager → Repository/DataAccess`

## Service-Manager Map

| Manager | Interface | Main responsibility | Key dependencies |
| --- | --- | --- | --- |
| `AdminAccessManager` | `IAdminAccessService` | Admin role check | `IUserDao` |
| `AuthManager` | `IAuthService` | Register, login, refresh, logout | `IUserService`, `IRefreshTokenService`, `PasswordEncoder`, `JwtTokenProvider` |
| `RefreshTokenManager` | `IRefreshTokenService` | Token issue/refresh/revoke | `IUserRefreshTokenDao`, `JwtTokenProvider` |
| `UserManager` | `IUserService` | User CRUD and auth lookups | `IUserDao`, `IUserRoleDao`, `IUserSubscriptionDao` |
| `UserProfileManager` | `IUserProfileService` | Profile aggregation (self/non-self visibility) | `IUserProfileDao`, `IFriendshipDao`, `IPlateReviewDao`, `IUserSettingsService` |
| `UserSettingsManager` | `IUserSettingsService` | Settings defaults, updates, overview | `IUserSettingsDao`, `IUserDao`, `IUserProfileDao`, `IUserSubscriptionDao` |
| `PlateSearchManager` | `IPlateSearchService` | Plate search, plate creation, aggregate dto | `IPlateDao`, `IPlateReviewDao`, `IPlateReportDao`, `IPlateSearchEventDao`, `ICityDao` |
| `PlateReviewManager` | `IPlateReviewService` | Plate review lifecycle (add/update/delete) | `IPlateDao`, `IPlateReviewDao`, `IPlateReportService`, `IPlateModerationService` |
| `PlateModerationManager` | `IPlateModerationService` | Content moderation, request hashing, moderation event logging | `ContentModerationService`, `HashingService`, `PlateReviewModerationEventService` |
| `PlateReportManager` | `IPlateReportService` | Sync report tags per user/plate | `IPlateReportDao`, `IPlateReportTypeDao` |
| `PlateReportTypeManager` | `IPlateReportTypeService` | Report type listing and admin changes | `IPlateReportTypeDao`, policy service |
| `FriendshipManager` | `IFriendshipService` | Request/accept/reject/remove/list | `IFriendshipDao`, `IUserDao`, `INotificationService` |
| `ModerationAdminManager` | `IModerationAdminService` | Admin approve/reject/remove, hide/restore | `IPlateReviewDao`, `IPlateDao`, moderation event service |
| `CommentReportManager` | `ICommentReportService` | Report creation, threshold escalation, admin review | `ICommentReportDao`, `IPlateReviewDao`, `IPlateDao` |
| `PlateRemovalRequestManager` | `IPlateRemovalRequestService` | Removal requests and admin review | `IPlateRemovalRequestDao`, `IPlateDao` |
| `SubscriptionManager` | `ISubscriptionService` | Premium activation, status/role sync | `IUserSubscriptionDao`, `IUserDao`, `IUserRoleDao` |
| `SocialMediaLinkManager` | `ISocialMediaLinkService` | Profile social link CRUD | `ISocialMediaLinkDao` |
| `ChatRoomManager` | `IChatRoomService` | Room creation/listing | `IChatRoomDao`, `IParticipantService` |
| `ChatMessageManager` | `IChatMessageService` | Message send/list/read + notifications | `IChatMessageDao`, `IUserSettingsDao`, `INotificationService` |
| `ParticipantManager` | `IParticipantService` | Chat participants | `IParticipantDao` |
| `NotificationManager` | `INotificationService` | Settings gate + socket + FCM push | `IFcmTokenDao`, `SocketIOServer` |
| `FcmTokenManager` | `IFcmTokenService` | Device token register/unregister | `IFcmTokenDao` |
| `FcmManager` | `IFcmService` | Firebase push send | Firebase Admin SDK |
| `DiscoveryManager` | `IDiscoveryService` | Discovery home and city activity | city DAO, discovery utilities |
| `CityManager` | `ICityService` | City listing/lookup | `ICityDao` |

## Cross-Manager Call Graph

| Caller | Calls | Via interface | Notes |
| --- | --- | --- | --- |
| `AuthManager` | `UserManager` | `IUserService` | Register flow and login user lookup |
| `AuthManager` | `RefreshTokenManager` | `IRefreshTokenService` | Token generation, refresh flow, and revoking |
| `PlateReviewManager` | `PlateReportManager` | `IPlateReportService` | Syncing user reports for a plate during review creation/update |
| `PlateReviewManager` | `PlateModerationManager` | `IPlateModerationService` | Delegating moderation, hashing, and event logging |
| `FriendshipManager` | `NotificationManager` | `INotificationService` | Triggering notifications for new friend requests |
| `ChatRoomManager` | `ParticipantManager` | `IParticipantService` | Validating and managing room membership |
| `ChatMessageManager` | `NotificationManager` | `INotificationService` | Triggering real-time and push notifications for new messages |
| `UserProfileManager` | `UserSettingsManager` | `IUserSettingsService` | Fetching user settings for profile visibility rules when viewing self-profile |
| `CommentReportManager` | `ModerationAdminManager` | indirect | Triggering admin review when report threshold is met |

## Manager Implementation Pattern

1. Load entities via repositories
2. Guard checks (`BusinessRules.run(...)` or private helpers)
3. Business mutation or read composition
4. Persist via repositories
5. Map to DTOs via mappers
6. Return `Result` / `DataResult<T>` with `Messages.*` constants via `IMessageService`

## Private Helper Convention

Keep small local rules as private helpers when only used by one manager. Examples:

* `FriendshipManager`: `checkIfSelfRequest`, `checkIfAlreadyExists`, `checkIfUserAuthorized`
* `PlateManager`: `normalizePlate`, `validateSubmissionRules`, `applyReviewMutation`, `refreshPlateStatistics`
* `UserProfileManager`: `buildFullReviewStatusCounts`, `buildViewerStatusCounts`, `toEvaluationTotals`
* `SubscriptionManager`: `resolveBaseStart`, `syncSubscriptionStatuses`, `syncRoleFromSubscriptions`

Extract when: reused by multiple managers, complex enough for separate testing, or represents a separate concern. Targets: validator, mapper, policy, domain service, feature service interface.

## Important Business Rules

### Auth
* Register creates user via `IUserService.add`, issues tokens via `IRefreshTokenService`.
* Login compares raw password with hash via `PasswordEncoder.matches`.
* Refresh failure throws `RefreshTokenServiceException` → 401 with error code (`REFRESH_EXPIRED`/`REVOKED`/`INVALID`).
* User delete: self or admin. User update: self-only.

### Profile & Settings
* Profile owner sees all review statuses + recent friend requests. Non-self sees approved reviews only.
* `reviewCount` = all user reviews. `averageGivenRating` = sum/count, 0.0 when none.
* `totalFriendCounts` = accepted friendships. Profile limits: 10 reviews, 10 friend requests.
* Settings defaults on first access: messaging/message notifications/friend notifications enabled.
* Overview includes email, premium flags, settings, social links.

### Plate & Reviews
* `searchByPlateCode`: normalize (remove spaces, uppercase), validate via `IPlateValidator`.
* Searching/reviewing creates plate if missing. Only `ACTIVE` plates publicly visible.
* Public reviews = approved only. Rating: 1–5. Comment: optional, max 250.
* Non-premium: no free-text, must provide report type codes. Premium: free-text allowed, moderation applies.
* New/updated reviews → `PENDING_REVIEW`. Existing non-rejected review blocks new POST.
* Rejected review → resubmit via POST, logs `SUBMITTED_FOR_REVIEW`.
* Delete → `REMOVED_BY_USER`, sets `deletedAt`, refreshes plate stats.
* Plate aggregates refreshed from approved reviews only.

### Moderation & Reports
* Admin: approve → `APPROVED`, reject → `REJECTED`, remove → `REMOVED_BY_MODERATOR`. Each logs event + refreshes stats.
* Comment report: blocks duplicate reporter/comment. Threshold (`moderation.comment-report-threshold`, default 3) triggers auto-pending.
* Accepted report review → removes comment by moderator.

### Friendships
* Self-request rejected. Active pair checked direction-independently.
* New: `REQUESTED`. Accept/reject: addressee only. Remove: sets `REMOVED` (soft).
* `getFriends`: accepted. `getPendingRequests`: requested where user is addressee.

### Subscriptions
* Base start: latest future non-canceled expiry or now. Status: `PENDING` (future) / `ACTIVE` (current).
* Sync: `ACTIVE`/`PENDING`/`EXPIRED` by time; `CANCELED` untouched.
* Premium role when active subscription exists; admin role never downgraded.

### Chat
* All message operations enforce room participant authorization in business layer.
* Recipient `messagingEnabled` checked for 1:1 rooms before send.

### Social Links
* Platform must resolve from request id/code. No duplicate platform per profile. Owner-only update/delete.

## PlateManager Refactor Plan

Previously, `PlateManager` had an excessive dependency count (15 dependencies) and handled multiple distinct responsibilities. To adhere to SOLID principles and prevent the class from becoming unmaintainable, a long-term refactor plan was established.

**Status Update:** `PlateManager` has been completely deleted as of Phase 2! Its core responsibilities were split. However, further modularization is still planned for Phase 3 and 4 to remove moderation and discovery concerns from the review and search managers.

**IMPORTANT RULES FOR NEW FEATURES:**
* Do **NOT** add new dependencies or new distinct domain responsibilities to `PlateSearchManager` or `PlateReviewManager`.
* When adding new plate-related behavior, always consider creating a specific manager instead of inflating existing ones.

### Target Managers

1. **`PlateSearchManager` (COMPLETED - Phase 1)**: Responsible for plate searching (`searchByPlateCode`), plate creation on first search, and basic plate detail aggregation.
2. **`PlateReviewManager` (COMPLETED - Phase 2)**: Responsible for the core review lifecycle (adding, updating, deleting reviews), validating submission rules.
3. **`PlateModerationManager` (COMPLETED - Phase 3)**: Responsible for content moderation (integration with `ContentModerationService`), IP/UserAgent hashing (`HashingService`), and logging moderation events (`PlateReviewModerationEventService`). Extracted from `PlateReviewManager`.
4. **`PlateDiscoveryManager` (PENDING - Phase 4)**: Will be responsible for discovery/trending operations, logging search events (`IPlateSearchEventDao`), computing plate scores, and integrating with city resolution (`TrPlateCityResolver` / `ICityDao`). Currently intertwined in `PlateSearchManager`.

## Open Questions

* Several service interfaces import `entities.concrete`: `INotificationService`, `IParticipantService`, `IPlateReportService`. See [[known-violations]].
