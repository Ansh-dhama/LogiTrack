# 🚚 LogiTrack — Shipment & Delivery Tracking Backend

LogiTrack is a backend-focused shipment and delivery tracking system built using **Java 17 and Spring Boot**.

The project demonstrates practical backend engineering concepts including:

- REST API design
- JWT authentication
- Email OTP verification
- Role-based authorization
- Shipment lifecycle management
- Driver management
- Automatic driver assignment
- Shipment tracking
- Secure API access
- MySQL persistence
- DTO mapping
- Validation
- Exception handling
- Scheduled background processing
- External email API integration
- Docker-based deployment

The application is deployed as a complete working demo, but the main focus of this project is the **backend architecture and implementation**.

---

# 🌐 Live Demo

### Production Application

👉 **https://logitrack-2.onrender.com**

> The service is deployed on Render.  
> Since it uses a free hosting tier, the first request after inactivity may take some time while the service starts.

---

# 🧠 Backend Architecture

```text
Client
   |
   | HTTPS / REST
   v
Spring Boot Application
   |
   +-------------------------------+
   |                               |
Authentication                 Shipment APIs
   |                               |
   |                               |
Spring Security                 Service Layer
   |                               |
JWT Filter                     Business Logic
   |                               |
Role Authorization             Repository Layer
   |                               |
   +---------------+---------------+
                   |
                   v
               MySQL
                   |
                   |
          External Services
                   |
                   v
             Resend Email API
```

---

# 🛠 Backend Tech Stack

| Technology | Purpose |
|---|---|
| **Java 17** | Core backend language |
| **Spring Boot 3.4.1** | Backend application framework |
| **Spring MVC** | REST API development |
| **Spring Security** | Authentication and authorization |
| **JWT / JJWT** | Stateless authentication |
| **Spring Data JPA** | Database access |
| **Hibernate** | ORM |
| **MySQL** | Relational database |
| **Aiven MySQL** | Cloud database |
| **Bean Validation** | Request validation |
| **MapStruct** | Entity ↔ DTO mapping |
| **Lombok** | Boilerplate reduction |
| **Resend API** | OTP and notification emails over HTTPS |
| **Maven** | Dependency and build management |
| **JUnit** | Backend testing |
| **Docker** | Containerized deployment |
| **Render** | Cloud deployment |

---

# ✨ Core Backend Features

## 🔐 Authentication & Security

LogiTrack uses **Spring Security with stateless JWT authentication**.

Customer authentication follows a two-step login flow:

```text
Email + Password
      |
      v
POST /auth/login
      |
      v
Credentials Verified
      |
      v
Generate 6-digit OTP
      |
      v
Save OTP + Expiry
      |
      v
Send OTP using Resend API
      |
      v
POST /auth/verify-login-otp
      |
      v
OTP Verified
      |
      v
Generate JWT
      |
      v
Access Protected APIs
```

JWT is passed using:

```http
Authorization: Bearer <token>
```

---

# 🔑 OTP Authentication

Customer login does **not immediately generate a JWT**.

First:

```http
POST /auth/login
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password"
}
```

The backend:

1. Validates credentials.
2. Generates a 6-digit OTP.
3. Stores the OTP with expiration.
4. Sends the OTP using the **Resend HTTPS Email API**.

Then:

```http
POST /auth/verify-login-otp
```

Request:

```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

After successful verification, the backend generates a JWT.

---

# 👥 Role-Based Authorization

The application supports three main roles:

```text
USER
DRIVER
ADMIN
```

Spring Security and method-level authorization are used to restrict sensitive operations.

Examples:

```java
@PreAuthorize("hasRole('USER')")
```

```java
@PreAuthorize("hasRole('DRIVER')")
```

```java
@PreAuthorize("hasRole('ADMIN')")
```

---

# 👤 User Operations

Users can:

- Register
- Login using email/password
- Verify OTP
- Receive JWT
- Fetch profile
- Update profile
- Delete account
- Create shipments
- View personal shipments
- Track shipment progress

---

# 🚚 Driver Operations

Drivers can:

- Register
- Login
- Receive JWT
- View profile
- Update profile
- Change availability
- View driver status
- Update assigned shipment status
- Update shipment location

Sensitive driver operations use the authenticated user identity rather than trusting client-supplied IDs.

---

# 🛡 Admin Operations

Admins can:

- Login
- View dashboard statistics
- View all shipments
- Assign drivers
- Change driver availability
- Manage shipment operations

Admin APIs are protected using role-based authorization.

---

# 📦 Shipment Management

A user can create a shipment using:

```http
POST /shipment
```

Example request:

```json
{
  "senderAddress": "Meerut, Uttar Pradesh",
  "receiverAddress": "Delhi, India",
  "weight": 2
}
```

The backend automatically generates a tracking number such as:

```text
TRK-3395904A
```

A shipment contains information such as:

```text
Tracking Number
Sender Address
Receiver Address
Weight
Shipment Status
Assigned Driver
Creation Time
User
```

---

# 🔄 Shipment Lifecycle

Example shipment lifecycle:

```text
PENDING
   |
   v
