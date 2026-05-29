---
type: backend-doc
area: tasks
updated: 2026-05-29
---

# Refactor Tasks

Prioritized by severity and effort. Each task is self-contained — give one task at a time to the agent. After completion, agent should mark the task as done and update [[known-violations]] and [[changelog]] as needed.

---

## Priority 1 — Security & Exception Safety (High Impact, Low Effort)

### TASK-001: Fix GlobalExceptionHandler exception leaks

**Problem:** `handleGeneralExceptions` and `handleTypeMismatchException` expose `ex.getMessage()` to clients. May leak SQL errors, class names, internal details.

**Files to change:**
- `core/exceptions/GlobalExceptionHandler.java`
- `business/utilities/constants/Messages.java`
- `src/main/resources/messages.properties`

**What to do:**
1. Replace `ex.getMessage()` in `handleGeneralExceptions` data field with `null` or a generic string.
2. Replace `ex.getMessage()` in `handleTypeMismatchException` data field with `null` or a generic string.
3. Replace hardcoded Turkish strings (`"Dogrulama hatalari"`, `"Gecersiz istek parametresi."`, `"Beklenmeyen bir hata olustu."`) with `messageService.getMessage(Messages.XXXX)`.
4. Add corresponding `Messages.*` constants and `messages.properties` entries.
5. Keep `log.error(...)` with `ex.getMessage()` for server-side logging.

**Docs to read:** [[core-utilities]], [[conventions]]
**Tests to run:** `GlobalExceptionHandler`-related tests + `gradlew test`

---

### TASK-002: Fix ChatSocketHandler exception leak

**Problem:** `handleSendMessage` catch block sends `new ErrorResult(e.getMessage())` via socket to client.

**Files to change:**
- `api/socket/concrete/ChatSocketHandler.java`
- `business/utilities/constants/Messages.java`
- `src/main/resources/messages.properties`

**What to do:**
1. Replace `new ErrorResult(e.getMessage())` with `new ErrorResult(messageService.getMessage(Messages.UNEXPECTED_ERROR))` or a simple generic error string.
2. Keep the `log.error(...)` line for server-side logging.
3. Inject `IMessageService` into `ChatSocketHandler` (add to constructor).

**Docs to read:** [[socket-chat]], [[conventions]]

---

### TASK-003: Add @Valid to FcmTokensController

**Problem:** `RegisterTokenRequest` not validated on `register` endpoint.

**Files to change:**
- `api/controllers/concrete/FcmTokensController.java`

**What to do:**
1. Add `@Valid` before `@RequestBody RegisterTokenRequest request` in the `register` method.
2. Verify `RegisterTokenRequest` has appropriate validation annotations. Add `@NotBlank` on `token` if missing.

**Docs to read:** [[controllers]], [[conventions]]

---

## Priority 2 — Architecture Violations (High Impact, Medium Effort)

### TASK-004: Move SocketEvents to neutral package

**Problem:** `SocketEvents` lives in `api.socket.utilities.constants` but is imported by `NotificationManager` in `business.concrete` — cross-layer dependency.

**Files to change:**
- Create `business/utilities/constants/SocketEvents.java` (move content)
- `api/socket/concrete/ChatSocketHandler.java` (update import)
- `api/socket/SocketModule.java` (update import)
- `business/concrete/NotificationManager.java` (update import)
- Delete `api/socket/utilities/constants/SocketEvents.java`

**What to do:**
1. Move `SocketEvents` class to `business/utilities/constants/` package.
2. Update all imports in `ChatSocketHandler`, `SocketModule`, `NotificationManager`.
3. Delete old file.

**Docs to read:** [[socket-chat]], [[architecture]], [[conventions]]

---

### TASK-005: Fix ChatSocketHandler DAO injection

**Problem:** `ChatSocketHandler` (API layer) directly injects `IParticipantDao` (data access layer).

**Files to change:**
- `business/abstracts/IParticipantService.java` (add method)
- `business/concrete/ParticipantManager.java` (implement method)
- `api/socket/concrete/ChatSocketHandler.java` (replace DAO with service)

