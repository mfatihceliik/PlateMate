# PlateMate Client DTO Contract (Current)

Last updated: 2026-05-18

Bu dokuman, client tarafta model/DTO olustururken birebir backend kontratina bagli kalmaniz icin hazirlandi.

## 1) Ortak response zarfi

Backend genel olarak iki zarf tipi doner:

```ts
export interface ResultResponse {
  success: boolean
  message: string
}

export interface DataResultResponse<T> extends ResultResponse {
  data: T
}
```

### Hata zarfi ornekleri

Validation hatasi (`400`):

```json
{
  "success": false,
  "message": "Dogrulama hatalari",
  "data": {
    "rating": "Puan en az 1 olmali.",
    "comment": "Yorum bos olamaz."
  }
}
```

Pagination hatasi (`400`):

```json
{
  "success": false,
  "message": "Gecersiz sayfalama parametreleri.",
  "data": "Sayfa 0 veya daha buyuk olmali."
}
```

## 2) Custom pagination modeli

```ts
export interface PaginationMeta {
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

export interface PagedData<T> {
  items: T[]
  meta: PaginationMeta
}
```

### Pagination kurallari

- `page` default: `0`
- `size` default: `20`
- `size` max: `100`
- `page < 0`, `size <= 0`, `size > 100` => `400`

## 3) Response DTO modelleri (client tarafi)

Tarih alanlari JSON'da `LocalDateTime` oldugu icin string gelir:

```ts
export type IsoDateTime = string // Ornek: "2026-05-12T22:35:19.271"
```

### User / Auth

```ts
export interface UserDto {
  id: number
  username: string
  email: string | null
  token: string | null
  refreshToken: string | null
  premiumUntil: IsoDateTime | null
  premiumActive: boolean
  roleCode: "NORMAL" | "PREMIUM" | "ADMIN" | null
  currentSubscriptionStartedAt: IsoDateTime | null
  currentSubscriptionExpiresAt: IsoDateTime | null
  currentSubscriptionPurchasedDays: number | null
  currentSubscriptionStatus: "PENDING" | "ACTIVE" | "EXPIRED" | "CANCELED" | null
}
```

### Profile / Social

```ts
export type SocialPlatform = "INSTAGRAM" | "X" | "SNAPCHAT" | "LINKEDIN" | "FACEBOOK"

export interface SocialMediaLinkDto {
  platform: SocialPlatform
  url: string
}

export interface UserProfileDto {
  id: number
  username: string
  driverRating: number
  reviewCount: number
  totalRatingSum: number
  socialMediaLinks: SocialMediaLinkDto[]
  plateReviews: PagedData<PlateReviewDto>
}
```

### Plate / PlateReview