ASSIGNED
   |
   v
IN_TRANSIT
   |
   v
DELIVERED
```

Additional states can include:

```text
CANCELLED
DELIVERY_ATTEMPTED
RETURNED
```

Status transitions are validated in the service layer.

---

# 📍 Shipment Tracking

Public shipment tracking is exposed through:

```http
GET /track/{trackingNumber}
```

Shipment status history is available through:

```http
GET /shipment/{trackingNumber}/updates
```

Tracking history stores shipment status changes along with timestamps.

---

# 🚛 Automatic Driver Assignment

The backend contains scheduled logic for assigning available drivers to pending shipments.

The assignment process considers:

```text
Pending Shipments
        |
        v
Available Drivers
        |
        v
Assignment Logic
        |
        v
Shipment → Driver
```

The scheduled job helps automate shipment allocation without requiring constant manual admin action.

---

# 📧 Email Notification Service

LogiTrack uses the **Resend API** instead of traditional SMTP.

Architecture:

```text
Spring Boot
    |
    | HTTPS
    v
Resend API
    |
    v
Customer Email
```

This is used for:

- Login OTP
- Shipment notifications
- Delivery notifications
- Cancellation notifications

The email service exposes a reusable method:

```java
sendEmail(
    String to,
    String subject,
    String body
)
```

This allows multiple backend services to send emails using the same implementation.

---

# 🗄 Database Design

The project uses **MySQL with Spring Data JPA and Hibernate**.

Main domain entities include:

```text
User
Driver
Shipment
TrackingUpdate
```

Relationships are handled using JPA entity mappings.

Example:

```text
User
 |
 | 1
 |
 |------ *
        Shipment
           |
           |
           v
        Driver
```

Shipment tracking information is persisted separately to maintain status history.

---

# 🧱 Backend Layered Architecture

```text
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
Database
```

The project is organized into:

```text
LogiTrack/
│
├── Config/
├── Controller/
├── Dto/
├── Entity/
├── Enums/
├── Exceptions/
├── Filter/
├── MapStructs/
├── Repository/
├── Services/
└── Util/
```

### Controller Layer

Responsible for:

```text
HTTP requests
Request validation
Authentication context
HTTP responses
```

### Service Layer

Responsible for:

```text
Business logic
Shipment workflow
OTP generation
Driver assignment
Authorization checks
Tracking logic
Email notifications
```

### Repository Layer

Responsible for:

```text
Database queries
Entity persistence
Spring Data JPA operations
```

---

# 🔒 Security Architecture

```text
Request
   |
   v
Spring Security Filter Chain
   |
   v
JWT Filter
   |
   +---- Invalid / Missing JWT
   |          |
   |          v
   |        Reject
   |
   v
Validate JWT
   |
   v
Load User
   |
   v
SecurityContext
   |
   v
Role Authorization
   |
   v
Controller
```

Security practices implemented in the project include:

- BCrypt password hashing
- Stateless JWT authentication
- OTP-based customer login
- Role-based authorization
- Protected REST APIs
- Request validation
- Backend-enforced roles
- Environment-variable secrets
- Authenticated-user ownership checks
- DTOs instead of exposing entities directly

---

# 🌍 Main REST APIs

## Authentication

| Method | Endpoint | Access |
|---|---|---|
| POST | `/auth/register` | Public |
| POST | `/auth/login` | Public |
| POST | `/auth/verify-login-otp` | Public |
| GET | `/auth/me` | Authenticated |
| PUT | `/auth/update` | Authenticated |
| DELETE | `/auth/delete` | Authenticated |

---

## Driver

| Method | Endpoint | Access |
|---|---|---|
| POST | `/driver/register` | Public |
| POST | `/driver/login` | Public |
| GET | `/driver/profile` | DRIVER |
| PUT | `/driver/update` | DRIVER |
| DELETE | `/driver/delete` | DRIVER |
| POST | `/driver/status` | DRIVER |
| GET | `/driver/checkStatus` | DRIVER |

---

## Shipment

| Method | Endpoint | Access |
|---|---|---|
| POST | `/shipment` | USER |
| GET | `/shipment` | USER |
| GET | `/shipment/{trackingNumber}/updates` | Authenticated |
| PATCH | `/shipment/status` | Authorized user |
| POST | `/shipment/{id}/location` | DRIVER / ADMIN |

---

## Public Tracking

| Method | Endpoint | Access |
|---|---|---|
| GET | `/track/{trackingNumber}` | Public |

---

## Admin

| Method | Endpoint | Access |
|---|---|---|
| POST | `/admin/login` | Public |
| GET | `/admin/dashboard` | ADMIN |
| GET | `/admin/shipments` | ADMIN |
| PUT | `/admin/assign/{shipmentId}/{driverId}` | ADMIN |
| PUT | `/admin/driver-status/{driverId}` | ADMIN |

---

# ⚙️ Environment Variables

Production secrets are not hardcoded.

Required environment variables:

```env
SPRING_DATASOURCE_URL=jdbc:mysql://HOST:PORT/DATABASE
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false