**What to do:**
1. Add `boolean isRoomMember(Long userId, Long roomId)` to `IParticipantService`.
2. Implement in `ParticipantManager`: delegate to `participantDao.existsByUserIdAndChatRoomId(userId, roomId)`.
3. In `ChatSocketHandler`: replace `IParticipantDao` injection with `IParticipantService`. Replace `participantDao.existsByUserIdAndChatRoomId(...)` calls with `participantService.isRoomMember(...)`.

**Docs to read:** [[socket-chat]], [[services-business]]

---

### TASK-006: Extract NotificationManager socket dependency behind interface

**Problem:** `NotificationManager` directly injects `SocketIOServer` — infrastructure concern in business layer.

**Files to change:**
- Create `business/abstracts/ISocketPushService.java`
- Create `api/socket/concrete/SocketPushManager.java` (or similar)
- `business/concrete/NotificationManager.java` (replace SocketIOServer)

**What to do:**
1. Create `ISocketPushService` with method `void sendToUserRoom(Long userId, String event, Object data)`.
2. Create implementation in `api/socket/concrete/` that injects `SocketIOServer` and implements the method.
3. In `NotificationManager`: replace `SocketIOServer` with `ISocketPushService`. Replace direct socket call with `socketPushService.sendToUserRoom(userId, SocketEvents.NOTIFICATION_RECEIVED, data)`.

**Docs to read:** [[notifications-fcm]], [[socket-chat]], [[architecture]]

---

## Priority 3 — Code Consistency (Medium Impact, Low Effort)

### TASK-007: Migrate string literal message keys to Messages constants

**Problem:** 40 calls use string literal keys like `messageService.getMessage("auth.login.success")` instead of `Messages.*` constants.

**Files to change:**
- `business/utilities/constants/Messages.java` (add missing constants)
- `business/concrete/AuthManager.java` (6 literals)
- `business/concrete/ModerationAdminManager.java` (9 literals)
- `business/concrete/PlateRemovalRequestManager.java` (7 literals)
- `business/concrete/CommentReportManager.java` (6 literals)
- `business/concrete/AdminAccessManager.java` (1 literal)
- `business/concrete/ChatMessageManager.java` (3 literals)
- `business/concrete/FcmTokenManager.java` (1 literal)
- `business/concrete/FriendshipManager.java` (2 literals)
- `business/concrete/PlateReportTypeManager.java` (2 literals)
- `business/concrete/SocialMediaLinkManager.java` (2 literals)
- `business/concrete/PlateManager.java` (1 literal — `"review.delete.unauthorized"`)

**What to do:**
1. For each file, find `messageService.getMessage("some.key")` patterns.
2. Add corresponding `public static final String SOME_KEY = "some.key"` to `Messages.java`.
3. Replace string literal with `Messages.SOME_KEY`.
4. Do NOT change message.properties values — only the Java reference.

**Docs to read:** [[conventions]]

---

### TASK-008: Add missing @Transactional annotations

**Problem:** Public write methods without `@Transactional` in 4 managers.

**Files to change:**
- `business/concrete/ParticipantManager.java`
- `business/concrete/SocialMediaLinkManager.java`
- `business/concrete/UserManager.java`
- `business/concrete/UserSettingsManager.java`

**What to do:**
1. Identify public methods that call `.save()` or `.delete()` on repositories.
2. Add `@Transactional` annotation to those methods (use `jakarta.transaction.Transactional`).
3. Do NOT add to private helper methods.

**Docs to read:** [[conventions]], [[services-business]]

---

### TASK-009: Remove ChatController dead imports

**Problem:** `ChatController` imports `ChatMessage`, `ChatRoom`, `User` from `entities.concrete` but none are used.

**Files to change:**
- `api/controllers/concrete/ChatController.java`

**What to do:**
1. Remove unused imports: `ChatMessage`, `ChatRoom`, `User`.
2. Also remove the Turkish comment `// REST ile mesaj gönderme (Socket alternatifi — offline fallback)` or replace with English.

**Docs to read:** [[controllers]]

---

