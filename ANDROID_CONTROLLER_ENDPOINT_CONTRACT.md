# PlateMate Controller Request/Response Contract (Kotlin Android)

Last updated: 2026-05-27
Source of truth: `src/main/java/com/mefy/platemate/api/controllers/**`

## Lookup Geçiş Kuralı

- Persist edilen enum alanlar lookup modeline tasinmistir; response modellerinde `...Id` + `...Code` birlikte tutulmalidir.
- Write request'lerde gecis doneminde `id` veya `code` gonderilebilir; ikisi birden varsa `id` backend tarafinda authoritative kabul edilir.
- `premiumUntil` response'ta bulunmaya devam eder ancak artik `users` tablosundan degil `user_subscriptions` verisinden hesaplanir.

## 1) Base URL and Auth

- Base URL: `http://localhost:8080`
- JWT header (required endpoints):
  - `Authorization: Bearer <token>`
- JWT not required:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `GET /api/cities`
  - `GET /api/cities/{id}`
  - `GET /api/plates/search?plate=...` (JWT zorunlu)
- Missing/invalid token response:
  - HTTP `401`
  - Body: `{"success":false,"message":"Gecersiz veya eksik token."}`

## 2) Common Kotlin Models

```kotlin
typealias IsoDateTime = String

data class ApiResult(
    val success: Boolean,
    val message: String?
)

data class ApiDataResult<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)

data class PaginationMeta(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

data class PagedData<T>(
    val items: List<T>,
    val meta: PaginationMeta
)
```

Pagination rules:
- default `page=0`, `size=20`
- max `size=100`
- invalid pagination -> HTTP `400` with `ApiDataResult<String>`

## 3) Request Models (Android)

```kotlin
data class RegisterRequest(
    val username: String, // min 3, max 30, not blank
    val password: String, // min 6, not blank
    val email: String? = null // if sent, must be valid email
)

data class LoginRequest(
    val username: String? = null,
    val email: String? = null,
    val password: String // not blank
)

data class UpdateUserRequest(
    val email: String? = null, // if sent, must be valid email
    val password: String? = null // if sent, min 6
)

data class UpdateSettingsRequest(
    val messagingEnabled: Boolean? = null,
    val messageNotificationsEnabled: Boolean? = null,
    val friendNotificationsEnabled: Boolean? = null
)

data class ActivateSubscriptionRequest(
    val days: Int // 1..365
)

data class AddPlateReviewRequest(
    val rating: Int, // 1..5
    val comment: String? = null, // premium users can send free-text; non-premium should keep empty/null
    val reportTypeCodes: List<String>? = null // null => reports untouched, [] => clear all active reports
)

data class UpdatePlateReviewRequest(
    val rating: Int, // 1..5
    val comment: String? = null, // premium users can send free-text; non-premium should keep empty/null
    val reportTypeCodes: List<String>? = null // null => reports untouched, [] => clear all active reports
)

data class SyncPlateReportsRequest(
    val reportTypeCodes: List<String> // required; [] => clear all active reports
)

data class AddPlateReportTypeRequest(
    val code: String,
    val label: String,
    val description: String,
    val iconKey: String,
    val severity: PlateReportSeverity,
    val colorHex: String, // #RRGGBB or #RRGGBBAA
    val weight: Int, // min 1
    val sortOrder: Int // min 1
)

data class UpdatePlateReportTypeRequest(
    val code: String,
    val label: String,
    val description: String,
    val iconKey: String,
    val severity: PlateReportSeverity,
    val colorHex: String, // #RRGGBB or #RRGGBBAA
    val weight: Int, // min 1
    val sortOrder: Int // min 1
)

data class UpdatePlateReportTypeActiveRequest(
    val active: Boolean
)

data class AddSocialLinkRequest(
    val platformId: Long?, // either platformId or platformCode required; id wins if both given
    val platformCode: String?,
    val url: String // not blank
)

data class UpdateSocialLinkRequest(
    val id: Long,
    val platformId: Long?,
    val platformCode: String?,
    val url: String // not blank
)

data class AddSocialPlatformRequest( // admin only
    val code: String, // not blank
    val label: String, // not blank
    val iconUrl: String?,
    val backgroundColorHex: String?, // "#RRGGBB" or "#AARRGGBB" if present
    val iconTintColorHex: String?,
    val sortOrder: Int // min 1
)

data class UpdateSocialPlatformRequest( // admin only, same shape as Add
    val code: String,
    val label: String,
    val iconUrl: String?,
    val backgroundColorHex: String?,
    val iconTintColorHex: String?,
    val sortOrder: Int
)

data class UpdateSocialPlatformActiveRequest( // admin only
    val active: Boolean
)

data class SendMessageRequest(
    val chatRoomId: Long,
    val content: String // not blank
)

data class RegisterFcmTokenRequest(
    val token: String,
    val deviceId: String
)
```

