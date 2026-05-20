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
- **URL**: `/api/profiles/{userId}?page=0&size=20`
- **Not**: Profile cevabinda `username`, `driverRating`, `reviewCount`, `totalRatingSum`, `socialMediaLinks`, `plateReviews(PagedData)` doner.
- **Plate Reviews Alan Ornegi**:
```json
{
  "items": [
    {
      "id": 12,
      "plateCode": "34ABC123",
      "rating": 5,
      "comment": "Temiz kullanim.",
      "userId": 7,
      "username": "fatih",
      "createdAt": "2026-05-12T11:20:00",
      "updatedAt": "2026-05-12T11:20:00"
    }
  ],
  "meta": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

## 4) User Settings (`/api/settings`)

### Get My Settings
- **Method**: `GET`
- **URL**: `/api/settings/{userId}`
- **Not**: `userId`, token icindeki user ile ayni olmalidir.

### Update My Settings
- **Method**: `PUT`
- **URL**: `/api/settings/{userId}`
- **Body**:
```json
{
  "messagingEnabled": true,
  "locationSharingEnabled": true,
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

### Add Or Update My Review For Plate
- **Method**: `POST`
- **URL**: `/api/plates/{plateCode}/reviews`
- **Body**:
```json
{
  "rating": 5,
  "comment": "Temiz kullanim.",
  "reportTypeCodes": ["RED_LIGHT_VIOLATION", "WRONG_WAY"]
}
```
- **Not**:
  - `reportTypeCodes` opsiyoneldir.
  - `reportTypeCodes` `null` ise ihbar alanina dokunulmaz.
  - `reportTypeCodes` gonderilirse user+plate ihbar seti sync edilir (bos liste = tum aktif ihbarlari geri ceker).
  - Ayni kullanici ayni plaka icin tekrar `POST` atarsa yeni kayit yerine mevcut yorumunu gunceller.

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
  "reportTypeCodes": ["HIT_AND_RUN", "RED_LIGHT_VIOLATION"]
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
  - `tabs`: `trendPlates`, `dangerousPlates`, `goodDriverPlates`, `newPlates`
  - `cityStats`: bugunun en cok yorum alan sehirleri
  - `recentActivities`: `REVIEW_ADDED`, `RATING_GIVEN`, `REPORT_SUBMITTED`

### Discovery Single Tab
- **Method**: `GET`
- **URL**: `/api/discovery/tabs/{tabType}?limit=8`
- **Path `tabType`**: `TREND`, `DANGEROUS`, `GOOD_DRIVER`, `NEW`

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
- **Body**:
```json
{
  "platform": "INSTAGRAM",
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
  "platform": "X",
  "url": "https://x.com/user"
}
```

### Delete Social Link
- **Method**: `DELETE`
- **URL**: `/api/social-links/{id}`

---

## 11) Friendships (`/api/friendships`)

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
- **Not**: Sadece `ACCEPTED` kayitlar doner.

### Get Pending Incoming Requests
- **Method**: `GET`
- **URL**: `/api/friendships/pending`

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

## 13) User Locations (`/api/locations`)

### Get User Location
- **Method**: `GET`
- **URL**: `/api/locations/user/{userId}`

### Get Visible Locations For Me
- **Method**: `GET`
- **URL**: `/api/locations/visible`

### Block User From My Location
- **Method**: `POST`
- **URL**: `/api/locations/block/{targetUserId}`

### Unblock User From My Location
- **Method**: `DELETE`
- **URL**: `/api/locations/block/{targetUserId}`

### Get My Blocked User Id List
- **Method**: `GET`
- **URL**: `/api/locations/blocked`

---

## 14) Cities (`/api/cities`) - Public

### Get All Cities
- **Method**: `GET`
- **URL**: `/api/cities`

### Get City By Id
- **Method**: `GET`
- **URL**: `/api/cities/{id}`

---

## 15) FCM Tokens (`/api/fcm-tokens`)

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

## 16) WebSocket (Socket.io)

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