```ts
export interface PlateDetailDto {
  id: number
  plateCode: string
  cityName: string | null
  ratingAverage: number
  reviewCount: number
  totalRatingSum: number
  todaySearchCount: number
  todayReviewCount: number
  todayReportCount: number
  todayWeightedReportScore: number
  score: number
  lastActivityAt: IsoDateTime | null
  recentReviews: PlateReviewDto[]
  recentReportTypes: PlateReportTypeDto[]
}

export interface PlateReviewDto {
  id: number
  plateCode: string
  rating: number
  comment: string
  userId: number
  username: string
  createdAt: IsoDateTime
  updatedAt: IsoDateTime
}

export type PlateReportSeverity = "RED" | "YELLOW"

export interface PlateReportTypeDto {
  code: string
  label: string
  description: string
  iconKey: string
  severity: PlateReportSeverity
  colorHex: string
  weight: number
  sortOrder: number
}

export interface PlateReportTypeAdminDto extends PlateReportTypeDto {
  id: number
  active: boolean
  createdAt: IsoDateTime
  updatedAt: IsoDateTime
}

export type DiscoveryTabType = "TREND" | "DANGEROUS" | "GOOD_DRIVER" | "NEW"
export type DiscoveryActivityActionType = "REVIEW_ADDED" | "RATING_GIVEN" | "REPORT_SUBMITTED"

export interface DiscoveryDailyStatsDto {
  todaySearchCount: number
  todayReviewCount: number
  todayReportCount: number
}

export interface DiscoveryPlateCardDto {
  plateCode: string
  ratingAverage: number
  reviewCount: number
  todaySearchCount: number
  todayReviewCount: number
  todayReportCount: number
  todayWeightedReportScore: number
  score: number
  lastActivityAt: IsoDateTime | null
}

export interface DiscoveryTabsDto {
  trendPlates: DiscoveryPlateCardDto[]
  dangerousPlates: DiscoveryPlateCardDto[]
  goodDriverPlates: DiscoveryPlateCardDto[]
  newPlates: DiscoveryPlateCardDto[]
}

export interface DiscoveryCityStatDto {
  cityId: number
  cityName: string
  todayReviewCount: number
}

export interface DiscoveryRecentActivityDto {
  username: string
  plateCode: string
  actionType: DiscoveryActivityActionType
  occurredAt: IsoDateTime
  rating: number | null
  comment: string | null
  reportTypeCode: string | null
  reportTypeLabel: string | null
}

export interface DiscoveryHomeDto {
  dailyStats: DiscoveryDailyStatsDto
  tabs: DiscoveryTabsDto
  cityStats: DiscoveryCityStatDto[]
  recentActivities: DiscoveryRecentActivityDto[]
}

export interface CityPlateActivityDto {
  plateCode: string
  todayReviewCount: number
  todayReportCount: number
  lastActivityAt: IsoDateTime | null
  ratingAverage: number
  reviewCount: number
}
```

Not: `GET /api/plates/search` artik `PlateDetailDto` doner. `recentReviews` yalnizca son 20 kaydi (`createdAt desc`) icerir; devam verisi `GET /api/plates/{plateCode}/reviews?page=1,2,...` ile alinmalidir.

### Settings / Subscription

```ts
export interface UserSettingsDto {
  messagingEnabled: boolean
  locationSharingEnabled: boolean
  messageNotificationsEnabled: boolean
  friendNotificationsEnabled: boolean
}

export interface UserSubscriptionDto {
  id: number
  purchasedDays: number
  status: "PENDING" | "ACTIVE" | "EXPIRED" | "CANCELED"
  startedAt: IsoDateTime
  expiresAt: IsoDateTime
  createdAt: IsoDateTime
  updatedAt: IsoDateTime
}
```

### Friendship

```ts
export type FriendshipStatus = "PENDING" | "ACCEPTED" | "REJECTED"

export interface FriendshipDto {
  id: number
  friendUserId: number
  friendUsername: string
  status: FriendshipStatus
  createdAt: IsoDateTime
}
```

### Chat

Not: Java tarafinda boolean alanlar `isGroup` ve `isRead` olarak tanimli ama JSON'da pratikte `group` ve `read` olarak gelir.

```ts
export interface ChatRoomDto {
  id: number
  roomName: string | null
  group: boolean
  lastMessageAt: IsoDateTime | null
  lastMessageContent: string | null
  otherParticipantName: string | null
}

export interface ChatMessageDto {
  id: number
  senderUsername: string
  messageContent: string
  sentAt: IsoDateTime
  read: boolean
}
```

### Location / City

```ts
export interface UserLocationDto {
  id: number
  userId: number
  username: string
  latitude: number
  longitude: number
  lastUpdatedAt: IsoDateTime
}

export interface CityDto {
  id: number
  name: string
}
```

## 4) Request DTO modelleri (client -> backend)