RESEND_API_KEY=your_resend_api_key

APP_JWT_SECRET=your_secure_jwt_secret
```

Render automatically provides the server port through:

```text
PORT
```

Spring Boot reads it using:

```properties
server.port=${PORT:8090}
```

---

# ▶️ Run Backend Locally

### Requirements

```text
Java 17+
MySQL
Maven
```

Clone the repository:

```bash
git clone git@github.com:Ansh-dhama/LogiTrack.git
```

Move into the backend:

```bash
cd LogiTrack/demo
```

Set the required environment variables.

Example:

```bash
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/logitrack"
export SPRING_DATASOURCE_USERNAME="root"
export SPRING_DATASOURCE_PASSWORD="your_password"

export RESEND_API_KEY="your_resend_key"

export APP_JWT_SECRET="your_secure_secret"
```

Run:

```bash
./mvnw spring-boot:run
```

Local backend:

```text
http://localhost:8090
```

---

# 🧪 Build & Test

Compile:

```bash
./mvnw clean compile
```

Run tests:

```bash
./mvnw clean test
```

Build executable JAR:

```bash
./mvnw clean package
```

Run JAR:

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

---

# 🐳 Docker

The project contains a multi-stage Docker build.

Architecture:

```text
Maven Build Image
       |
       v
Build Spring Boot JAR
       |
       v
Java Runtime Image
       |
       v
Run application
```

Build:

```bash
docker build -t logitrack .
```

Run:

```bash
docker run -p 8090:8090 logitrack
```

---

# ☁️ Deployment

Current production setup:

```text
Render
   |
   v
Dockerized Spring Boot
   |
   +--------> Aiven MySQL
   |
   +--------> Resend Email API
```

### Live Backend Demo

**https://logitrack-2.onrender.com**

---

# ✅ Deployment Tests

The deployed backend has been tested for:

```text
✓ Application startup
✓ Frontend/static application response
✓ Customer password authentication
✓ OTP generation
✓ OTP email delivery
✓ OTP verification
✓ JWT generation
✓ Protected profile API
✓ Unauthorized request rejection
✓ Shipment creation
✓ Shipment listing
✓ Role-based API protection
✓ Invalid tracking-number handling
```

---

# 🎯 Backend Concepts Demonstrated

This project was built to practice and demonstrate:

```text
Java Backend Development
Spring Boot
REST APIs
Spring Security
JWT
OTP Authentication
Role-Based Access Control
Spring Data JPA
Hibernate
MySQL
Transactions
DTO Pattern
MapStruct
Validation
Exception Handling
Scheduled Jobs
External API Integration
Email Notifications
Docker
Cloud Deployment
Environment Variables
```

---

# 🚀 Future Backend Improvements

Planned improvements include:

- Refresh token support
- Password reset flow
- Registration email verification
- Redis caching
- Rate limiting
- API documentation using Swagger / OpenAPI
- WebSocket-based real-time tracking
- Pagination and filtering
- Audit logging
- Database migrations using Flyway
- Integration tests
- Testcontainers
- GitHub Actions CI/CD
- Metrics using Micrometer
- Prometheus + Grafana monitoring
- Structured production logging

---

# 👨‍💻 Author

**Ansh Dhama**

GitHub: **Ansh-dhama**

---

# 📌 Project Purpose

LogiTrack is primarily a **Java backend engineering portfolio project** designed to demonstrate production-oriented Spring Boot development.

The browser-based interface included with the deployment is used to interact with and demonstrate the backend APIs; the primary engineering focus of this repository is the backend system.
