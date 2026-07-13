# PlateMate API Endpoints

Bu dokuman mevcut backend kodundaki endpointleri yansitir.

**Base URL**: `http://localhost:8080`

## Auth Notu

- Cogu `/api/**` endpoint JWT ister: `Authorization: Bearer <token>`
- JWT muaf endpointler:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `POST /api/auth/refresh`
  - `POST /api/auth/logout`
  - `GET /api/cities/**`
- Token omurleri:
  - Access token: `15 dakika`
  - Refresh token: `30 gun`
- Root:
  - `GET /` -> `redirect:/swagger-ui/index.html`

---

## Pagination Notu (Custom Core Yapisi)

- Pagination kullanan endpointlerde response `data.items` + `data.meta` formatindadir.
- `meta` alanlari: `page`, `size`, `totalElements`, `totalPages`, `hasNext`, `hasPrevious`
- Varsayilan: `page=0`, `size=20`
- Limit: `size` en fazla `100`
- Gecersiz pagination parametreleri (`page < 0`, `size <= 0`, `size > 100`) -> `400 Bad Request`

---

## Lookup Normalizasyonu (Two-Phase Geçiş Notu)

- Persist edilen enum alanlari lookup tablolara tasindi ve API response'larda `...Id + ...Code` birlikte doner.
- Yazma request'lerinde gecis doneminde `id` veya `code` kabul edilir; ikisi birden gelirse `id` onceliklidir.
- Asagidaki domainlerde lookup model aktiftir:
  - `user_subscriptions.status`
  - `plates.status`
  - `comment_reports.reason`, `comment_reports.status`
  - `plate_removal_requests.reason`, `plate_removal_requests.status`
  - `plate_report_types.severity`
  - `social_media_links.platform`
  - `plate_review_moderation_events.action_type`
  - `user_roles.code`
- `users.premium_until` artik source-of-truth degildir; `premiumUntil` API'de `user_subscriptions` uzerinden hesaplanir.

---

## 1) Authentication (`/api/auth`)

### Register
- **Method**: `POST`
- **URL**: `/api/auth/register`
- **Body**:
```json
{
  "username": "fatih",
  "password": "password123",
  "email": "fatih@example.com"
}
```
- **Response Notu**: `data.token` (access) ve `data.refreshToken` birlikte doner.

### Login
- **Method**: `POST`
- **URL**: `/api/auth/login`
- **Body** (username veya email ile):
```json
{
  "username": "fatih",
  "password": "password123"
}
```
veya
```json
{
  "email": "fatih@example.com",
  "password": "password123"
}
```
- **Response Notu**: `data.token` (access) ve `data.refreshToken` birlikte doner.

### Refresh Access Token
- **Method**: `POST`
- **URL**: `/api/auth/refresh`
- **Body**:
```json
{
  "refreshToken": "..."
}
```
- **Basarili durumda**: Yeni `token` (access) + yeni `refreshToken` doner (rotation).
- **Basarisiz durumda**: `401` + `data.code`
  - `REFRESH_EXPIRED`
  - `REFRESH_REVOKED`
  - `REFRESH_INVALID`

### Logout (Refresh Revoke)
- **Method**: `POST`
- **URL**: `/api/auth/logout`
- **Body**:
```json
{
  "refreshToken": "..."
}
```
- **Not**: Idempotenttir. Token aktifse revoke edilir, daha once revoke/invalid olsa da `200` donebilir.

---

## 2) Users (`/api/users`)

### Get All Users
- **Method**: `GET`
- **URL**: `/api/users`

### Get User By Id
- **Method**: `GET`
- **URL**: `/api/users/{id}`

### Search User By Username
- **Method**: `GET`
- **URL**: `/api/users/search?username=fatih`

### Update User (Self)
- **Method**: `PUT`
- **URL**: `/api/users/{userId}`
- **Body**:
```json
{
  "email": "newmail@example.com",
  "password": "newpassword123"
}
```

### Delete User
- **Method**: `DELETE`
- **URL**: `/api/users/{id}`

---

## 3) Profiles (`/api/profiles`)

