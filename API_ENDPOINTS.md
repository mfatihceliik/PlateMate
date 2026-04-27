# PlateMate API Documentation

This document contains all available API endpoints for the PlateMate Backend. Use this to generate a Postman collection.

**Base URL**: `http://localhost:8080`

**Authentication**: Most endpoints require a JWT token in the `Authorization: Bearer <token>` header.

---

## 1. Authentication (`/api/auth`)

### Register
- **Method**: `POST`
- **URL**: `/api/auth/register`
- **Body** (JSON):
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
- **Body** (JSON):
  ```json
  {
    "username": "fatih",
    "password": "password123"
  }
  ```
  *(Note: You can also use "email" instead of "username" in the field)*

---

## 2. User Management (`/api/users`)

### Get All Users
- **Method**: `GET`
- **URL**: `/api/users`

### Get User By ID
- **Method**: `GET`
- **URL**: `/api/users/{id}`

### Search User By Username
- **Method**: `GET`
- **URL**: `/api/users/search?username=fatih`

### Update User (Self)
- **Method**: `PUT`
- **URL**: `/api/users`
- **Headers**: `Authorization: Bearer <token>`
- **Body** (JSON):
  ```json
  {
    "email": "newemail@example.com",
    "password": "newpassword123"
  }
  ```

### Delete User
- **Method**: `DELETE`
- **URL**: `/api/users/{id}`

---

## 3. Profile Management (`/api/profiles`)

### Get Profile
- **Method**: `GET`
- **URL**: `/api/profiles/{userId}?page=0&size=20`

### Update Profile (Self)
- **Method**: `PUT`
- **URL**: `/api/profiles`
- **Headers**: `Authorization: Bearer <token>`
- **Body** (JSON):
  ```json
  {
    "firstName": "Fatih",
    "lastName": "Celik",
    "bio": "Software Developer"
  }
  ```

---

## 4. User Reviews (`/api/reviews`)

### Add Review
- **Method**: `POST`
- **URL**: `/api/reviews`
- **Headers**: `Authorization: Bearer <token>`
- **Body** (JSON):
  ```json
  {
    "targetProfileId": 2,
    "rating": 5,
    "comment": "Great driver!"
  }
  ```

### Get Reviews for Profile
- **Method**: `GET`
- **URL**: `/api/reviews/target/{targetProfileId}?page=0&size=20`

### Update Review
- **Method**: `PUT`
- **URL**: `/api/reviews`
- **Headers**: `Authorization: Bearer <token>`
- **Body** (JSON):
  ```json
  {
    "id": 1,
    "rating": 4,
    "comment": "Actually, it was okay."
  }
  ```

### Delete Review
- **Method**: `DELETE`
- **URL**: `/api/reviews/{id}`
- **Headers**: `Authorization: Bearer <token>`

---

## 5. Vehicle Management (`/api/vehicles`)

### Get All Vehicles
- **Method**: `GET`
- **URL**: `/api/vehicles`

### Search Vehicle By Plate
- **Method**: `GET`
- **URL**: `/api/vehicles/search?plate=34ABC123`

### Get User's Vehicles
- **Method**: `GET`
- **URL**: `/api/vehicles/user/{userId}`

### Add Vehicle
- **Method**: `POST`
- **URL**: `/api/vehicles`
- **Headers**: `Authorization: Bearer <token>`
- **Body** (JSON):
  ```json
  {
    "plateCode": "34ABC123",
    "brand": "BMW",
    "model": "M3",
    "color": "Black",
    "cityId": 34
  }
  ```

### Update Vehicle
- **Method**: `PUT`
- **URL**: `/api/vehicles`
- **Headers**: `Authorization: Bearer <token>`
- **Body** (JSON):
  ```json
  {
    "id": 1,
    "plateCode": "34XYZ789",
    "brand": "Audi",
    "model": "A4",
    "color": "White",
    "cityId": 34
  }
  ```

### Delete Vehicle
- **Method**: `DELETE`
- **URL**: `/api/vehicles/{id}`
- **Headers**: `Authorization: Bearer <token>`

---

## 6. Social Media Links (`/api/social-links`)

### Add Social Link
- **Method**: `POST`
- **URL**: `/api/social-links`
- **Headers**: `Authorization: Bearer <token>`
- **Body** (JSON):
  ```json
  {
    "platform": "INSTAGRAM",
    "url": "https://instagram.com/user"
  }
  ```

### Update Social Link
- **Method**: `PUT`
- **URL**: `/api/social-links`
- **Headers**: `Authorization: Bearer <token>`
- **Body** (JSON):
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
- **Headers**: `Authorization: Bearer <token>`

---

## 7. City Data (`/api/cities`)

### Get All Cities
- **Method**: `GET`
- **URL**: `/api/cities`

---

## 8. Real-time Chat & Messages (`/api/chat`)

### Get My Chat Rooms
- **Method**: `GET`
- **URL**: `/api/chat/rooms`
- **Headers**: `Authorization: Bearer <token>`

### Get Room Messages
- **Method**: `GET`
- **URL**: `/api/chat/messages/{roomId}`
- **Headers**: `Authorization: Bearer <token>`

### Mark Messages as Read
- **Method**: `POST`
- **URL**: `/api/chat/read/{roomId}`
- **Headers**: `Authorization: Bearer <token>`

---

## 9. WebSocket Messaging (`Socket.io`)

**URL**: `ws://localhost:9092`
**Query Parameters**: `token=<JWT_TOKEN>`

### Connection
- **Library**: `socket.io-client`
- **Authentication**: JWT token must be passed in query string (e.g., `?token=...`).

### Events (Send)
- `join_room`: Joins a specific chat room.
  - **Data**: `String` (roomId)
- `send_message`: Sends a message to the room.
  - **Data** (JSON):
    ```json
    {
      "chatRoomId": 1,
      "content": "Hello via Socket.io!"
    }
    ```

### Events (Receive)
- `new_message`: Triggered when a new message arrives in a joined room.
  - **Data**: `ChatMessageDto`

---

## 10. Friendship / Follow (`/api/friends`)

### Follow User
- **Method**: `POST`
- **URL**: `/api/friends/follow/{targetUserId}`
- **Headers**: `Authorization: Bearer <token>`

### Unfollow User
- **Method**: `POST`
- **URL**: `/api/friends/unfollow/{targetUserId}`
- **Headers**: `Authorization: Bearer <token>`

### Get Followers
- **Method**: `GET`
- **URL**: `/api/friends/followers/{userId}`

### Get Following
- **Method**: `GET`
- **URL**: `/api/friends/following/{userId}`

### Get Follow Status
- **Method**: `GET`
- **URL**: `/api/friends/status/{targetUserId}`
- **Headers**: `Authorization: Bearer <token>`
