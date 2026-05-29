---
type: backend-doc
area: core-utilities
tags: [backend, spring-boot]
updated: 2026-05-29
---

# Core Utilities

## Purpose

Documents result wrappers, pagination, mappers, message abstraction, and global exception handling.

## Result Wrappers (`core.utilities.results`)

* `Result`: `{ success, message }`
* `DataResult<T>`: `Result` + `data`
* `SuccessResult` / `ErrorResult`, `SuccessDataResult<T>` / `ErrorDataResult<T>`

Controller pattern: call service → if `!result.isSuccess()` return 400 (or 403/401) → else return 200/201.

## Pagination (`core.utilities.pagination`)

`PaginationRequest`: validates page (default 0) and size (default 20, max 100). Invalid → `InvalidPaginationException`.

`PaginationMapper.fromPage(...)`: converts Spring `Page<T>` to `PagedData<T>` with meta (page, size, totalElements, totalPages, hasNext, hasPrevious).

Used by: `PlateManager`, `CommentReportManager`, `PlateRemovalRequestManager`, `ModerationAdminManager`.

## Mappers (`core.utilities.mappers`)

All implement `ModelMapperService<E, D>`. `dtoToEntity(...)` mostly unused.

| Mapper | Conversion |
| --- | --- |
| `UserMapper` | `User` → `UserDto` |
| `UserProfileMapper` | `UserProfile` → `UserProfileDto` (+ social links) |
| `UserSettingsMapper` | `UserSettings` → `UserSettingsDto` |
| `FriendshipMapper` | `Friendship` → `FriendshipDto` (current-user perspective) |
| `PlateReviewMapper` | `PlateReview` → `PlateReviewDto` (statusId + statusCode) |
| `PlateReportTypeMapper` | `PlateReportType` → `PlateReportTypeDto` (policy-adjusted) |
| `SocialMediaLinkMapper` | `SocialMediaLink` → `SocialMediaLinkDto` |
| `ChatRoomMapper` | `ChatRoom` → `ChatRoomDto` |
| `ChatMessageMapper` | `ChatMessage` → `ChatMessageDto` |

## Messages (`core.utilities.messages`)

`IMessageService.getMessage(key)` / `getMessage(key, args)`. `MessageManager` delegates to Spring `MessageSource` + `LocaleContextHolder`. Default locale: `tr`. Basename: `messages`.

All managers should use `Messages.*` constants, not string literal keys.

## GlobalExceptionHandler (`core.exceptions`)

`@RestControllerAdvice`:
* `MethodArgumentNotValidException` → 400, `ErrorDataResult<Map<String,String>>`
* `InvalidPaginationException` → 400, localized message
* `MethodArgumentTypeMismatchException` → 400, generic param error
* `Exception` → 500, generic unexpected error


