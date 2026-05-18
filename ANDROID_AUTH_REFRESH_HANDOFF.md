# Android Auth Refresh Handoff (Backend Uyumlu)

Last updated: 2026-05-18  
Backend source of truth:
- `src/main/java/com/mefy/platemate/api/controllers/concrete/AuthController.java`
- `src/main/java/com/mefy/platemate/config/jwt/JwtTokenProvider.java`
- `src/main/java/com/mefy/platemate/business/concrete/RefreshTokenManager.java`

## 1) Durum Ozeti

Backend tarafinda access + refresh token akisi aktif:
- Access token: `15 dakika`
- Refresh token: `30 gun`
- Refresh rotation aktif (her refresh cagrisi yeni refresh token dondurur)
- Logout endpointi refresh token revoke eder

Android tarafi icin **kismi uyum** var. Asagidaki maddeler uygulanmadan tam uyum yok:
1. `UserDto` icine `refreshToken: String?` eklenmeli.
2. `RefreshTokenRequest(refreshToken: String)` eklenmeli.
3. Retrofit'e `POST /api/auth/refresh` ve `POST /api/auth/logout` eklenmeli.
4. 401 handling: otomatik refresh + original request retry akisi kurulmali.
5. Refresh fail kodlari (`REFRESH_EXPIRED`, `REFRESH_REVOKED`, `REFRESH_INVALID`) parse edilip logout akisi tetiklenmeli.

## 2) Guncel Backend Auth Endpointleri

JWT'siz endpointler:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`

### 2.1 Register
- `POST /api/auth/register`
- Request:
```json
{
  "username": "fatih",
  "password": "password123",
  "email": "fatih@example.com"
}
```
- Response: `201`, `ApiDataResult<UserDto>`  
  `data.token` = access token, `data.refreshToken` = refresh token

### 2.2 Login
- `POST /api/auth/login`
- Request:
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
- Response: `200`, `ApiDataResult<UserDto>`  
  `data.token` = access token, `data.refreshToken` = refresh token

### 2.3 Refresh
- `POST /api/auth/refresh`
- Request:
```json
{
  "refreshToken": "..."
}
```
- Success response: `200`, `ApiDataResult<UserDto>`
  - `data.token` = yeni access token
  - `data.refreshToken` = yeni refresh token (rotation nedeniyle eskiyi overwrite et)

- Failure response: `401`
```json
{
  "success": false,
  "message": "...",
  "data": {
    "code": "REFRESH_EXPIRED | REFRESH_REVOKED | REFRESH_INVALID"
  }
}
```

### 2.4 Logout
- `POST /api/auth/logout`
- Request:
```json
{
  "refreshToken": "..."
}
```
- Response: `200`, `ApiResult`
- Davranis idempotent: token zaten invalid/revoked olsa da `200` donebilir.

## 3) Android Model Degisiklikleri (Zorunlu)

```kotlin
data class UserDto(
    val id: Long,
    val username: String,
    val email: String?,
    val token: String?,
    val refreshToken: String?, // NEW
    val premiumUntil: String?,
    val premiumActive: Boolean,
    val roleCode: UserRoleCode?,
    val currentSubscriptionStartedAt: String?,
    val currentSubscriptionExpiresAt: String?,
    val currentSubscriptionPurchasedDays: Int?,
    val currentSubscriptionStatus: UserSubscriptionStatus?
)

data class RefreshTokenRequest(
    val refreshToken: String
)
```

## 4) Retrofit API Degisiklikleri (Zorunlu)

```kotlin
@POST("api/auth/refresh")
suspend fun refresh(@Body body: RefreshTokenRequest): Response<ApiDataResult<UserDto>>

@POST("api/auth/logout")
suspend fun logout(@Body body: RefreshTokenRequest): Response<ApiResult>
```

## 5) 401 -> Refresh -> Retry Akisi (Android)

Beklenen akis:
1. Protected endpoint `401` donerse, refresh token varsa `POST /api/auth/refresh` cagir.
2. Refresh `200` ise:
   - yeni `token` ve `refreshToken` persist et
   - original request'i yeni access token ile 1 kez retry et
3. Refresh `401` ise (`code` parse et):
   - `REFRESH_EXPIRED` / `REFRESH_REVOKED` / `REFRESH_INVALID` -> local session clear + login ekranina yonlendir
4. Refresh token yoksa direkt login ekranina yonlendir.

Onemli:
- Ayni anda birden fazla request 401 alirsa tek bir refresh call yap (single-flight / mutex).
- Infinite loop engeli icin refresh request'i kendi 401 logic'inden muaf tut.
- Retry en fazla 1 kez yap.

## 6) Mevcut Android Contract Dosyasina Gore Uyusmazliklar

`ANDROID_CONTROLLER_ENDPOINT_CONTRACT.md` dosyasinda su an:
- `UserDto` icinde `refreshToken` yok.
- `RefreshTokenRequest` tanimi yok.
- Endpoint matrix'te `/api/auth/refresh` ve `/api/auth/logout` yok.
- Retrofit skeleton'da refresh/logout fonksiyonlari yok.
- JWT'siz endpoint listesinde refresh/logout yok.

Bu maddeler guncellenirse Android tarafi backend ile uyumlu olur.

## 7) Android Codex'e Verecegin Kisa Gorev Tanimi

Asagidaki metni direkt Android tarafindaki Codex'e verebilirsin:

1. `UserDto` modeline `refreshToken: String?` ekle.  
2. `RefreshTokenRequest` modelini ekle.  
3. Retrofit API'ye `refresh()` ve `logout()` endpointlerini ekle.  
4. OkHttp Authenticator/Interceptor katmaninda `401 -> refresh -> retry once` akisini kur.  
5. Refresh fail `data.code` (`REFRESH_EXPIRED|REFRESH_REVOKED|REFRESH_INVALID`) geldiginde session'i temizleyip login'e yonlendir.  
6. Refresh basarili oldugunda hem access hem refresh tokeni atomik sekilde guncelle.  
