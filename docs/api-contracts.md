---
type: backend-doc
area: api-contracts
tags: [backend, spring-boot]
updated: 2026-05-28
---

# API Contracts

## Purpose

Canonical endpoint reference: routes, request/response DTOs, status codes, and auth requirements.

## Response Wrappers

| Wrapper | Shape |
| --- | --- |
| `Result` | `{ success, message }` |
| `DataResult<T>` | `Result` + `data` |
| `SuccessResult` / `ErrorResult` | Success/error command responses |
| `SuccessDataResult<T>` / `ErrorDataResult<T>` | Success/error data responses |
| `PagedData<T>` | `items` + pagination meta |

## Status Codes

| Status | When |
| --- | --- |
| `200 OK` | Most successful reads/updates/deletes |
| `201 Created` | Auth register, plate review create |
| `400 Bad Request` | Unsuccessful service results |
| `401 Unauthorized` | Login/refresh failure, missing/invalid JWT |
| `403 Forbidden` | Admin access failure, self-only access failure |

## Auth Endpoints

| Method/path | Request | Response | Auth | Notes |
| --- | --- | --- | --- | --- |
| `POST /api/auth/register` | `RegisterRequest` | `DataResult<UserDto>` | Public | 201 |
| `POST /api/auth/login` | `LoginRequest` | `DataResult<UserDto>` | Public | Invalid → 401 |
| `POST /api/auth/refresh` | `RefreshTokenRequest` | `DataResult<UserDto>` | Public | Failure → 401 with error code |
| `POST /api/auth/logout` | `RefreshTokenRequest` | `Result` | Public | Revokes refresh token |
| `PUT /api/auth/change-password` | `ChangePasswordRequest` | `Result` | JWT | Verifies current password; 400 on mismatch |

## User / Profile / Settings Endpoints

| Method/path | Request | Response | Auth |
| --- | --- | --- | --- |
| `GET /api/users` | token user id | `DataResult<List<UserAdminDto>>` | Admin |
| `GET /api/users/{id}` | path id | `DataResult<UserAdminDto>` | Admin |
| `GET /api/users/search?username=` | query | `DataResult<UserAdminDto>` | Admin |
| `PUT /api/users/{userId}` | `UpdateUserRequest` | `Result` | Self-only |
| `DELETE /api/users/{id}` | path id | `Result` | Self or admin |
| `GET /api/profiles/{userId}` | path id | `DataResult<UserProfileDto>` | JWT |
| `PUT /api/profiles/{userId}` | `UpdateProfileRequest` | `Result` | Self-only |
| `GET /api/settings/{userId}` | path id | `DataResult<UserSettingsDto>` | Self-only |
| `GET /api/settings/{userId}/overview` | path id | `DataResult<UserSettingsOverviewDto>` | Self-only |
| `PUT /api/settings/{userId}` | `UpdateSettingsRequest` | `Result` | Self-only |

## Follow Endpoints

| Method/path | Request | Response | Auth |
| --- | --- | --- | --- |
| `POST /api/follows/{userId}` | path id | `Result` | JWT |
| `DELETE /api/follows/{userId}` | path id | `Result` | JWT |

## Plate / Review / Report Endpoints

| Method/path | Request | Response | Auth |
| --- | --- | --- | --- |
| `GET /api/plates/search?plate=` | query | `DataResult<PlateDetailDto>` | JWT |
| `GET /api/plates/{plateCode}/reviews?page=&size=` | pagination | `DataResult<PagedData<PlateReviewDto>>` | JWT |
| `POST /api/plates/{plateCode}/reviews` | `AddPlateReviewRequest` | `Result` | JWT |
| `PUT /api/plates/reviews/{id}` | `UpdatePlateReviewRequest` | `Result` | Owner |
| `DELETE /api/plates/reviews/{id}` | path id | `Result` | Owner |
| `PUT /api/plates/{plateCode}/reports` | `SyncPlateReportsRequest` | `Result` | JWT |
| `POST /api/plates/{plateId}/removal-requests` | `AddPlateRemovalRequestRequest` | `DataResult<PlateRemovalRequestDto>` | JWT |
| `GET /api/plate-report-types` | none | `DataResult<List<PlateReportTypeDto>>` | JWT |

## Social / Friendship / Chat / Subscription Endpoints