### TASK-010: Replace all Turkish comments with English

**Problem:** 20+ Turkish comments throughout source code.

**Files to change:**
- `config/jwt/JwtAuthenticationInterceptor.java`
- `config/I18nConfig.java`
- `dataAccess/abstracts/IParticipantDao.java`
- `business/abstracts/IChatRoomService.java`
- `business/abstracts/IChatMessageService.java`
- `business/utilities/plate/concrete/TrPlateValidator.java`
- `business/utilities/constants/Messages.java`
- `business/concrete/UserManager.java`
- `business/concrete/FcmTokenManager.java`
- `business/concrete/ChatRoomManager.java`
- `business/concrete/NotificationManager.java`
- `api/socket/concrete/ChatSocketHandler.java`

**What to do:**
1. Find all Turkish comments (`//` lines with Turkish text).
2. Replace with clear English equivalent or remove if comment adds no value.
3. Do NOT change any code logic — comments only.

**Docs to read:** [[conventions]]

---

## Priority 4 — Interface Cleanup (Medium Impact, Medium Effort)

### TASK-011: Create IFcmTokenController interface

**Problem:** `FcmTokensController` is the only controller without an interface.

**Files to change:**
- Create `api/controllers/abstracts/IFcmTokenController.java`
- `api/controllers/concrete/FcmTokensController.java` (implement interface, move route annotations)

**What to do:**
1. Create `IFcmTokenController` with `@RequestMapping("/api/fcm-tokens")` and route method signatures.
2. Move `@PostMapping`/`@DeleteMapping` to interface.
3. `FcmTokensController implements IFcmTokenController`, remove `@RequestMapping` from concrete class.
4. Follow existing pattern from any other controller (e.g. `IPlateController`/`PlateController`).

**Docs to read:** [[controllers]], [[conventions]]

---

### TASK-012: Remove IChatMessageService entity overload

**Problem:** `sendMessage(ChatMessage, Long)` accepts JPA entity; unused by controllers.

**Files to change:**
- `business/abstracts/IChatMessageService.java`
- `business/concrete/ChatMessageManager.java`

**What to do:**
1. Check if `sendMessage(ChatMessage, Long)` is called anywhere outside `ChatMessageManager` itself.
2. If only used internally or unused: remove it from the interface and make it private in manager (or remove entirely if dead code).
3. Keep `sendMessage(SendMessageRequest, Long)` as the public contract.

**Docs to read:** [[services-business]]

---

### TASK-013: Create CityDto and update ICityService

**Problem:** `ICityService` returns `City` entity directly instead of DTO.

**Files to change:**
- Create `entities/dto/CityDto.java`
- Create or update `core/utilities/mappers/CityMapper.java`
- `business/abstracts/ICityService.java`
- `business/concrete/CityManager.java`

**What to do:**
1. Create `CityDto` with fields: `id`, `name`, and any other public-facing fields.
2. Create `CityMapper` implementing `ModelMapperService<City, CityDto>`.
3. Update `ICityService` to return `DataResult<List<CityDto>>` and `DataResult<CityDto>`.
4. Update `CityManager` to use mapper.

**Docs to read:** [[entities]], [[conventions]], [[services-business]]

---

## Priority 5 — Long-Term (Low Urgency)

### TASK-014: Move IRefreshTokenService exception to separate file

**Problem:** Nested `RefreshTokenServiceException` in service interface.

**Files:** `IRefreshTokenService.java`, create `core/exceptions/RefreshTokenServiceException.java` or `business/exceptions/`.

### TASK-015: Clean entity imports from remaining service interfaces

**Files:** `INotificationService`, `IParticipantService`, `IPlateReportService` — replace entity params with id/code primitives.

### TASK-016: Consider PlateManager decomposition

**Problem:** 15 dependencies. Long-term candidate for splitting into `PlateSearchManager` + `PlateReviewManager`.

### TASK-017: Resolve phantom city endpoint exclusion

**Problem:** `WebMvcConfig` excludes `/api/cities/**` but no controller exists.
**Options:** Add `CityController` (if Android needs it) or remove the exclusion.
