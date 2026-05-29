---
type: backend-doc
area: moderation-compliance
tags: [backend, spring-boot, moderation]
updated: 2026-05-28
---

# Moderation Compliance

## Purpose

Documents moderation lifecycles, compliance retention jobs, and status transitions.

## Components

| Area | Class | Role |
| --- | --- | --- |
| Content scan | `ContentModerationService` | Comment sanitization + rule tagging |
| Event logging | `PlateReviewModerationEventService` | Persists status transition events |
| Review workflow | `PlateManager` | Submit/update/delete + moderation routing |
| Comment reports | `CommentReportManager` | Report creation, threshold escalation, admin review |
| Removal requests | `PlateRemovalRequestManager` | Request creation, admin review |
| Admin moderation | `ModerationAdminManager` | Approve/reject/remove comment, hide/restore plate |
| Compliance retention | `ComplianceRetentionScheduler` | Deletes old resolved records |
| Search retention | `PlateSearchEventRetentionScheduler` | Deletes old search events |

## Plate Review Moderation

**User transitions:** add → `PENDING_REVIEW`, update → `PENDING_REVIEW`, delete → `REMOVED_BY_USER`, resubmit rejected → `PENDING_REVIEW`. Each logs moderation event.

**Admin transitions:** approve → `APPROVED`, reject → `REJECTED`, remove → `REMOVED_BY_MODERATOR`. Each logs event + refreshes plate aggregates.

## ContentModerationService

`moderate(...)`: trims, normalizes spaces. Rejects empty/symbol-only/>250 chars. Flags for review: profanity, threats, heavy accusations, personal data patterns (phone/TC id/address), excessive repeated chars. Returns `ContentModerationResult { allowed, requiresReview, reasons, sanitizedText }`.

## Comment Report Lifecycle

1. Resolve reason, validate reporter + target review, block duplicate `(commentId, reporterUserId)`
2. Save with `OPEN`, increment review `reportCount`
3. If `reportCount >= threshold` (default 3) and review `APPROVED` → auto-pending + log `AUTO_PENDING_BY_REPORT_THRESHOLD` + refresh stats
4. Admin review: accepted → remove comment by moderator + log + refresh stats

## Plate Removal Request Lifecycle

1. Resolve reason, validate plate + requester, save with `OPEN`
2. If `moderation.hide-plate-on-removal-request=true` and plate `ACTIVE` → auto-hide
3. Admin review: accepted → force hide; rejected → restore only if current hide was from auto-hide

## Retention Jobs

**ComplianceRetentionScheduler:** cron `${platemate.compliance-retention.cron:0 45 3 * * *}` (Europe/Istanbul). Cutoff: `now - max(30, moderation-retention-days)`. Deletes resolved `CommentReport` and accepted/rejected `PlateRemovalRequest`.

**PlateSearchEventRetentionScheduler:** cron `0 30 3 * * *` (Europe/Istanbul). Deletes events older than 180 days.

## Lookup Types

| Domain | Helper | Lookup entity |
| --- | --- | --- |
| Review status | `PlateReviewStatus` | `PlateReviewStatusLookup` |
| Moderation action | `PlateReviewModerationActionType` | `PlateReviewModerationActionTypeLookup` |
| Report reason/status | `CommentReportReason`/`Status` | corresponding lookups |
| Removal reason/status | `PlateRemovalRequestReason`/`Status` | corresponding lookups |
| Plate status | `PlateStatus` | `PlateStatusLookup` |