### Get Profile By User Id
- **Method**: `GET`
- **URL**: `/api/profiles/{userId}`
- **Not**: Profile cevabinda `username`, `totalFriendCounts`, `averageGivenRating`, `reviewCount`, `joinedAt`, `premiumActive`, `userSettings(self-only)`, `socialMediaLinks`, `plateReviews(last 10)`, `friendRequests(last 10, self-only)`, `evaluationTotals` doner.
- **Review Status Kontrati**: Her review item icinde `reviewStatusId` (1..6) ve `reviewStatusCode` (`PENDING_REVIEW`, `APPROVED`, ...) birlikte doner.
- **Gorunurluk Kurali**:
  - `plateReviews`: `requesterUserId == userId` ise tum review statusleri doner; diger kullanicilar sadece `APPROVED` gorur.
  - `friendRequests`: sadece self profile gorunumunde doner; non-self gorunumde bos liste doner.
- **Status Counts Kurali**: Top-level `reviewStatusCounts` profile-wide toplamlari doner. Self profile icin tum statuslar doludur; baska profil goruntulemede yalnizca `approved` dolu, diger alanlar `0` doner.
- **Evaluation Totals Kurali**: `evaluationTotals` profile-wide total metrikleri (`totalApproved`, `totalPendingReview`, `totalRejected`, `totalRemovedByUser`, `totalRemovedByModerator`, `totalRemovedByLegalRequest`) self/non-self fark etmeksizin full doner.
- **Response Ornegi**:
```json
{
  "id": 7,
  "username": "fatih",
  "totalFriendCounts": 24,
  "averageGivenRating": 4.5,
  "reviewCount": 12,
  "joinedAt": "2026-01-10T10:00:00",
  "premiumActive": true,
  "userSettings": null,
  "reviewStatusCounts": {
    "approved": 8,
    "pendingReview": 1,
    "rejected": 1,
    "removedByUser": 1,
    "removedByModerator": 1,
    "removedByLegalRequest": 0
  },
  "evaluationTotals": {
    "totalApproved": 8,
    "totalPendingReview": 1,
    "totalRejected": 1,
    "totalRemovedByUser": 1,
    "totalRemovedByModerator": 1,
    "totalRemovedByLegalRequest": 0
  },
  "plateReviews": [],
  "friendRequests": []
}
```
- **User Settings Notu**: `userSettings` sadece kullanici kendi profiline baktiginda dolu doner, diger profillerde `null` doner.

---

## 4) User Settings (`/api/settings`)

### Get My Settings
- **Method**: `GET`
- **URL**: `/api/settings/{userId}`
- **Not**: `userId`, token icindeki user ile ayni olmalidir.

### Get My Settings Overview
- **Method**: `GET`
- **URL**: `/api/settings/{userId}/overview`
- **Not**:
  - `userId`, token icindeki user ile ayni olmalidir.
  - Response non-paginated olarak `email`, `premiumActive`, `premiumUntil`, `userSettings`, `socialMediaLinks` doner.
  - `socialMediaLinks` elemanlari `id`, `platform`, `url` alanlarini icerir.
  - Password alani response'a eklenmez; sifre guncelleme `PUT /api/users/{userId}` ile yapilir.

### Update My Settings
- **Method**: `PUT`
- **URL**: `/api/settings/{userId}`
- **Body**:
```json
{
  "messagingEnabled": true,
  "messageNotificationsEnabled": true,
  "friendNotificationsEnabled": true
}
```

---

## 5) Plates & Plate Reviews (`/api/plates`)

### Search Plate
- **Method**: `GET`
- **URL**: `/api/plates/search?plate=34ABC123`
- **Auth**: Evet (`Authorization: Bearer <token>` zorunlu)
- **Not**:
  - Plaka formati gecersizse `400`
  - Token eksik/gecersizse `401`
  - Gecerli plaka ise `200`
  - Kayit yoksa backend plate kaydini olusturur (upsert davranisi)
  - Search cevabi `PlateDetailDto` yapisindadir (core alanlar + today metrics + `recentReviews` + `recentReportTypes`)
  - `recentReviews` yalnizca son 20 yorumu (`createdAt desc`) doner
  - Daha fazla yorum icin `GET /api/plates/{plateCode}/reviews?page=1,2,...` kullanilir
  - `GET /api/plates/{plateCode}/detail` endpointi kaldirilmistir