| Method/path | Request | Response | Auth |
| --- | --- | --- | --- |
| `POST /api/social-links` | `AddSocialLinkRequest` | `Result` | JWT |
| `PUT /api/social-links` | `UpdateSocialLinkRequest` | `Result` | Owner |
| `DELETE /api/social-links/{id}` | path id | `Result` | Owner |
| `GET /api/social-platforms` | none | `DataResult<List<SocialPlatformDto>>` | JWT |
| `POST /api/friendships/request/{addresseeId}` | path id | `Result` | JWT |
| `PUT /api/friendships/{id}/accept` | path id | `Result` | Addressee |
| `PUT /api/friendships/{id}/reject` | path id | `Result` | Addressee |
| `DELETE /api/friendships/{id}` | path id | `Result` | Participant |
| `GET /api/friendships` | token user id | `DataResult<List<FriendshipDto>>` | JWT |
| `GET /api/friendships/pending` | token user id | `DataResult<List<FriendshipDto>>` | JWT |
| `GET /api/chat/rooms` | token user id | `DataResult<List<ChatRoomDto>>` | JWT |
| `POST /api/chat/rooms?otherUserId=` | query | `DataResult<ChatRoomDto>` | JWT |
| `GET /api/chat/rooms/{roomId}/messages` | path room id | `DataResult<List<ChatMessageDto>>` | Participant |
| `POST /api/chat/rooms/messages` | `SendMessageRequest` | `DataResult<ChatMessageDto>` | Participant |
| `PUT /api/chat/rooms/{roomId}/read` | path room id | `Result` | Participant |
| `POST /api/subscriptions/activate` | `ActivateSubscriptionRequest` | `DataResult<UserDto>` | JWT |
| `GET /api/subscriptions/me` | token user id | `DataResult<UserDto>` | JWT |
| `GET /api/subscriptions/me/history` | token user id | `DataResult<List<UserSubscriptionDto>>` | JWT |
| `POST /api/fcm-tokens/register` | `RegisterTokenRequest` | `Result` | JWT |
| `DELETE /api/fcm-tokens/unregister?token=` | query | `Result` | JWT |

## Discovery / Comment Report Endpoints

| Method/path | Request | Response | Auth |
| --- | --- | --- | --- |
| `GET /api/discovery/home?limit=&cityLimit=&activityLimit=` | query (defaults: 8,5,20) | `DataResult<DiscoveryHomeDto>` | JWT |
| `GET /api/discovery/cities/{cityId}/plates?page=&size=` | pagination | `DataResult<PagedData<CityPlateActivityDto>>` | JWT |
| `POST /api/comments/{commentId}/reports` | `AddCommentReportRequest` | `Result` | JWT |

## Admin Endpoints

| Method/path | Request | Response |
| --- | --- | --- |
| `GET /api/admin/comments/pending?page=&size=` | pagination | `DataResult<PagedData<PlateReviewAdminDto>>` |
| `PATCH /api/admin/comments/{commentId}/approve` | path id | `Result` |
| `PATCH /api/admin/comments/{commentId}/reject` | `AdminCommentModerationRequest` | `Result` |
| `PATCH /api/admin/comments/{commentId}/remove` | `AdminCommentModerationRequest` | `Result` |
| `GET /api/admin/plates/hidden?page=&size=` | pagination | `DataResult<PagedData<PlateAdminDto>>` |
| `PATCH /api/admin/plates/{plateId}/hide` | `HidePlateRequest` | `Result` |
| `PATCH /api/admin/plates/{plateId}/restore` | path id | `Result` |
| `GET /api/admin/comment-reports?page=&size=` | pagination | `DataResult<PagedData<CommentReportDto>>` |
| `PATCH /api/admin/comment-reports/{reportId}/review` | `ReviewCommentReportRequest` | `Result` |
| `GET /api/admin/plate-removal-requests?page=&size=` | pagination | `DataResult<PagedData<PlateRemovalRequestDto>>` |
| `PATCH /api/admin/plate-removal-requests/{requestId}/review` | `ReviewPlateRemovalRequestRequest` | `Result` |
| `GET /api/admin/plate-report-types` | token user id | `DataResult<List<PlateReportTypeAdminDto>>` |
| `POST /api/admin/plate-report-types` | `AddPlateReportTypeRequest` | `DataResult<PlateReportTypeAdminDto>` |
| `PUT /api/admin/plate-report-types/{id}` | `UpdatePlateReportTypeRequest` | `DataResult<PlateReportTypeAdminDto>` |
| `PATCH /api/admin/plate-report-types/{id}/active` | `UpdatePlateReportTypeActiveRequest` | `Result` |
| `GET /api/admin/social-platforms` | token user id | `DataResult<List<SocialPlatformAdminDto>>` |
| `POST /api/admin/social-platforms` | `AddSocialPlatformRequest` | `DataResult<SocialPlatformAdminDto>` |
| `PUT /api/admin/social-platforms/{id}` | `UpdateSocialPlatformRequest` | `DataResult<SocialPlatformAdminDto>` |
| `PATCH /api/admin/social-platforms/{id}/active` | `UpdateSocialPlatformActiveRequest` | `Result` |

All admin endpoints require `IAdminAccessService.checkAdmin(...)`.

## Conventions

* Lookup-backed DTOs include both id and code fields.
* Pagination defaults: `page=0`, `size=20`.
* Validation messages use message keys with `@Valid`.
* `/api/auth/**` excluded from JWT interceptor; all other `/api/**` require bearer token.