## 4) Response Models (Android)

```kotlin
enum class UserRoleCode { NORMAL, PREMIUM, ADMIN }
enum class UserSubscriptionStatus { PENDING, ACTIVE, EXPIRED, CANCELED }

data class UserDto(
    val id: Long,
    val username: String,
    val email: String?,
    val token: String?,
    val premiumUntil: IsoDateTime?,
    val premiumActive: Boolean,
    val roleCode: UserRoleCode?,
    val currentSubscriptionStartedAt: IsoDateTime?,
    val currentSubscriptionExpiresAt: IsoDateTime?,
    val currentSubscriptionPurchasedDays: Int?,
    val currentSubscriptionStatus: UserSubscriptionStatus?
)

data class SocialMediaLinkDto(
    val id: Long,
    val platformId: Long?,
    val platformCode: String?,
    val url: String
)

data class SocialPlatformDto( // GET /api/social-platforms, active only
    val id: Long,
    val code: String,
    val label: String,
    val iconUrl: String?,
    val backgroundColorHex: String?,
    val iconTintColorHex: String?,
    val sortOrder: Int
)

data class SocialPlatformAdminDto( // GET/POST/PUT /api/admin/social-platforms
    val id: Long,
    val code: String,
    val label: String,
    val iconUrl: String?,
    val backgroundColorHex: String?,
    val iconTintColorHex: String?,
    val sortOrder: Int,
    val active: Boolean,
    val createdAt: IsoDateTime,
    val updatedAt: IsoDateTime
)

data class PlateDetailDto(
    val id: Long,
    val plateCode: String,
    val cityName: String?,
    val ratingAverage: Double,
    val reviewCount: Int,
    val totalRatingSum: Long,
    val todaySearchCount: Long,
    val todayReviewCount: Long,
    val todayReportCount: Long,
    val todayWeightedReportScore: Long,
    val score: Double,
    val lastActivityAt: IsoDateTime?,
    val recentReviews: List<PlateReviewDto>,
    val recentReportTypes: List<PlateReportTypeDto>
)

// Not: `recentReviews` sadece ilk 20 yorumu (`createdAt desc`) doner.
// Devami icin `GET /api/plates/{plateCode}/reviews?page=1,2,...` kullanilir.

data class PlateReviewDto(
    val id: Long,
    val plateCode: String,
    val rating: Int,
    val comment: String,
    val reviewStatusId: Long,
    val reviewStatusCode: String,
    val userId: Long,
    val username: String,
    val createdAt: IsoDateTime,
    val updatedAt: IsoDateTime
)

enum class PlateReportSeverity { RED, YELLOW }

data class PlateReportTypeDto(
    val code: String,
    val label: String,
    val description: String,
    val iconKey: String,
    val severity: PlateReportSeverity,
    val colorHex: String,
    val weight: Int,
    val sortOrder: Int
)

data class PlateReportTypeAdminDto(
    val id: Long,
    val code: String,
    val label: String,
    val description: String,
    val iconKey: String,
    val severity: PlateReportSeverity,
    val colorHex: String,
    val weight: Int,
    val sortOrder: Int,
    val active: Boolean,
    val createdAt: IsoDateTime?,
    val updatedAt: IsoDateTime?
)

enum class DiscoveryTabType { TREND, DANGEROUS, GOOD_DRIVER, NEW }
enum class DiscoveryActivityActionType { REVIEW_ADDED, RATING_GIVEN, REPORT_SUBMITTED }

data class DiscoveryDailyStatsDto(
    val todaySearchCount: Long,
    val todayReviewCount: Long,
    val todayReportCount: Long
)

data class DiscoveryPlateCardDto(
    val plateCode: String,
    val cityName: String?,
    val ratingAverage: Double,
    val reviewCount: Int,
    val todaySearchCount: Long,
    val todayReviewCount: Long,
    val todayReportCount: Long,
    val todayWeightedReportScore: Long,
    val score: Double,
    val lastActivityAt: IsoDateTime?,
    val trendPlates: List<PlateReportTypeDto>
)

data class DiscoveryTabsDto(
    val trendPlates: List<DiscoveryPlateCardDto>,
    val attentionPlates: List<DiscoveryPlateCardDto>,
    val goodDriverPlates: List<DiscoveryPlateCardDto>,
    val newPlates: List<DiscoveryPlateCardDto>
)

data class DiscoveryCityStatDto(
    val cityId: Int,
    val cityName: String,
    val todayReviewCount: Long
)

data class DiscoveryRecentActivityDto(
    val username: String,
    val plateCode: String,
    val actionType: DiscoveryActivityActionType,
    val occurredAt: IsoDateTime,
    val rating: Int?,
    val comment: String?,
    val reportTypeCode: String?,
    val reportTypeLabel: String?
)

data class DiscoveryHomeDto(
    val dailyStats: DiscoveryDailyStatsDto,
    val tabs: DiscoveryTabsDto,
    val cityStats: List<DiscoveryCityStatDto>,
    val topCityPlates: List<CityPlateActivityDto>,
    val recentActivities: List<DiscoveryRecentActivityDto>
)

data class CityPlateActivityDto(
    val plateCode: String,
    val todayReviewCount: Long,
    val todayReportCount: Long,
    val lastActivityAt: IsoDateTime?,
    val ratingAverage: Double,
    val reviewCount: Int
)

data class UserProfileDto(
    val id: Long,
    val username: String,
    val totalFriendCounts: Int,
    val averageGivenRating: Double?,
    val reviewCount: Int?,
    val joinedAt: IsoDateTime?,
    val premiumActive: Boolean,
    val userSettings: UserSettingsDto?,
    val reviewStatusCounts: UserReviewStatusCountsDto,
    val evaluationTotals: UserReviewEvaluationTotalsDto,
    val socialMediaLinks: List<SocialMediaLinkDto>,
    val plateReviews: List<PlateReviewDto>,
    val friendRequests: List<UserProfileFriendRequestDto>
)

data class UserProfileFriendRequestDto(
    val id: Long,
    val requesterUserId: Long,
    val requesterUsername: String,
    val addresseeUserId: Long,
    val addresseeUsername: String,
    val statusId: Long,
    val statusCode: String, // REQUESTED | ACCEPTED | REJECTED | REMOVED
    val createdAt: IsoDateTime,
    val respondedAt: IsoDateTime?,
    val lastActionAt: IsoDateTime
)

data class UserReviewStatusCountsDto(
    val approved: Int,
    val pendingReview: Int,
    val rejected: Int,
    val removedByUser: Int,
    val removedByModerator: Int,
    val removedByLegalRequest: Int
)

data class UserReviewEvaluationTotalsDto(
    val totalApproved: Int,
    val totalPendingReview: Int,
    val totalRejected: Int,
    val totalRemovedByUser: Int,
    val totalRemovedByModerator: Int,
    val totalRemovedByLegalRequest: Int
)

data class UserSettingsDto(
    val messagingEnabled: Boolean,
    val messageNotificationsEnabled: Boolean,
    val friendNotificationsEnabled: Boolean
)

data class UserSettingsOverviewDto(
    val email: String?,
    val premiumActive: Boolean,
    val premiumUntil: IsoDateTime?,
    val userSettings: UserSettingsDto,
    val socialMediaLinks: List<SocialMediaLinkDto>
)

// Note: Password response payload'larinda donmez.
// Password update icin PUT /api/users/{userId} kullanilir.

data class UserSubscriptionDto(
    val id: Long,
    val purchasedDays: Int,
    val status: UserSubscriptionStatus,
    val startedAt: IsoDateTime?,
    val expiresAt: IsoDateTime?,
    val createdAt: IsoDateTime?,
    val updatedAt: IsoDateTime?
)

data class FriendshipDto(
    val id: Long,
    val friendUserId: Long,
    val friendUsername: String,
    val statusId: Long,
    val statusCode: String, // REQUESTED | ACCEPTED | REJECTED | REMOVED
    val createdAt: IsoDateTime?
)

// Note: backend boolean fields are "isGroup" / "isRead" in Java.
// JSON payload commonly appears as "group" and "read".
data class ChatRoomDto(
    val id: Long,
    val roomName: String?,
    val group: Boolean,
    val lastMessageAt: IsoDateTime?,
    val lastMessageContent: String?,
    val otherParticipantName: String?
)

data class ChatMessageDto(
    val id: Long,
    val senderUsername: String,
    val messageContent: String,
    val sentAt: IsoDateTime?,
    val read: Boolean
)

data class CityDto(
    val id: Int,
    val name: String
)
```