### Get Plate Reviews
- **Method**: `GET`
- **URL**: `/api/plates/{plateCode}/reviews?page=0&size=20`
- **Response `data` Ornegi**:
```json
{
  "items": [
    {
      "id": 34,
      "plateCode": "34ABC123",
      "rating": 4,
      "comment": "Yol kurallarina dikkat ediyor.",
      "reviewStatusId": 2,
      "reviewStatusCode": "APPROVED",
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
```

### Add My Review For Plate
- **Method**: `POST`
- **URL**: `/api/plates/{plateCode}/reviews`
- **Body**:
```json
{
  "rating": 5,
  "comment": "Temiz kullanim. (premium icin serbest metin)",
  "reportTypeCodes": ["RED_LIGHT_VIOLATION", "WRONG_WAY"]
}
```
- **Kural**:
  - `rating` her zaman `1..5`
  - Premium olmayan kullanici serbest metin yorum gonderemez (`comment` bos/null olmalidir)
  - Premium olmayan kullanici en az bir `reportTypeCodes` etiketi secmelidir
  - Premium kullanici serbest metin yorum gonderebilir, etiket opsiyoneldir
  - Yeni/updated yorumlar her zaman `PENDING_REVIEW` olarak kaydedilir
- **Not**:
  - `reportTypeCodes` opsiyoneldir.
  - `reportTypeCodes` `null` ise ihbar alanina dokunulmaz.
  - `reportTypeCodes` gonderilirse user+plate ihbar seti sync edilir (bos liste = tum aktif ihbarlari geri ceker).
  - Ayni kullanicinin mevcut yorumu `REJECTED` ise ikinci `POST` ayni kaydi yeniden `PENDING_REVIEW`e tasir.
  - Mevcut durum `PENDING_REVIEW`, `APPROVED` veya `REMOVED_*` ise ikinci `POST` hata doner (`review.already.exists.for.plate`).

### Update Review By Review Id
- **Method**: `PUT`
- **URL**: `/api/plates/reviews/{id}`
- **Body**:
```json
{
  "rating": 4,
  "comment": "Yorum guncellendi.",
  "reportTypeCodes": ["PHONE_USAGE"]
}
```

### Delete Review By Review Id
- **Method**: `DELETE`
- **URL**: `/api/plates/reviews/{id}`

### Sync My Reports For Plate
- **Method**: `PUT`
- **URL**: `/api/plates/{plateCode}/reports`
- **Body**:
```json
{
  "reportTypeCodes": ["TRAFFIC_RULE_VIOLATION", "PHONE_USAGE"]
}
```
- **Not**:
  - `reportTypeCodes` zorunludur.
  - Bos liste gonderilirse ilgili user+plate icin tum aktif ihbarlar soft-delete (pasif) olur.
  - Ayni tip tekrar gonderilirse yeni satir acmak yerine mevcut kayit re-activate/update edilir.

---

## 6) Plate Report Types (`/api/plate-report-types`)

### Get Active Plate Report Types
- **Method**: `GET`
- **URL**: `/api/plate-report-types`
- **Not**:
  - Sadece aktif tipler doner.
  - Alanlar: `code`, `label`, `description`, `iconKey`, `severity`, `colorHex`, `weight`, `sortOrder`.

---

## 7) Admin Plate Report Types (`/api/admin/plate-report-types`)

### Get All Plate Report Types (Admin)
- **Method**: `GET`
- **URL**: `/api/admin/plate-report-types`
- **Not**: `ADMIN` rolunde kullanici gerektirir, aktif + pasif tipler birlikte doner.

### Add Plate Report Type (Admin)
- **Method**: `POST`
- **URL**: `/api/admin/plate-report-types`
- **Body**:
```json
{
  "code": "TAILGATING",
  "label": "Takip Mesafesi Ihlali",
  "description": "Araci tehlikeli seviyede yakin takip ediyor",
  "iconKey": "tailgating",
  "severity": "YELLOW",
  "colorHex": "#F9A825",
  "weight": 3,
  "sortOrder": 9
}
```