```ts
export interface RegisterRequest {
  username: string
  password: string
  email?: string
}

export interface LoginRequest {
  username?: string
  email?: string
  password: string
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface UpdateUserRequest {
  email?: string
  password?: string
}

export interface UpdateSettingsRequest {
  messagingEnabled?: boolean
  locationSharingEnabled?: boolean
  messageNotificationsEnabled?: boolean
  friendNotificationsEnabled?: boolean
}

export interface ActivateSubscriptionRequest {
  days: number // min 1, max 365
}

export interface AddPlateReviewRequest {
  rating: number // 1..5
  comment: string
  reportTypeCodes?: string[] | null // null => reports untouched, [] => clear all active reports
}

export interface UpdatePlateReviewRequest {
  rating: number // 1..5
  comment: string
  reportTypeCodes?: string[] | null // null => reports untouched, [] => clear all active reports
}

export interface SyncPlateReportsRequest {
  reportTypeCodes: string[] // required; [] => clear all active reports
}

export interface AddPlateReportTypeRequest {
  code: string
  label: string
  description: string
  iconKey: string
  severity: PlateReportSeverity
  colorHex: string // #RRGGBB or #RRGGBBAA
  weight: number // min 1
  sortOrder: number // min 1
}

export interface UpdatePlateReportTypeRequest extends AddPlateReportTypeRequest {}

export interface UpdatePlateReportTypeActiveRequest {
  active: boolean
}

export interface AddSocialLinkRequest {
  platform: SocialPlatform
  url: string
}

export interface UpdateSocialLinkRequest {
  id: number
  platform: SocialPlatform
  url: string
}

export interface SendMessageRequest {
  chatRoomId: number
  content: string
}

export interface LocationUpdateRequest {
  latitude: number
  longitude: number
}

export interface RegisterFcmTokenRequest {
  token: string
  deviceId: string
}
```

## 5) Endpoint donus tipi ozeti

- `POST /api/auth/register` -> `DataResultResponse<UserDto>`
- `POST /api/auth/login` -> `DataResultResponse<UserDto>`
- `POST /api/auth/refresh` -> `DataResultResponse<UserDto>` (`data.token` + `data.refreshToken` yenilenir)
- `POST /api/auth/logout` -> `ResultResponse`
- `GET /api/users` -> `DataResultResponse<UserDto[]>`
- `GET /api/users/{id}` -> `DataResultResponse<UserDto>`
- `GET /api/users/search` -> `DataResultResponse<UserDto>`
- `PUT /api/users/{userId}` -> `ResultResponse`
- `DELETE /api/users/{id}` -> `ResultResponse`
- `GET /api/profiles/{userId}` -> `DataResultResponse<UserProfileDto>`
- `GET /api/settings/{userId}` -> `DataResultResponse<UserSettingsDto>`
- `PUT /api/settings/{userId}` -> `ResultResponse`
- `GET /api/plates/search` -> `DataResultResponse<PlateDetailDto>`
- `GET /api/plates/{plateCode}/reviews` -> `DataResultResponse<PagedData<PlateReviewDto>>`
- `POST /api/plates/{plateCode}/reviews` -> `ResultResponse`
- `PUT /api/plates/reviews/{id}` -> `ResultResponse`
- `DELETE /api/plates/reviews/{id}` -> `ResultResponse`
- `PUT /api/plates/{plateCode}/reports` -> `ResultResponse`
- `GET /api/plate-report-types` -> `DataResultResponse<PlateReportTypeDto[]>`
- `GET /api/admin/plate-report-types` -> `DataResultResponse<PlateReportTypeAdminDto[]>`
- `POST /api/admin/plate-report-types` -> `DataResultResponse<PlateReportTypeAdminDto>`
- `PUT /api/admin/plate-report-types/{id}` -> `DataResultResponse<PlateReportTypeAdminDto>`
- `PATCH /api/admin/plate-report-types/{id}/active` -> `ResultResponse`
- `GET /api/discovery/home` -> `DataResultResponse<DiscoveryHomeDto>`
- `GET /api/discovery/tabs/{tabType}` -> `DataResultResponse<DiscoveryPlateCardDto[]>`
- `GET /api/discovery/cities/{cityId}/plates` -> `DataResultResponse<PagedData<CityPlateActivityDto>>`
- `POST /api/subscriptions/activate` -> `DataResultResponse<UserDto>`
- `GET /api/subscriptions/me` -> `DataResultResponse<UserDto>`
- `GET /api/subscriptions/me/history` -> `DataResultResponse<UserSubscriptionDto[]>`
- `POST /api/social-links` -> `ResultResponse`
- `PUT /api/social-links` -> `ResultResponse`
- `DELETE /api/social-links/{id}` -> `ResultResponse`
- `POST /api/friendships/request/{addresseeId}` -> `ResultResponse`
- `PUT /api/friendships/{id}/accept` -> `ResultResponse`
- `PUT /api/friendships/{id}/reject` -> `ResultResponse`
- `DELETE /api/friendships/{id}` -> `ResultResponse`
- `GET /api/friendships` -> `DataResultResponse<FriendshipDto[]>`
- `GET /api/friendships/pending` -> `DataResultResponse<FriendshipDto[]>`
- `GET /api/chat/rooms` -> `DataResultResponse<ChatRoomDto[]>`
- `POST /api/chat/rooms?otherUserId=...` -> `DataResultResponse<ChatRoomDto>`
- `GET /api/chat/rooms/{roomId}/messages` -> `DataResultResponse<ChatMessageDto[]>`
- `POST /api/chat/rooms/messages` -> `DataResultResponse<ChatMessageDto>` (basarili durumda)
- `PUT /api/chat/rooms/{roomId}/read` -> `ResultResponse`
- `GET /api/locations/user/{userId}` -> `DataResultResponse<UserLocationDto>`
- `GET /api/locations/visible` -> `DataResultResponse<UserLocationDto[]>`
- `POST /api/locations/block/{targetUserId}` -> `ResultResponse`
- `DELETE /api/locations/block/{targetUserId}` -> `ResultResponse`
- `GET /api/locations/blocked` -> `DataResultResponse<number[]>`
- `GET /api/cities` -> `DataResultResponse<CityDto[]>`
- `GET /api/cities/{id}` -> `DataResultResponse<CityDto>`
- `POST /api/fcm-tokens/register` -> `ResultResponse`
- `DELETE /api/fcm-tokens/unregister?token=...` -> `ResultResponse`