## 5) Endpoint Matrix (Request + Response)

| Controller | Method | Path | Auth | Request | Success Response |
|---|---|---|---|---|---|
| Auth | POST | `/api/auth/register` | No | Body: `RegisterRequest` | HTTP `201`, `ApiDataResult<UserDto>` |
| Auth | POST | `/api/auth/login` | No | Body: `LoginRequest` (`username` or `email` + `password`) | HTTP `200`, `ApiDataResult<UserDto>` |
| Users | GET | `/api/users` | Yes | - | HTTP `200`, `ApiDataResult<List<UserDto>>` |
| Users | GET | `/api/users/{id}` | Yes | Path: `id: Long` | HTTP `200`, `ApiDataResult<UserDto>` |
| Users | GET | `/api/users/search?username=...` | Yes | Query: `username: String` | HTTP `200`, `ApiDataResult<UserDto>` |
| Users | PUT | `/api/users/{userId}` | Yes | Path: `userId: Long`, Body: `UpdateUserRequest` | HTTP `200`, `ApiResult` |
| Users | DELETE | `/api/users/{id}` | Yes | Path: `id: Long` | HTTP `200`, `ApiResult` |
| Profiles | GET | `/api/profiles/{userId}` | Yes | Path: `userId: Long` | HTTP `200`, `ApiDataResult<UserProfileDto>` |
| Settings | GET | `/api/settings/{userId}` | Yes | Path: `userId: Long` | HTTP `200`, `ApiDataResult<UserSettingsDto>` |
| Settings | GET | `/api/settings/{userId}/overview` | Yes | Path: `userId: Long` | HTTP `200`, `ApiDataResult<UserSettingsOverviewDto>` |
| Settings | PUT | `/api/settings/{userId}` | Yes | Path: `userId: Long`, Body: `UpdateSettingsRequest` | HTTP `200`, `ApiResult` |
| Plates | GET | `/api/plates/search?plate=...` | Yes | Query: `plate: String` | HTTP `200`, `ApiDataResult<PlateDetailDto>` |
| Plates | GET | `/api/plates/{plateCode}/reviews?page=0&size=20` | Yes | Path: `plateCode: String`, Query: `page`, `size` | HTTP `200`, `ApiDataResult<PagedData<PlateReviewDto>>` |
| Plates | POST | `/api/plates/{plateCode}/reviews` | Yes | Path: `plateCode: String`, Body: `AddPlateReviewRequest` | HTTP `201`, `ApiResult` |
| Plates | PUT | `/api/plates/reviews/{id}` | Yes | Path: `id: Long`, Body: `UpdatePlateReviewRequest` | HTTP `200`, `ApiResult` |
| Plates | DELETE | `/api/plates/reviews/{id}` | Yes | Path: `id: Long` | HTTP `200`, `ApiResult` |
| Plates | PUT | `/api/plates/{plateCode}/reports` | Yes | Path: `plateCode: String`, Body: `SyncPlateReportsRequest` | HTTP `200`, `ApiResult` |
| Plate Report Types | GET | `/api/plate-report-types` | Yes | - | HTTP `200`, `ApiDataResult<List<PlateReportTypeDto>>` |
| Admin Plate Report Types | GET | `/api/admin/plate-report-types` | Yes | - | HTTP `200`, `ApiDataResult<List<PlateReportTypeAdminDto>>` |
| Admin Plate Report Types | POST | `/api/admin/plate-report-types` | Yes | Body: `AddPlateReportTypeRequest` | HTTP `201`, `ApiDataResult<PlateReportTypeAdminDto>` |
| Admin Plate Report Types | PUT | `/api/admin/plate-report-types/{id}` | Yes | Path: `id: Long`, Body: `UpdatePlateReportTypeRequest` | HTTP `200`, `ApiDataResult<PlateReportTypeAdminDto>` |
| Admin Plate Report Types | PATCH | `/api/admin/plate-report-types/{id}/active` | Yes | Path: `id: Long`, Body: `UpdatePlateReportTypeActiveRequest` | HTTP `200`, `ApiResult` |
| Discovery | GET | `/api/discovery/home?limit=8&cityLimit=5&activityLimit=20` | Yes | Query: `limit`, `cityLimit`, `activityLimit` | HTTP `200`, `ApiDataResult<DiscoveryHomeDto>` |
| Discovery | GET | `/api/discovery/tabs/{tabType}?limit=8` | Yes | Path: `tabType: DiscoveryTabType`, Query: `limit` | HTTP `200`, `ApiDataResult<List<DiscoveryPlateCardDto>>` |
| Discovery | GET | `/api/discovery/cities/{cityId}/plates?page=0&size=20` | Yes | Path: `cityId: Int`, Query: `page`, `size` | HTTP `200`, `ApiDataResult<PagedData<CityPlateActivityDto>>` |
| Subscriptions | POST | `/api/subscriptions/activate` | Yes | Body: `ActivateSubscriptionRequest` | HTTP `200`, `ApiDataResult<UserDto>` |
| Subscriptions | GET | `/api/subscriptions/me` | Yes | - | HTTP `200`, `ApiDataResult<UserDto>` |
| Subscriptions | GET | `/api/subscriptions/me/history` | Yes | - | HTTP `200`, `ApiDataResult<List<UserSubscriptionDto>>` |
| Social Links | POST | `/api/social-links` | Yes | Body: `AddSocialLinkRequest` | HTTP `201`, `ApiResult` |
| Social Links | PUT | `/api/social-links` | Yes | Body: `UpdateSocialLinkRequest` | HTTP `200`, `ApiResult` |
| Social Links | DELETE | `/api/social-links/{id}` | Yes | Path: `id: Long` | HTTP `200`, `ApiResult` |
| Social Platforms | GET | `/api/social-platforms` | Yes | - | HTTP `200`, `ApiDataResult<List<SocialPlatformDto>>` |
| Admin Social Platforms | GET | `/api/admin/social-platforms` | Yes | - | HTTP `200`, `ApiDataResult<List<SocialPlatformAdminDto>>` |
| Admin Social Platforms | POST | `/api/admin/social-platforms` | Yes | Body: `AddSocialPlatformRequest` | HTTP `201`, `ApiDataResult<SocialPlatformAdminDto>` |
| Admin Social Platforms | PUT | `/api/admin/social-platforms/{id}` | Yes | Path: `id: Long`, Body: `UpdateSocialPlatformRequest` | HTTP `200`, `ApiDataResult<SocialPlatformAdminDto>` |
| Admin Social Platforms | PATCH | `/api/admin/social-platforms/{id}/active` | Yes | Path: `id: Long`, Body: `UpdateSocialPlatformActiveRequest` | HTTP `200`, `ApiResult` |
| Friendships | POST | `/api/friendships/request/{addresseeId}` | Yes | Path: `addresseeId: Long` | HTTP `201`, `ApiResult` |
| Friendships | PUT | `/api/friendships/{id}/accept` | Yes | Path: `id: Long` | HTTP `200`, `ApiResult` |
| Friendships | PUT | `/api/friendships/{id}/reject` | Yes | Path: `id: Long` | HTTP `200`, `ApiResult` |
| Friendships | DELETE | `/api/friendships/{id}` | Yes | Path: `id: Long` | HTTP `200`, `ApiResult` |
| Friendships | GET | `/api/friendships` | Yes | - | HTTP `200`, `ApiDataResult<List<FriendshipDto>>` |
| Friendships | GET | `/api/friendships/pending` | Yes | - | HTTP `200`, `ApiDataResult<List<FriendshipDto>>` |
| Chat | GET | `/api/chat/rooms` | Yes | - | HTTP `200`, `ApiDataResult<List<ChatRoomDto>>` |
| Chat | POST | `/api/chat/rooms?otherUserId=...` | Yes | Query: `otherUserId: Long` | HTTP `201`, `ApiDataResult<ChatRoomDto>` |
| Chat | GET | `/api/chat/rooms/{roomId}/messages` | Yes | Path: `roomId: Long` | HTTP `200`, `ApiDataResult<List<ChatMessageDto>>` |
| Chat | POST | `/api/chat/rooms/messages` | Yes | Body: `SendMessageRequest` | HTTP `201`, `ApiDataResult<ChatMessageDto>` |
| Chat | PUT | `/api/chat/rooms/{roomId}/read` | Yes | Path: `roomId: Long` | HTTP `200`, `ApiResult` |
| Cities | GET | `/api/cities` | No | - | HTTP `200`, `ApiDataResult<List<CityDto>>` |
| Cities | GET | `/api/cities/{id}` | No | Path: `id: Int` | HTTP `200`, `ApiDataResult<CityDto>` |
| FCM | POST | `/api/fcm-tokens/register` | Yes | Body: `RegisterFcmTokenRequest` | HTTP `200`, `ApiResult` |
| FCM | DELETE | `/api/fcm-tokens/unregister?token=...` | Yes | Query: `token: String` | HTTP `200`, `ApiResult` |
| Swagger Redirect | GET | `/` | No | - | HTTP `302` -> `/swagger-ui/index.html` |

