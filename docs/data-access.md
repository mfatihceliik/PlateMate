---
type: backend-doc
area: data-access
tags: [backend, spring-boot]
updated: 2026-05-28
---

# Data Access

## Purpose

Documents repositories, important query methods, persistence conventions, and service relationships.

## Structure

Repositories: `dataAccess/abstracts/I*Dao` extending `JpaRepository`. Projections: `dataAccess/projections`. No separate persistence adapter layer.

## Repository Table

| Repository | Entity | Key queries | Main users |
| --- | --- | --- | --- |
| `IUserDao` | `User` | user/active/auth identifier lookup | `UserManager`, `FriendshipManager`, `AdminAccessManager` |
| `IUserRoleDao` | `UserRole` | role by code/code id | `UserManager`, `SubscriptionManager`, seed config |
| `IUserRefreshTokenDao` | `UserRefreshToken` | token persistence/revocation | `RefreshTokenManager` |
| `IUserSettingsDao` | `UserSettings` | `findByUserId` | `UserSettingsManager`, chat/notification managers |
| `IUserProfileDao` | `UserProfile` | `findByIdWithSocialMediaLinks` | `UserProfileManager`, `UserSettingsManager` |
| `IUserSubscriptionDao` | `UserSubscription` | `findByUserIdOrderByCreatedAtDesc` | `SubscriptionManager`, `UserSettingsManager` |
| `IPlateDao` | `Plate` | plate code lookup, status queries, hidden list | `PlateManager`, `ModerationAdminManager` |
| `IPlateReviewDao` | `PlateReview` | reviews by plate/user/status, counts, rating sums, activity projections | `PlateManager`, `UserProfileManager`, moderation/discovery |
| `IPlateReviewModerationEventDao` | `PlateReviewModerationEvent` | event persistence | `PlateReviewModerationEventService` |
| `IPlateReportDao` | `PlateReport` | active counts, weighted score, activity | `PlateManager`, `PlateReportManager`, discovery |
| `IPlateReportTypeDao` | `PlateReportType` | active types, code lookup, admin list | `PlateReportTypeManager`, `PlateReportManager` |
| `IPlateSearchEventDao` | `PlateSearchEvent` | search counts, timestamps | `PlateManager`, discovery |
| `IPlateRemovalRequestDao` | `PlateRemovalRequest` | listing, status filters | `PlateRemovalRequestManager` |
| `ICommentReportDao` | `CommentReport` | duplicate check, paged listing | `CommentReportManager` |
| `IFollowDao` | `Follow` | follower/following counts, existence check, find by pair | `FollowManager` |
| `IFriendshipDao` | `Friendship` | active pair count, accepted/pending lists, recent profile | `FriendshipManager`, `UserProfileManager` |
| `ISocialMediaLinkDao` | `SocialMediaLink` | duplicate platform check, id lookup | `SocialMediaLinkManager` |
| `IChatRoomDao` | `ChatRoom` | persistence/listing | `ChatRoomManager` |
| `IChatMessageDao` | `ChatMessage` | persistence/listing/read | `ChatMessageManager` |
| `IParticipantDao` | `Participant` | `existsByUserIdAndChatRoomId`, private room lookup | `ParticipantManager`, `ChatMessageManager` |
| `IFcmTokenDao` | `UserFcmToken` | token/device registration, lookup | `FcmTokenManager`, `NotificationManager` |
| `ICityDao` | `City` | listing/lookup | `CityManager`, `PlateManager`, `DiscoveryManager` |

## Key Query Patterns

**Review visibility:** `IPlateReviewDao` supports public approved lists (`findByPlatePlateCodeAndStatusId`), self profile lists (`findByUserId`), non-self approved lists (`findByUserIdAndStatusId`), plate aggregate counts/sums, and discovery projections. Default methods delegate to id-based queries.

**Friendship access:** `IFriendshipDao` uses direction-independent requester/addressee checks. `countActiveBetweenUsers` blocks duplicates. Status-based queries for accepted/pending/recent lists.

**Discovery projections:** Projection interfaces for city stats, daily aggregates, and recent activity — used by discovery utility services.

## Conventions

* Managers inject repos via final fields + Lombok `@RequiredArgsConstructor`.
* JPQL when derived queries aren't expressive enough.
* Pagination: Spring `Pageable` → `PaginationMapper.fromPage(...)`.
* Lookup domains queried by `*_id`. Compatibility helpers accept enum wrappers.
* Plate aggregate fields refreshed by service logic after review/moderation changes.
* JPQL queries fully parameterized (`:statusId`, `:plateStatusId`), except hardcoded ids in some `IPlateReviewDao`/`IPlateReportDao` queries.
