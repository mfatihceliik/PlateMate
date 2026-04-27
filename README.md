# PlateMate Backend

PlateMate is a social platform for drivers and vehicle owners. This backend API provides features for user authentication, profile management, vehicle registration, user reviews, social media integration, and real-time messaging.

## Features

- **Multi-Field Authentication**: Login using either `username` or `email`.
- **User Profiles**: Manage personal information, bios, and social media links.
- **Vehicle Management**: Register and manage vehicles by plate numbers.
- **Review System**: Rate and review other users' driving profiles with built-in self-review prevention and pagination.
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
- `/api/reviews`: User evaluations and ratings
- `/api/vehicles`: Vehicle registrations
- `/api/social-links`: Social media integration
- `/api/cities`: City data management

## License

This project is licensed under the MIT License.
