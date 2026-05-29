---
type: backend-doc
area: notifications-fcm
tags: [backend, spring-boot, firebase]
updated: 2026-05-29
---

# Notifications FCM

## Purpose

Documents device token management and push notification flow.

## Components

| Class | Role |
| --- | --- |
| `FcmTokensController` | REST register/unregister |
| `FcmTokenManager` (`IFcmTokenService`) | Token persistence, reassignment |
| `FcmManager` (`IFcmService`) | Firebase multicast push |
| `NotificationManager` (`INotificationService`) | Preference gate + socket + FCM |
| `FirebaseConfig` | Firebase Admin SDK init from `serviceAccountKey.json` |
| `UserFcmToken` | Entity: id, user (ManyToOne), token (unique), deviceId, timestamps |

## Token Register (`POST /api/fcm-tokens/register`)

Uses `userId` from request attribute. If token exists: reassign to current user if needed, update deviceId/timestamp. If new: create `UserFcmToken`. Returns 200/400.

## Token Unregister (`DELETE /api/fcm-tokens/unregister?token=`)

Uses `userId` from request attribute. Checks ownership via `findByTokenAndUserId`. Non-owner → 400 with `auth.unauthorized`. Owner → delete token.

## Notification Flow (`NotificationManager.sendNotification`)

1. **Settings gate:** check `UserSettings` — skip if message/friend notifications disabled for type
2. **Socket push:** emit `notification_received` to `user_{userId}` room with `SuccessDataResult<NotificationSignalDto>`
3. **FCM push:** load all user tokens → `FcmManager` builds Firebase `MulticastMessage` with data keys (`type`, `timestamp`) → `sendEachForMulticast`

## Triggers

* **Chat message:** `ChatMessageManager.sendMessage` → `NotificationType.MESSAGE` to recipient (checks `messagingEnabled` first)
* **Friend request:** `FriendshipManager.sendRequest` → `NotificationType.FRIEND_REQUEST` to addressee

## Known Issues (see [[known-violations]])

* No scheduled cleanup of stale FCM tokens visible.
* No dedicated unit tests for `FcmTokenManager`, `FcmManager`, or `NotificationManager`.
