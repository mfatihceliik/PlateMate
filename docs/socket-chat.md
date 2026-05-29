---
type: backend-doc
area: socket-chat
tags: [backend, spring-boot, socketio]
updated: 2026-05-29
---

# Socket Chat

## Purpose

Documents Socket.io chat architecture, handshake auth, room-join behavior, and event flow.

## Components

| Class | Responsibility |
| --- | --- |
| `SocketIOConfig` | Server bean, host/port config, handshake JWT auth via query param `token` |
| `SocketServerRunner` | Starts server on boot (`CommandLineRunner`), stops on shutdown |
| `SocketModule` | Connect/disconnect listeners, delegates to `ISocketRegistrar` handlers |
| `ChatSocketHandler` | Handles `join_room` and `send_message` events |
| `SocketEvents` | Event name constants and `USER_ROOM_PREFIX = "user_"` (Located in `business.utilities.constants`) |

## Connection Flow

1. Handshake: read `token` from query param → `JwtTokenProvider.validateToken` → reject if invalid
2. On connect: parse `userId` from token → store in client context → join `user_{userId}` room → auto-join all chat rooms via `IParticipantService.getByUserId`
3. On disconnect: log session

## Events

| Event | Handler | Flow |
| --- | --- | --- |
| `join_room` | `ChatSocketHandler` | Validate membership via `IParticipantService` → join room |
| `send_message` | `ChatSocketHandler` | Validate membership → `IChatMessageService.sendMessage` → emit `new_message` to room (or `error` on failure) |
| `notification_received` | `NotificationManager` | Sent to `user_{userId}` room for real-time notifications |

## Service Boundary

Socket handler delegates to `ChatMessageManager` via `IChatMessageService` for persistence, authorization, recipient preference checks, and notification triggers.

## Known Issues (see [[known-violations]])


* `ChatSocketHandler.handleSendMessage` catch block sends `new ErrorResult(e.getMessage())` to client via socket — leaks internal exception details.

## Open Questions

* No socket-focused tests exist.
* Socket runs on separate port (9092) with query-token auth; MVC interceptor excludes `/ws/**`.