### Auth refresh ve 401 handling (client)

- Access token omru kisadir (15 dakika), refresh token omru uzundur (30 gun).
- Herhangi bir protected endpoint `401` donerse su akisi izlenmeli:
  1. `POST /api/auth/refresh` cagir (`RefreshTokenRequest` ile).
  2. Basariliysa yeni `token` ve `refreshToken` kaydet.
  3. Basarisizsa kullaniciyi login ekranina yonlendir.
- Refresh hata kodlari `data.code` alaninda gelir:
  - `REFRESH_EXPIRED`
  - `REFRESH_REVOKED`
  - `REFRESH_INVALID`

## 6) Kritik nested response ornekleri

### Plate review listeleme (`GET /api/plates/{plateCode}/reviews?page=0&size=20`)

```json
{
  "success": true,
  "message": "Yorumlar basariyla listelendi.",
  "data": {
    "items": [
      {
        "id": 34,
        "plateCode": "34ABC123",
        "rating": 4,
        "comment": "Yol kurallarina dikkat ediyor.",
        "userId": 15,
        "username": "ali",
        "createdAt": "2026-05-12T09:30:00",
        "updatedAt": "2026-05-12T09:30:00"
      }
    ],
    "meta": {
      "page": 0,
      "size": 20,
      "totalElements": 45,
      "totalPages": 3,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

### Profil (`GET /api/profiles/{userId}?page=0&size=20`)

```json
{
  "success": true,
  "message": "Profil bulundu.",
  "data": {
    "id": 7,
    "username": "fatih",
    "driverRating": 4.5,
    "reviewCount": 12,
    "totalRatingSum": 54,
    "socialMediaLinks": [
      {
        "platform": "INSTAGRAM",
        "url": "https://instagram.com/fatih"
      }
    ],
    "plateReviews": {
      "items": [],
      "meta": {
        "page": 0,
        "size": 20,
        "totalElements": 0,
        "totalPages": 0,
        "hasNext": false,
        "hasPrevious": false
      }
    }
  }
}
```
