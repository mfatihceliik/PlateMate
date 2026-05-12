# PlateMate Backend

PlateMate is a social platform for drivers and vehicle owners. This backend API provides features for user authentication, profile management, plate-centered lookup, plate reviews, social media integration, and real-time messaging.

## Features

- **Multi-Field Authentication**: Login using either `username` or `email`.
- **User Profiles**: View username-based profile summary, plate review stats, and social media links.
- **Vehicle Catalog**: Search Turkish plates, fetch seeded brand/color catalogs, and collect community-confirmed vehicle details.
- **Review System**: Rate and review license plates with pagination.
- **Subscription Plate Claims**: Let active subscribers make an in-app ownership claim for a plate without presenting it as official ownership verification.
- **Subscription History Tracking**: Store subscription activation time, duration, expiry, and status history for audit and future panel use.
- **Ownership Confirmation Requests**: If a subscribed user cannot claim a plate because another subscribed user already claimed it, they can submit a photo-backed ownership confirmation request for manual review.
- **Social Media Integration**: Add, update, and delete social media links (Instagram, X, etc.) with platform-uniqueness constraints.
- **Real-time Chat**: Messaging system with WebSocket support.
- **Internationalization (i18n)**: Fully localized error and success messages (Turkish and English supported).
- **Security**: JWT-based authentication and secure password hashing.

## Tech Stack

- **Framework**: Spring Boot 4
- **Language**: Java 21
- **Security**: Spring Security & JWT
- **Database**: PostgreSQL (or H2 for local development)
- **Mapping**: Manual Mappers & Lombok
- **Validation**: Jakarta Validation (Hibernate Validator)
- **Communication**: WebSocket (STOMP)

## Getting Started

### Prerequisites

- JDK 21 
- Maven 3.6+
- PostgreSQL (optional, defaults to H2 if configured)

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```

2. Configure the database in `src/main/resources/application.properties`.

3. Build and run the application:
   ```bash
   mvn spring-boot:run
   ```

## API Documentation

The API endpoints are organized as follows:

- `/api/auth`: Registration and Login
- `/api/profiles`: Profile management
- `/api/vehicle-brands`: Vehicle brand catalog
- `/api/vehicle-colors`: Vehicle color catalog
- `/api/plates`: Plate lookup and plate review operations
- `/api/subscriptions`: Subscription activation and status
- `/api/social-links`: Social media integration
- `/api/cities`: City data management

## License

This project is licensed under the MIT License.
