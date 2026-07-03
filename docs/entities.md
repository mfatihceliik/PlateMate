---
type: backend-doc
area: entities
tags: [backend, spring-boot]
updated: 2026-05-28
---

# Entities

## Purpose

Documents entities, DTOs, lookup models, relationships, and validation annotations.

## Structure

Entities: `entities/concrete`. DTOs: `entities/dto`. Requests: `entities/dto/request`. Most DTOs implement `IDto`.

## Entity Groups

| Group | Entities | Notes |
| --- | --- | --- |
| User/account | `User`, `UserProfile`, `UserSettings`, `UserRole`, `UserRoleCodeLookup`, `UserRefreshToken`, `UserFcmToken` | User owns profile/settings/subscriptions/tokens. Role uses lookup code id. |
| Subscription | `UserSubscription`, `UserSubscriptionStatus`, `UserSubscriptionStatusLookup` | Expiry drives premium state and role sync. |
| Plate | `Plate`, `City`, `PlateSearchEvent` | Plate may have city; search events track searches. |
| Reviews | `PlateReview`, `PlateReviewStatus`, `PlateReviewStatusLookup`, `PlateReviewModerationEvent`, `PlateReviewModerationActionTypeLookup` | Review → plate + user; moderation events audit transitions. |
| Reports | `PlateReport`, `PlateReportType`, `PlateReportSeverityLookup` | Report tags per user/plate; types have weight, severity. |
| Removal requests | `PlateRemovalRequest`, reason/status lookups | User requests for plate hiding; admin reviews. |
| Comment reports | `CommentReport`, reason/status lookups | Reports against reviews; admin review. |
| Follow | `Follow` | Unidirectional follower/following relationship. |
| Friendship | `Friendship`, `FriendshipRequestStatusLookup` | Requester/addressee with status id. |
| Social | `SocialMediaLink`, `SocialPlatformLookup` | Links belong to profile; platform id/code. `SocialPlatformLookup` is a fully admin-manageable catalog (code, label, iconUrl, colors, sortOrder, active) — no enum helper, validation resolves against the DB table via `ISocialPlatformLookupDao`. |
| Chat | `ChatRoom`, `ChatMessage`, `Participant` | Private rooms with participants and messages. `Participant.hiddenAt` supports per-user "delete conversation" (self-only, cleared automatically when the other participant sends a new message or the user reopens the chat) — `ChatRoom`/`ChatMessage` rows are never touched. |
| Notifications | `NotificationType`, `NotificationSignalDto` | Code-level only (not lookup-backed). |

## Key Response DTOs

| DTO | Key fields | Used by |
| --- | --- | --- |
| `UserDto` | id, username, email, token, refreshToken, premiumUntil, premiumActive, roleId, roleCode | Auth, subscription |
| `UserProfileDto` | displayName, bio, profilePhotoUrl, verified, followerCount, followingCount, isFollowing, totalFriendCounts, averageGivenRating, reviewCount, reviewStatusCounts, evaluationTotals, socialMediaLinks, plateReviews, friendRequests | Profile |
| `UserProfileReviewDto` | plateCode, cityName, rating, comment, reviewStatusId, reviewStatusCode, reportTags | Profile reviews |
| `UserSettingsDto` | messagingEnabled, messageNotificationsEnabled, friendNotificationsEnabled | Settings/profile |
| `UserSettingsOverviewDto` | email, premiumActive, premiumUntil, userSettings, socialMediaLinks | Settings overview |
| `PlateDetailDto` | plate identity, city, rating metrics, ratingDistribution, tagSummary, recentReviews | Plate search |
| `PlateDetailReviewDto` | id, userId, username, displayName, profilePhotoUrl, rating, comment, reportTags, createdAt | Plate detail reviews |
| `RatingDistributionDto` | rating (1-5), count, percentage | Plate detail rating bars |
| `PlateTagSummaryDto` | code, label, iconKey, colorHex, count | Plate detail aggregated tags |
| `PlateReviewDto` | plateCode, rating, comment, reviewStatusId, reviewStatusCode | Reviews |
| `FriendshipDto` | friend user id/name, statusId, statusCode | Friendships |
| `CommentReportDto` | comment/report/user ids, reason/status id/code, admin note | Admin reports |
| `PlateRemovalRequestDto` | plate/requester/reason/status/admin fields | Removal requests |

## Key Request DTOs and Validation

| Request | Validation | Used by |
| --- | --- | --- |
| `UpdateProfileRequest` | displayName: max 50; username: 3-30; bio: max 160; profilePhotoUrl: max 500 | Edit profile |
| `ChangePasswordRequest` | currentPassword: notBlank; newPassword: notBlank, min 8, uppercase, digit | Change password |
| `RegisterRequest` | username: notBlank, 3-30; password: notBlank, min 6; email: format | Register |
| `LoginRequest` | password: notBlank | Login |
| `AddPlateReviewRequest` | rating: notNull, 1-5; comment: max 250 | Add review |
| `UpdatePlateReviewRequest` | rating: notNull, 1-5; comment: max 250 | Update review |
| `SyncPlateReportsRequest` | report type code list: notNull | Report sync |
| `AddCommentReportRequest` | description: max 1000 | Comment report |
| `AddPlateRemovalRequestRequest` | description: notBlank, max 1000; requesterEmail: max 255 | Removal request |
| `AddSocialLinkRequest` | url: notBlank | Social link |
| `SendMessageRequest` | roomId: notNull; content: notBlank | Chat message |
| `ActivateSubscriptionRequest` | days: notNull, 1-365 | Subscription |
| `AdminCommentModerationRequest` | reason: max 500 | Admin reject/remove |
| `HidePlateRequest` | reason: notBlank, max 500 | Admin hide plate |
| `AddPlateReportTypeRequest` | code/label/description/icon/color: required; weight/sort: min 1 | Admin report type |
| `AddSocialPlatformRequest` | code/label: notBlank; backgroundColorHex/iconTintColorHex: hex pattern if present; sortOrder: min 1 | Admin social platform |

## Lookup Models

Two forms: JPA lookup entities (`*Lookup`) and enum-like helper classes (`PlateReviewStatus`, `CommentReportStatus`, `UserRoleCode`). Helpers resolve id/code for readable business logic. DTOs expose both id and code to clients. `SocialPlatformLookup` is the one lookup-backed domain with no enum helper — it's admin-manageable (see `ISocialPlatformService`), so validation queries the DB directly instead of a static enum.

## Key Relationships

* `PlateReview` → `Plate`, `User`, status lookup
* `PlateReviewModerationEvent` → `PlateReview`, from/to status, action type
* `Friendship` → requester/addressee `User`, status lookup
* `UserProfile` owns `SocialMediaLink` list
* `ChatMessage` → `ChatRoom`, sender user
* `Participant` links users to chat rooms
* `PlateReport` → user, plate, report type
* `CommentReport` → plate review
* `PlateRemovalRequest` → plate, requester
