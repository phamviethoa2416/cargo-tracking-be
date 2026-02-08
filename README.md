# Cargo Tracking Backend

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://www.postgresql.org)

A production-ready **IoT-enabled cargo tracking system** built with Kotlin and Spring Boot. This backend service manages the complete logistics workflow from order creation to shipment delivery, with real-time device tracking capabilities.

## 🔗 Related Services

This backend works together with the **Ingestion Service** for IoT device data processing:

| Service               | Repository                                                                              | Description                                             |
|-----------------------|-----------------------------------------------------------------------------------------|---------------------------------------------------------|
| **Backend API**       | This repo                                                                               | Core business logic, REST API, user management          |
| **Ingestion Service** | [Cargo Tracking Ingestion](https://github.com/phamviethoa2416/cargo-tracking-ingestion) | IoT data ingestion, telemetry processing, device events |

The two services communicate via **RabbitMQ** for asynchronous device event handling.

## 📋 Table of Contents

- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [Database Migrations](#-database-migrations)
- [API Documentation](#-api-documentation)
- [Project Structure](#-project-structure)
- [Deployment](#-deployment)

## ✨ Features

### Core Functionality
- **Order Management**: Create, accept, reject, and cancel cargo orders
- **Shipment Lifecycle**: Full shipment workflow from creation to delivery
- **Device Tracking**: IoT device assignment and real-time telemetry
- **Multi-Role Support**: Customer, Provider, Shipper, and Admin roles

### Technical Highlights
- **Clean Architecture**: Modular, layered design with rich domain models
- **JWT Authentication**: RSA-512 signed tokens with refresh token support
- **Database Migrations**: Version-controlled schema with Flyway
- **Message Queue**: RabbitMQ integration for async device communication
- **API Documentation**: OpenAPI 3.0 with Swagger UI

## 🏗 Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Mobile    │     │   Web App   │     │  IoT Device │
│    App      │     │             │     │             │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │
       └───────────────────┼───────────────────┘
                           │
                    ┌──────▼──────┐
                    │   Backend   │ ◄── This Repository
                    │   Service   │
                    │  (Port 8080)│
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
       ┌──────▼──────┐ ┌───▼───┐ ┌──────▼──────┐
       │  PostgreSQL │ │RabbitMQ│ │  Ingestion  │
       │   Database  │ │       │ │   Service   │
       └─────────────┘ └───────┘ └─────────────┘
```

### System Integration

```
┌────────────────────┐          ┌────────────────────┐
│   Backend Service  │◄────────►│  Ingestion Service │
│   (This Repo)      │ RabbitMQ │  (cargo-tracking-  │
│                    │          │   ingestion)       │
└─────────┬──────────┘          └─────────┬──────────┘
          │                               │
          │ REST API                      │ Device Events
          │                               │
    ┌─────▼─────┐                   ┌─────▼─────┐
    │  Mobile/  │                   │    IoT    │
    │  Web App  │                   │  Devices  │
    └───────────┘                   └───────────┘
```

### Business Workflow

```
Customer                    Provider                    Shipper
    │                           │                           │
    │  1. Create Order          │                           │
    │ ─────────────────────────►│                           │
    │                           │                           │
    │                      2. Accept Order                  │
    │                      (Auto-create Shipment)           │
    │                           │                           │
    │                      3. Assign Shipper & Device       │
    │                           │──────────────────────────►│
    │                           │                           │
    │                           │                 4. Start Transit
    │                           │                           │
    │                           │                 5. Complete Delivery
    │◄──────────────────────────│◄──────────────────────────│
    │        Status Sync        │        Status Sync        │
```

## 🛠 Tech Stack

| Category           | Technology               |
|--------------------|--------------------------|
| **Language**       | Kotlin 2.2.21            |
| **Framework**      | Spring Boot 4.0.0        |
| **Database**       | PostgreSQL 15+           |
| **Migrations**     | Flyway                   |
| **Message Queue**  | RabbitMQ                 |
| **Authentication** | JWT (RSA-512)            |
| **API Docs**       | OpenAPI 3.0 / Swagger UI |
| **Build Tool**     | Gradle (Kotlin DSL)      |
| **JDK**            | 21                       |

## 🚀 Getting Started

### Prerequisites

- JDK 21+
- PostgreSQL 15+
- RabbitMQ 3.x
- Gradle 8.x (or use wrapper)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/phamhoa2416/cargo-tracking-be.git
   cd cargo-tracking-be
   ```

2. **Generate JWT Keys** (if not existing)
   ```bash
   mkdir -p src/main/resources/keys
   openssl genrsa -out src/main/resources/keys/private.pem 2048
   openssl rsa -in src/main/resources/keys/private.pem -pubout -out src/main/resources/keys/public.pem
   ```

3. **Configure Environment Variables**
   ```bash
   cp .env.local .env
   # Edit .env with your configuration
   ```

4. **Create Database**
   ```sql
   CREATE DATABASE database;
   ```

5. **Run the Application**
   ```bash
   ./gradlew bootRun
   ```

The application will start on `http://localhost:8080`

## ⚙️ Configuration

All sensitive configuration is externalized to environment variables:

### Required Environment Variables

| Variable               | Description               | Default                                     |
|------------------------|---------------------------|---------------------------------------------|
| `DB_URL`               | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/database` |
| `DB_USERNAME`          | Database username         | `admin`                                     |
| `DB_PASSWORD`          | Database password         | `password`                                  |
| `RABBITMQ_HOST`        | RabbitMQ host             | `localhost`                                 |
| `RABBITMQ_PORT`        | RabbitMQ port             | `5672`                                      |
| `RABBITMQ_USERNAME`    | RabbitMQ username         | `guest`                                     |
| `RABBITMQ_PASSWORD`    | RabbitMQ password         | `guest`                                     |
| `JWT_PUBLIC_KEY_PATH`  | Path to public key        | `classpath:keys/public.pem`                 |
| `JWT_PRIVATE_KEY_PATH` | Path to private key       | `classpath:keys/private.pem`                |
| `SERVER_PORT`          | Application port          | `8080`                                      |
| `INGESTION_URL`        | Ingestion service URL     | `http://localhost:8081`                     |

### Example `.env` file
```env
DB_URL=jdbc:postgresql://localhost:5432/cargo_tracking
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password
RABBITMQ_HOST=localhost
JWT_PUBLIC_KEY_PATH=file:/etc/secrets/public.pem
JWT_PRIVATE_KEY_PATH=file:/etc/secrets/private.pem
SERVER_PORT=8080
INGESTION_URL=http://ingestion-service:8081
```

## 🗃 Database Migrations

This project uses **Flyway** for database migrations. Migrations are located in:
```
src/main/resources/db/migration/
```

### Migration Files
| Version | Description                          |
|---------|--------------------------------------|
| V1      | Create `users` table                 |
| V2      | Create `refresh_tokens` table        |
| V3      | Create `password_reset_tokens` table |
| V4      | Create `orders` table                |
| V5      | Create `shipments` table             |
| V6      | Create `devices` table               |

Migrations run automatically on application startup. To run manually:
```bash
./gradlew flywayMigrate
```

## 📚 API Documentation

Once the application is running, access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### Key Endpoints

| Method  | Endpoint                             | Description       | Role           |
|---------|--------------------------------------|-------------------|----------------|
| `POST`  | `/api/auth/register`                 | Register new user | Public         |
| `POST`  | `/api/auth/login`                    | User login        | Public         |
| `POST`  | `/api/orders`                        | Create order      | Customer       |
| `PATCH` | `/api/orders/{id}/accept`            | Accept order      | Provider       |
| `PATCH` | `/api/shipments/{id}/assign-shipper` | Assign shipper    | Provider       |
| `PATCH` | `/api/shipments/{id}/start-transit`  | Start transit     | Shipper        |
| `PATCH` | `/api/shipments/{id}/complete`       | Complete delivery | Shipper        |
| `GET`   | `/api/devices`                       | List devices      | Provider/Admin |

## 📁 Project Structure

```
src/main/kotlin/com/example/cargotracking/
├── common/                     # Shared infrastructure
│   ├── client/                 # External service clients (Ingestion)
│   ├── entity/                 # Base entity classes
│   ├── exception/              # Global exception handling
│   ├── jwt/                    # JWT utilities
│   ├── messaging/              # RabbitMQ configuration
│   └── security/               # Security configuration
│
└── modules/                    # Domain modules
    ├── user/                   # User management & authentication
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   └── model/
    ├── order/                  # Order management
    ├── shipment/               # Shipment lifecycle
    └── device/                 # IoT device management
```

## 🐳 Deployment

### Docker

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose

```yaml
services:
  backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_URL=jdbc:postgresql://db:5432/cargo_tracking
      - DB_USERNAME=postgres
      - DB_PASSWORD=${DB_PASSWORD}
      - RABBITMQ_HOST=rabbitmq
      - JWT_PUBLIC_KEY_PATH=file:/app/keys/public.pem
      - JWT_PRIVATE_KEY_PATH=file:/app/keys/private.pem
      - INGESTION_URL=http://ingestion:8081
    volumes:
      - ./keys:/app/keys:ro
    depends_on:
      - db
      - rabbitmq

  ingestion:
    image: ghcr.io/phamviethoa2416/cargo-tracking-ingestion:latest
    ports:
      - "8081:8081"
    environment:
      - RABBITMQ_HOST=rabbitmq
    depends_on:
      - rabbitmq

  db:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: cargo_tracking
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data

  rabbitmq:
    image: rabbitmq:3-management-alpine
    ports:
      - "5672:5672"
      - "15672:15672"

volumes:
  postgres_data:
```

### Production Considerations

- ⚠️ **Never commit JWT keys** to version control
- ⚠️ Use strong, unique passwords for a database and RabbitMQ
- ✅ Enable HTTPS in production (via reverse proxy)
- ✅ Set appropriate CORS origins for your frontend domains
- ✅ Configure connection pool sizes based on an expected load