### Update Plate Report Type (Admin)
- **Method**: `PUT`
- **URL**: `/api/admin/plate-report-types/{id}`
- **Body**: `POST` ile ayni alanlar.

### Set Plate Report Type Active Status (Admin)
- **Method**: `PATCH`
- **URL**: `/api/admin/plate-report-types/{id}/active`
- **Body**:
```json
{
  "active": false
}
```

---

## 8) Discovery (`/api/discovery`)

### Discovery Home
- **Method**: `GET`
- **URL**: `/api/discovery/home?limit=8&cityLimit=5&activityLimit=20`
- **Not**:
  - Tum metrikler `Europe/Istanbul` gun penceresiyle hesaplanir.
  - `dailyStats`: `todaySearchCount`, `todayReviewCount`, `todayReportCount`
  - `tabs`: `trendPlates`, `attentionPlates`, `goodDriverPlates`, `newPlates`
  - Kart alanlari: `topReportTypes` en cok one cikan 2 report type bilgisini icerir
  - `cityStats`: bugunun en cok yorum alan sehirleri
  - `recentActivities`: `REVIEW_ADDED`, `RATING_GIVEN`, `REPORT_SUBMITTED`
  - `feedType`: `FREE` | `PREMIUM` (token kullanicisinin aktif premium durumuna gore)
  - `extendedStats`: `yesterdaySearchCount`, `yesterdayReviewCount`, `yesterdayReportCount`, `searchDeltaPercent`, `reviewDeltaPercent`, `reportDeltaPercent`, `topReportTypesToday` (`code`, `label`, `colorHex`, `iconKey`, `count` — en fazla 3)
  - `forYou` (yalnizca PREMIUM, degilse `null`): `followedPlates` + `savedPlates` (en fazla 10'ar `DiscoveryPlateCardDto`), `followedPlateActivities` (en fazla 10), `premiumStats` (`weeklySearchCount`, `weeklyReviewCount`, `weeklyReportCount` + onceki 7 gune gore `weekly*DeltaPercent`)

### Discovery Tab Feed (paged + filters)
- **Method**: `GET`
- **URL**: `/api/discovery/tabs/{tabType}/plates?page=0&size=20&cityIds=&cityIds=&reportTypeCode=&minRating=&windowDays=`
- **Path `tabType`**: `TREND`, `DANGEROUS`, `GOOD_DRIVER`, `NEW`
- **Not**:
  - Paged response doner: `PagedData<DiscoveryPlateCardDto>`.
  - Free filtreler: `cityIds` (coklu; tekrar eden query param, bos/eksik = tum sehirler), `minRating` (0-5).
  - Premium filtreler: `reportTypeCode`, `windowDays` (7|30). Free kullanici gonderirse `discovery.filter.premium.required` hatasi doner.
  - Gecersiz `tabType` -> `discovery.tab.invalid`; gecersiz filtre degerleri -> `discovery.filter.invalid`.
  - Filtreleme/sayfalama in-memory yapilir (aday tavani 500).

### Discovery City Plates
- **Method**: `GET`
- **URL**: `/api/discovery/cities/{cityId}/plates?page=0&size=20`
- **Not**:
  - Paged response doner.
  - Satir alanlari: `plateCode`, `todayReviewCount`, `todayReportCount`, `lastActivityAt`, `ratingAverage`, `reviewCount`.

---

## 9) Subscriptions (`/api/subscriptions`)

### Activate Subscription
- **Method**: `POST`
- **URL**: `/api/subscriptions/activate`
- **Body**:
```json
{
  "days": 30
}
```
- **Not**:
  - `user_subscriptions` tablosuna gecmis kaydi acilir
  - `users.premiumUntil` senkron tutulur
  - `user_roles` `NORMAL/PREMIUM` durumu otomatik senkronlanir

### Get Current Subscription
- **Method**: `GET`
- **URL**: `/api/subscriptions/me`

### Get Subscription History
- **Method**: `GET`
- **URL**: `/api/subscriptions/me/history`

---

## 10) Social Links (`/api/social-links`)

### Add Social Link
- **Method**: `POST`
- **URL**: `/api/social-links`
- **Body** (either `platformId` or `platformCode`; `platformId` wins if both given):
```json
{
  "platformCode": "INSTAGRAM",
  "url": "https://instagram.com/user"
}
```

### Update Social Link
- **Method**: `PUT`
- **URL**: `/api/social-links`
- **Body**:
```json
{
  "id": 1,
  "platformCode": "X",
  "url": "https://x.com/user"
}
```

### Delete Social Link
- **Method**: `DELETE`
- **URL**: `/api/social-links/{id}`

---

## 11) Social Platforms (`/api/social-platforms`, `/api/admin/social-platforms`)

Platform catalog backing the Social Links picker — admin-manageable, no code deploy needed to add a platform. See `social_platforms` in `docs/database.md`.

### List Active Platforms
- **Method**: `GET`
- **URL**: `/api/social-platforms`
- **Response**: `DataResult<List<SocialPlatformDto>>`, sorted by `sortOrder`

### Admin: List All Platforms (incl. inactive)
- **Method**: `GET`
- **URL**: `/api/admin/social-platforms`
- **Response**: `DataResult<List<SocialPlatformAdminDto>>`

### Admin: Add Platform
- **Method**: `POST`
- **URL**: `/api/admin/social-platforms`
- **Body**:
```json
{
  "code": "TIKTOK",
  "label": "TikTok",
  "iconUrl": "https://cdn.example.com/icons/tiktok.png",
  "backgroundColorHex": "#F1F5F9",
  "iconTintColorHex": "#0F172A",
  "sortOrder": 6
}
```

### Admin: Update Platform
- **Method**: `PUT`
- **URL**: `/api/admin/social-platforms/{id}`
- **Body**: same shape as Add

### Admin: Toggle Platform Active
- **Method**: `PATCH`
- **URL**: `/api/admin/social-platforms/{id}/active`
- **Body**:
```json
{
  "active": false
}
```

---

## 11b) Premium Catalog (`/api/premium`, `/api/admin/premium`)

Backend-driven pricing + feature bullets for the client's Premium screen — admin-manageable, no code deploy. See `premium_plans` / `premium_features` in `docs/database.md`. Feature text is bilingual (`*Tr` / `*En`); the client picks by device locale.

### Public: Get Catalog
- **Method**: `GET`
- **URL**: `/api/premium`
- **Response**: `DataResult<PremiumCatalogDto>` — `{ plans: [PremiumPlanDto], features: [PremiumFeatureDto] }` (active only, sorted by `sortOrder`). `PremiumPlanDto` = `{ id, period (MONTHLY|YEARLY), amount, currency, discountPercent, sortOrder }`; `PremiumFeatureDto` = `{ id, iconKey, titleTr, titleEn, subtitleTr, subtitleEn, sortOrder }`.

### Admin: List Plans / Update Plan / Toggle Plan Active (edit-only)
- `GET /api/admin/premium/plans` → `DataResult<List<PremiumPlanAdminDto>>`
- `PUT /api/admin/premium/plans/{id}` — Body `{ "amount": 399.00, "currency": "TRY", "discountPercent": 32, "sortOrder": 2 }`
- `PATCH /api/admin/premium/plans/{id}/active` — Body `{ "active": false }`

### Admin: Features CRUD
- `GET /api/admin/premium/features` → `DataResult<List<PremiumFeatureAdminDto>>`
- `POST /api/admin/premium/features` — Body `{ "iconKey": "star", "titleTr": "...", "titleEn": "...", "subtitleTr": null, "subtitleEn": null, "sortOrder": 7 }`
- `PUT /api/admin/premium/features/{id}` — same shape as Add
- `PATCH /api/admin/premium/features/{id}/active` — Body `{ "active": false }`

All `/api/admin/premium/**` endpoints require an `ADMIN` role (in-code `AdminAccessManager.checkAdmin`, `403` otherwise).

---

## 11c) Theme Catalog (`/api/theme`, `/api/admin/theme`) + Appearance

Backend-driven accent-color palette + grid size for the client's Theme Color screen (admin-manageable),
plus per-user appearance written through from the client. See `accent_colors` / `theme_config` /
`user_settings.theme_mode,accent_hex` in `docs/database.md`.

### Public: Get Catalog
- **Method**: `GET`
- **URL**: `/api/theme/catalog`
- **Response**: `DataResult<ThemeCatalogDto>` — `{ gridSize: int, colors: [AccentColorDto{ id, hex "#RRGGBB", sortOrder }] }` (active only, sorted).

### User: Write-through Appearance
- **Method**: `PUT`
- **URL**: `/api/settings/{userId}/appearance` (owner only — `userId` must equal the token user)
- **Body**: `{ "themeMode": "SYSTEM|LIGHT|DARK", "accentHex": "#06B6D4" | null }`
- **Response**: `Result`

### Admin: Colors CRUD + Grid Size (`ADMIN` role, else `403`)
- `GET /api/admin/theme/colors` → `DataResult<List<AccentColorAdminDto>>`
- `POST /api/admin/theme/colors` — Body `{ "hex": "#123456", "sortOrder": 9 }`
- `PUT /api/admin/theme/colors/{id}` — same shape as Add
- `PATCH /api/admin/theme/colors/{id}/active` — Body `{ "active": false }`
- `PUT /api/admin/theme/grid-size` — Body `{ "gridSize": 4 }` (1–8)

---

## 11) Friendships (`/api/friendships`)

- **Status Lookup (hard-cut)**:
  - `1=REQUESTED`
  - `2=ACCEPTED`
  - `3=REJECTED`
  - `4=REMOVED`
- **DTO Notu**: `FriendshipDto` artik `status` enum yerine `statusId` + `statusCode` doner.
- **History Notu**: Arkadaslik kayitlari soft-remove edilir (`statusId=REMOVED`) ve gecmis korunur.
- **Aktif Pair Kurali**: Ayni iki kullanici icin ayni anda yalnizca bir aktif kayit (`REQUESTED`/`ACCEPTED`) bulunabilir.

### Send Friendship Request
- **Method**: `POST`
- **URL**: `/api/friendships/request/{addresseeId}`

### Accept Request
- **Method**: `PUT`
- **URL**: `/api/friendships/{id}/accept`

### Reject Request
- **Method**: `PUT`
- **URL**: `/api/friendships/{id}/reject`

### Remove Friendship
- **Method**: `DELETE`
- **URL**: `/api/friendships/{id}`

### Get Friends
- **Method**: `GET`
- **URL**: `/api/friendships`
- **Not**: Sadece `ACCEPTED` (`statusId=2`) kayitlar doner.

### Get Pending Incoming Requests
- **Method**: `GET`
- **URL**: `/api/friendships/pending`
- **Not**: Sadece `REQUESTED` (`statusId=1`) gelen istekler doner.

---

## 12) Chat (`/api/chat`)

### Get My Rooms
- **Method**: `GET`
- **URL**: `/api/chat/rooms`

### Get Or Create Private Room
- **Method**: `POST`
- **URL**: `/api/chat/rooms?otherUserId=2`

### Get Room Messages
- **Method**: `GET`
- **URL**: `/api/chat/rooms/{roomId}/messages`

### Send Message (REST Fallback)
- **Method**: `POST`
- **URL**: `/api/chat/rooms/messages`
- **Body**:
```json
{
  "chatRoomId": 1,
  "content": "Merhaba"
}
```

### Mark Room Messages As Read
- **Method**: `PUT`
- **URL**: `/api/chat/rooms/{roomId}/read`

---

## 13) Cities (`/api/cities`) - Public

### Get All Cities
- **Method**: `GET`
- **URL**: `/api/cities`

### Get City By Id
- **Method**: `GET`
- **URL**: `/api/cities/{id}`

---

## 14) FCM Tokens (`/api/fcm-tokens`)

### Register FCM Token
- **Method**: `POST`
- **URL**: `/api/fcm-tokens/register`
- **Body**:
```json
{
  "token": "fcm_device_token",
  "deviceId": "android-123"
}
```

### Unregister FCM Token
- **Method**: `DELETE`
- **URL**: `/api/fcm-tokens/unregister?token=fcm_device_token`

---

## 15) WebSocket (Socket.io)

- **URL**: `ws://localhost:9092`
- **Query**: `token=<JWT>`
- **Send Event**: `send_message`
```json
{
  "chatRoomId": 1,
  "content": "Hello via socket"
}
```
- **Receive Event**: `new_message`
