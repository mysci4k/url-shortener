# URL Shortener - REST API

A clean and efficient REST API for shortening URLs, built with Spring Boot.  
It supports optional expiration, click tracking, soft deletion, scheduled cleanup, and distributed rate limiting powered by Redis + Bucket4j.

## ✨ Features

<details>
  <summary>Implemented</summary>

  #### Core
  - [x] Shorten long URLs into compact Base62 codes
  - [x] Redirect from short code to original URL (`302 Found`)
  - [x] Optional link expiration (1–365 days)
  - [x] Click count tracking
  - [x] Soft delete of short URLs
  - [x] Scheduled cleanup of expired and soft-deleted links

  #### Security & Reliability
  - [x] Input validation (HTTP/HTTPS only)
  - [x] Distributed rate limiting (Bucket4j + Redis)
  - [x] Global exception handling with consistent error responses
  - [x] `Retry-After` header on rate limit exceeded

  #### Developer Experience
  - [x] OpenAPI / Swagger UI documentation
  - [x] Flyway database migrations
  - [x] Actuator health endpoint
  - [x] Testcontainers support for integration tests
  - [x] Docker & Docker Compose support
</details>

<details>
  <summary>Improvements</summary>

  - [ ] Authentication / API keys
  - [ ] Caching layer for hot redirects
  - [ ] QR code generation
  - [ ] More comprehensive test coverage
</details>

## 🏗️ Architecture

The project follows a classic layered Spring Boot architecture with clear separation of concerns.

<details>
  <summary>Project Structure</summary>

  ```
  url-shortener/
  ├── src/main/java/com/mysci4k/shortener/
  │   ├── config/                 # Configuration (Rate limiting, Redis)
  │   ├── controller/             # REST controllers
  │   │   ├── UrlController.java
  │   │   └── RedirectController.java
  │   ├── dto/                    # Request / Response records
  │   ├── entity/                 # JPA entities
  │   ├── exception/              # Custom exceptions + GlobalExceptionHandler
  │   ├── ratelimit/              # @RateLimited annotation + AOP aspect
  │   ├── repository/             # Spring Data JPA repositories
  │   ├── service/                # Business logic
  │   │   ├── UrlShortenerService.java
  │   │   ├── Base62Encoder.java
  │   │   └── UrlCleanupScheduler.java
  │   └── UrlShortenerApplication.java
  │
  ├── src/main/resources/
  │   ├── application.yml
  │   ├── application-local.yml
  │   └── db/migration/           # Flyway migrations
  │
  ├── Dockerfile                  # Multi-stage Docker build
  ├── docker-compose.yml          # Full stack (app + PostgreSQL + Redis)
  ├── .dockerignore
  ├── pom.xml
  ├── LICENSE
  └── README.md
  ```
</details>

<details>
  <summary>Key Design Decisions</summary>

  - **Base62 encoding with scrambling** – sequential IDs are mixed before encoding so short codes are non-sequential and harder to guess.
  - **Soft delete** – links are marked with `deleted_at` instead of being hard-deleted immediately.
  - **Scheduled cleanup** – a cron job runs daily and permanently removes soft-deleted and expired URLs older than 30 days.
  - **Distributed rate limiting** – Bucket4j backed by Redis, keyed by client IP + endpoint.
  - **PostgreSQL sequence** – used as the source of unique IDs for short codes.
</details>

## 🚀 Getting Started

### Option A: Docker Compose (recommended)

The easiest way to run the entire stack (application + PostgreSQL + Redis) with a single command.

**Prerequisites:** Docker and Docker Compose

```bash
git clone https://github.com/mysci4k/url-shortener.git
cd url-shortener

docker-compose up --build
```

That's it. The stack will be available at:

| Service    | URL / Port              |
|------------|-------------------------|
| API        | http://localhost:8080   |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432          |
| Redis      | localhost:6379          |

Useful commands:

```bash
# Run in the background
docker-compose up --build -d

# View logs
docker-compose logs -f app

# Stop everything
docker-compose down

# Stop and remove volumes (reset database)
docker-compose down -v
```

The multi-stage `Dockerfile` builds a lean runtime image (JRE only).  
Health checks ensure the app starts only after PostgreSQL and Redis are ready.

### Option B: Local development

#### Prerequisites

- **Java** 21+
- **Maven** 3.9+ (or use the included Maven Wrapper)
- **PostgreSQL** 16+
- **Redis** 7+

#### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/mysci4k/url-shortener.git
   cd url-shortener
   ```

2. **Configure the application**

   The default active profile is `local`.  
   Edit `src/main/resources/application-local.yml` if needed:

   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/shortener
       username: postgres
       password: postgres
     data:
       redis:
         host: localhost
         port: 6379
   ```

   Main configuration (`application.yml`):

   | Property        | Description                  | Default                 |
   |-----------------|------------------------------|-------------------------|
   | `server.port`   | Server port                  | `8080`                  |
   | `app.base-url`  | Base URL used in responses   | `http://localhost:8080` |

3. **Start PostgreSQL and Redis**

   Example with Docker (only infrastructure):
   ```bash
   docker run -d --name shortener-postgres \
     -e POSTGRES_DB=shortener \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 postgres:16

   docker run -d --name shortener-redis \
     -p 6379:6379 redis:7
   ```

4. **Run the application**

   ```bash
   ./mvnw spring-boot:run
   ```

   The API will be available at `http://localhost:8080`.

   Interactive API documentation (Swagger UI):  
   `http://localhost:8080/swagger-ui.html`

## 📡 API Overview

### Shorten a URL

```http
POST /api/v1/urls
Content-Type: application/json

{
  "url": "https://example.com/very/long/path",
  "expiredInDays": 30          // optional, 1–365
}
```

**Response `201 Created`**
```json
{
  "shortUrl": "http://localhost:8080/aB3xY9",
  "originalUrl": "https://example.com/very/long/path",
  "shortCode": "aB3xY9",
  "expiresAt": "2026-10-25T18:00:00"
}
```

### Redirect

```http
GET /{shortCode}
```

Returns `302 Found` with `Location` header pointing to the original URL.  
Increments the click counter.

### Get statistics

```http
GET /api/v1/urls/{shortCode}/stats
```

**Response `200 OK`**
```json
{
  "shortCode": "aB3xY9",
  "originalUrl": "https://example.com/very/long/path",
  "clickCount": 42,
  "expiresAt": "2026-10-25T18:00:00",
  "createdAt": "2026-09-25T18:00:00"
}
```

### Soft delete

```http
DELETE /api/v1/urls/{shortCode}
```

Returns `204 No Content`.

### Error responses

All errors follow a consistent format:

```json
{
  "timestamp": "2026-09-25T18:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "URL with short code 'xyz' not found"
}
```

When rate limited, the response includes a `Retry-After` header.

## 🛠️ Tech Stack

| Technology              | Purpose                          |
|-------------------------|----------------------------------|
| Java 21                 | Language                         |
| Spring Boot 4.x         | Framework                        |
| Spring Data JPA         | Persistence                      |
| PostgreSQL              | Primary database                 |
| Flyway                  | Database migrations              |
| Redis + Bucket4j        | Distributed rate limiting        |
| Spring Validation       | Request validation               |
| springdoc-openapi       | API documentation                |
| Lombok                  | Boilerplate reduction            |
| Testcontainers          | Integration testing              |
| Docker / Docker Compose | Containerization                 |

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
