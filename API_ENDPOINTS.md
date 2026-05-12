# PlateMate API Endpoints

Bu dokuman mevcut backend kodundaki endpointleri yansitir.

**Base URL**: `http://localhost:8080`

## Auth Notu

- Cogu `/api/**` endpoint JWT ister: `Authorization: Bearer <token>`
- JWT muaf endpointler:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
  - `GET /api/cities/**`
  - `GET /api/plates/search`
  - `GET /api/plates/search/**`
- Root:
  - `GET /` -> `redirect:/swagger-ui/index.html`

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
- **Not**: Profile cevabinda `username`, `driverRating`, `reviewCount`, `totalRatingSum`, `socialMediaLinks`, `plateReviews(Page)` doner.

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
- **Public**: Evet (JWT istemez)
- **Not**:
  - Plaka formati gecersizse `400`
  - Gecerli plaka ise `200`
  - Kayit yoksa backend plate kaydini olusturur (upsert davranisi)
  - Yorumsuz plakada aggregate alanlar `0` doner

### Get Plate Reviews
- **Method**: `GET`
- **URL**: `/api/plates/{plateCode}/reviews?page=0&size=20`

### Add Or Update My Review For Plate
- **Method**: `POST`
- **URL**: `/api/plates/{plateCode}/reviews`
- **Body**:
```json
{
  "rating": 5,
  "comment": "Temiz kullanim."
}
```
- **Not**: Ayni kullanici ayni plaka icin tekrar `POST` atarsa yeni kayit yerine mevcut yorumunu gunceller.

### Update Review By Review Id
- **Method**: `PUT`
- **URL**: `/api/plates/reviews/{id}`
- **Body**:
```json
{
  "rating": 4,
  "comment": "Yorum guncellendi."
}
```

### Delete Review By Review Id
- **Method**: `DELETE`
- **URL**: `/api/plates/reviews/{id}`

---

## 6) Subscriptions (`/api/subscriptions`)

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

## 7) Social Links (`/api/social-links`)

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

## 8) Friendships (`/api/friendships`)

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

## 9) Chat (`/api/chat`)

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

## 10) User Locations (`/api/locations`)

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

## 11) Reports (`/api/reports`)

### Add Report
- **Method**: `POST`
- **URL**: `/api/reports/add`
- **Body**:
```json
{
  "reporter": { "id": 1 },
  "reportedUser": { "id": 2 },
  "reason": "Spam / hakaret"
}
```

### Mark Report As Reviewed
- **Method**: `PUT`
- **URL**: `/api/reports/{reportId}/review`

### Mark Report As Resolved
- **Method**: `PUT`
- **URL**: `/api/reports/{reportId}/resolve`

### Get Reports For Reported User
- **Method**: `GET`
- **URL**: `/api/reports/reportedUser/{reportedUserId}`

### Get All Pending Reports
- **Method**: `GET`
- **URL**: `/api/reports/pending`

---

## 12) Cities (`/api/cities`) - Public

### Get All Cities
- **Method**: `GET`
- **URL**: `/api/cities`

### Get City By Id
- **Method**: `GET`
- **URL**: `/api/cities/{id}`

---

## 13) FCM Tokens (`/api/fcm-tokens`)

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

## 14) WebSocket (Socket.io)

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