## 6) Important HTTP Notes for Android Client

- A few endpoints can return HTTP `200`/`201` even when business result is failure (`success=false` in body). Always check both:
  - HTTP status
  - `success` field in response body
- Validation errors: HTTP `400`, body is usually:
  - `ApiDataResult<Map<String, String>>` (field -> message)
- Pagination errors: HTTP `400`, body is usually:
  - `ApiDataResult<String>`
- Unauthorized token: HTTP `401`, body is plain `ApiResult` style JSON (without `data`).

## 7) Retrofit Interface Skeleton

```kotlin
import retrofit2.Response
import retrofit2.http.*

interface PlateMateApi {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<ApiDataResult<UserDto>>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<ApiDataResult<UserDto>>

    @GET("api/users")
    suspend fun getUsers(): Response<ApiDataResult<List<UserDto>>>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<ApiDataResult<UserDto>>

    @GET("api/users/search")
    suspend fun getUserByUsername(@Query("username") username: String): Response<ApiDataResult<UserDto>>

    @PUT("api/users/{userId}")
    suspend fun updateUser(
        @Path("userId") userId: Long,
        @Body body: UpdateUserRequest
    ): Response<ApiResult>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<ApiResult>

    @GET("api/profiles/{userId}")
    suspend fun getProfile(
        @Path("userId") userId: Long
    ): Response<ApiDataResult<UserProfileDto>>

    @GET("api/settings/{userId}")
    suspend fun getSettings(@Path("userId") userId: Long): Response<ApiDataResult<UserSettingsDto>>

    @GET("api/settings/{userId}/overview")
    suspend fun getSettingsOverview(@Path("userId") userId: Long): Response<ApiDataResult<UserSettingsOverviewDto>>

    @PUT("api/settings/{userId}")
    suspend fun updateSettings(
        @Path("userId") userId: Long,
        @Body body: UpdateSettingsRequest
    ): Response<ApiResult>

    // Requires Authorization: Bearer <access-token>
    @GET("api/plates/search")
    suspend fun searchPlate(@Query("plate") plate: String): Response<ApiDataResult<PlateDetailDto>>

    @GET("api/plates/{plateCode}/reviews")
    suspend fun getPlateReviews(
        @Path("plateCode") plateCode: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiDataResult<PagedData<PlateReviewDto>>>

    @POST("api/plates/{plateCode}/reviews")
    suspend fun addPlateReview(
        @Path("plateCode") plateCode: String,
        @Body body: AddPlateReviewRequest
    ): Response<ApiResult>

    @PUT("api/plates/reviews/{id}")
    suspend fun updatePlateReview(
        @Path("id") id: Long,
        @Body body: UpdatePlateReviewRequest
    ): Response<ApiResult>

    @DELETE("api/plates/reviews/{id}")
    suspend fun deletePlateReview(@Path("id") id: Long): Response<ApiResult>

    @PUT("api/plates/{plateCode}/reports")
    suspend fun syncPlateReports(
        @Path("plateCode") plateCode: String,
        @Body body: SyncPlateReportsRequest
    ): Response<ApiResult>

    @GET("api/plate-report-types")
    suspend fun getPlateReportTypes(): Response<ApiDataResult<List<PlateReportTypeDto>>>

    @GET("api/admin/plate-report-types")
    suspend fun getAdminPlateReportTypes(): Response<ApiDataResult<List<PlateReportTypeAdminDto>>>

    @POST("api/admin/plate-report-types")
    suspend fun addAdminPlateReportType(
        @Body body: AddPlateReportTypeRequest
    ): Response<ApiDataResult<PlateReportTypeAdminDto>>

    @PUT("api/admin/plate-report-types/{id}")
    suspend fun updateAdminPlateReportType(
        @Path("id") id: Long,
        @Body body: UpdatePlateReportTypeRequest
    ): Response<ApiDataResult<PlateReportTypeAdminDto>>

    @PATCH("api/admin/plate-report-types/{id}/active")
    suspend fun setAdminPlateReportTypeActive(
        @Path("id") id: Long,
        @Body body: UpdatePlateReportTypeActiveRequest
    ): Response<ApiResult>

    @GET("api/discovery/home")
    suspend fun getDiscoveryHome(
        @Query("limit") limit: Int = 8,
        @Query("cityLimit") cityLimit: Int = 5,
        @Query("activityLimit") activityLimit: Int = 20
    ): Response<ApiDataResult<DiscoveryHomeDto>>

    @GET("api/discovery/tabs/{tabType}")
    suspend fun getDiscoveryTab(
        @Path("tabType") tabType: DiscoveryTabType,
        @Query("limit") limit: Int = 8
    ): Response<ApiDataResult<List<DiscoveryPlateCardDto>>>

    @GET("api/discovery/cities/{cityId}/plates")
    suspend fun getDiscoveryCityPlates(
        @Path("cityId") cityId: Int,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ApiDataResult<PagedData<CityPlateActivityDto>>>

    @POST("api/subscriptions/activate")
    suspend fun activateSubscription(@Body body: ActivateSubscriptionRequest): Response<ApiDataResult<UserDto>>

    @GET("api/subscriptions/me")
    suspend fun getCurrentSubscription(): Response<ApiDataResult<UserDto>>

    @GET("api/subscriptions/me/history")
    suspend fun getSubscriptionHistory(): Response<ApiDataResult<List<UserSubscriptionDto>>>

    @POST("api/social-links")
    suspend fun addSocialLink(@Body body: AddSocialLinkRequest): Response<ApiResult>

    @PUT("api/social-links")
    suspend fun updateSocialLink(@Body body: UpdateSocialLinkRequest): Response<ApiResult>

    @DELETE("api/social-links/{id}")
    suspend fun deleteSocialLink(@Path("id") id: Long): Response<ApiResult>

    @GET("api/social-platforms")
    suspend fun getSocialPlatforms(): Response<ApiDataResult<List<SocialPlatformDto>>>

    @GET("api/admin/social-platforms")
    suspend fun getAdminSocialPlatforms(): Response<ApiDataResult<List<SocialPlatformAdminDto>>>

    @POST("api/admin/social-platforms")
    suspend fun addSocialPlatform(@Body body: AddSocialPlatformRequest): Response<ApiDataResult<SocialPlatformAdminDto>>

    @PUT("api/admin/social-platforms/{id}")
    suspend fun updateSocialPlatform(@Path("id") id: Long, @Body body: UpdateSocialPlatformRequest): Response<ApiDataResult<SocialPlatformAdminDto>>

    @PATCH("api/admin/social-platforms/{id}/active")
    suspend fun setSocialPlatformActive(@Path("id") id: Long, @Body body: UpdateSocialPlatformActiveRequest): Response<ApiResult>

    @POST("api/friendships/request/{addresseeId}")
    suspend fun sendFriendRequest(@Path("addresseeId") addresseeId: Long): Response<ApiResult>

    @PUT("api/friendships/{id}/accept")
    suspend fun acceptFriendRequest(@Path("id") id: Long): Response<ApiResult>

    @PUT("api/friendships/{id}/reject")
    suspend fun rejectFriendRequest(@Path("id") id: Long): Response<ApiResult>

    @DELETE("api/friendships/{id}")
    suspend fun removeFriend(@Path("id") id: Long): Response<ApiResult>

    @GET("api/friendships")
    suspend fun getFriends(): Response<ApiDataResult<List<FriendshipDto>>>

    @GET("api/friendships/pending")
    suspend fun getPendingFriendRequests(): Response<ApiDataResult<List<FriendshipDto>>>

    @GET("api/chat/rooms")
    suspend fun getChatRooms(): Response<ApiDataResult<List<ChatRoomDto>>>

    @POST("api/chat/rooms")
    suspend fun getOrCreateRoom(@Query("otherUserId") otherUserId: Long): Response<ApiDataResult<ChatRoomDto>>

    @GET("api/chat/rooms/{roomId}/messages")
    suspend fun getRoomMessages(@Path("roomId") roomId: Long): Response<ApiDataResult<List<ChatMessageDto>>>

    @POST("api/chat/rooms/messages")
    suspend fun sendMessage(@Body body: SendMessageRequest): Response<ApiDataResult<ChatMessageDto>>

    @PUT("api/chat/rooms/{roomId}/read")
    suspend fun markRoomAsRead(@Path("roomId") roomId: Long): Response<ApiResult>

    @GET("api/cities")
    suspend fun getCities(): Response<ApiDataResult<List<CityDto>>>

    @GET("api/cities/{id}")
    suspend fun getCityById(@Path("id") id: Int): Response<ApiDataResult<CityDto>>

    @POST("api/fcm-tokens/register")
    suspend fun registerFcmToken(@Body body: RegisterFcmTokenRequest): Response<ApiResult>

    @DELETE("api/fcm-tokens/unregister")
    suspend fun unregisterFcmToken(@Query("token") token: String): Response<ApiResult>
}
```
