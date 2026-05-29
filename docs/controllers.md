---
type: backend-doc
area: controllers
tags: [backend, spring-boot]
updated: 2026-05-29
---

# Controllers

## Purpose

Documents REST controller architecture, conventions, and access rules. For endpoint tables and request/response DTOs, see [[api-contracts]].

## Conventions

* Interfaces under `api/controllers/abstracts/I*Controller` define routes and method signatures.
* Concrete implementations under `api/controllers/concrete/*Controller` inject service interfaces and map results to HTTP.
* Authenticated controllers receive `@RequestAttribute("userId") Long currentUserId`.
* Admin controllers call `IAdminAccessService.checkAdmin(...)` → `403` on failure.
* Self-only controllers compare path user id with token user id → `403` on mismatch.
* Service failures → `400 Bad Request`. Create endpoints → `201 Created`.
* `@Valid @RequestBody` for request DTO validation.
* Controllers must not import `entities.concrete` or `dataAccess`.

## Controller-to-Service Map

| Controller | Base route | Service | Notes |
| --- | --- | --- | --- |
| `AuthController` | `/api/auth` | `IAuthService` | Public auth. |
| `UserController` | `/api/users` | `IUserService`, `IAdminAccessService` | Admin reads; self/admin delete; self update. |
| `UserProfileController` | `/api/profiles` | `IUserProfileService` | Requester id for visibility. |
| `UserSettingsController` | `/api/settings` | `IUserSettingsService` | Self-only. |
| `FriendshipController` | `/api/friendships` | `IFriendshipService` | Request lifecycle. |
| `PlateController` | `/api/plates` | `IPlateService` | Search, reviews, reports. |
| `PlateRemovalRequestController` | `/api/plates` | `IPlateRemovalRequestService` | Removal requests. |
| `PlateReportTypeController` | `/api/plate-report-types` | `IPlateReportTypeService` | Public report types. |
| `SocialMediaLinkController` | `/api/social-links` | `ISocialMediaLinkService` | Social links CRUD. |
| `ChatController` | `/api/chat` | `IChatRoomService`, `IChatMessageService` | Rooms and messages. |
| `SubscriptionController` | `/api/subscriptions` | `ISubscriptionService` | Premium activation/history. |
| `DiscoveryController` | `/api/discovery` | `IDiscoveryService` | Home and city activity. |
| `CommentReportController` | `/api/comments` | `ICommentReportService` | User comment reports. |
| `FcmTokensController` | `/api/fcm-tokens` | `IFcmTokenService` | |
| `AdminModerationController` | `/api/admin` | `IAdminAccessService`, `IModerationAdminService` | Admin moderation. |
| `AdminCommentReportController` | `/api/admin/comment-reports` | `IAdminAccessService`, `ICommentReportService` | Admin report review. |
| `AdminPlateRemovalRequestController` | `/api/admin/plate-removal-requests` | `IAdminAccessService`, `IPlateRemovalRequestService` | Admin removal review. |
| `AdminPlateReportTypeController` | `/api/admin/plate-report-types` | `IAdminAccessService`, `IPlateReportTypeService` | Admin report type CRUD. |
| `SwaggerRedirectController` | `/` | none | Redirects to Swagger UI. |

## Open Questions

