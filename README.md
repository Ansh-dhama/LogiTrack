LogiTrack

LogiTrack is a full-stack shipment and delivery tracking platform built with Spring Boot and React. It supports user authentication with email OTP, driver management, shipment creation and tracking, automatic driver assignment, admin operations, and role-based API security.

The React frontend is built and served directly from Spring Boot, so the application can be deployed as a single service.

Features

User

Register a new account

Login with email and password

Verify login using email OTP

JWT-based authentication

View and update profile

Create shipments

View personal shipments

Track shipment progress

Driver

Driver registration and login

View driver profile

Update driver profile

Set availability status

View assigned shipments

Update shipment delivery status

Update shipment location

Admin

Admin login

Dashboard statistics

View shipments

Assign drivers to shipments

Manage driver availability/status

Monitor shipment operations

Shipment Tracking

Public tracking by tracking number

Shipment status history

Delivery progress updates

Driver location updates

Tracking event storage

Automatic Driver Assignment

Scheduled shipment assignment

Considers only available drivers

Attempts to balance active shipment load

Avoids assigning a shipment to an invalid driver

Tech Stack

Backend

Java

Spring Boot

Spring Security

JWT Authentication

Spring Data JPA

Hibernate

MySQL

MapStruct

Lombok

Maven

Java Mail / SMTP

JUnit / Mockito

Frontend

React

Vite

Axios

React Router

Responsive dashboard UI

Deployment

Spring Boot serves the React production build

Docker support

Environment-variable based configuration

Suitable for platforms such as Koyeb, Railway, Render, or a Linux VM

Project Structure

LogiTrack-main/
│
├── demo/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/LogiTrack/
│   │   │   │   ├── Config/
│   │   │   │   ├── Controller/
│   │   │   │   ├── Dto/
│   │   │   │   ├── Entity/
│   │   │   │   ├── Enums/
│   │   │   │   ├── Exceptions/
│   │   │   │   ├── Filter/
│   │   │   │   ├── MapStructs/
│   │   │   │   ├── Repository/
│   │   │   │   └── Services/
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── static/
│   │   │           ├── index.html
│   │   │           └── assets/
│   │   └── test/
│   │       └── java/LogiTrack/
│   ├── Dockerfile
│   ├── pom.xml
│   ├── mvnw
│   └── DEPLOYMENT_ENV.md
│
└── README.md

Authentication Flow

User Login with OTP

User enters email + password
        ↓
POST /auth/login
        ↓
Password verified
        ↓
OTP sent to registered email
        ↓
POST /auth/verify-login-otp
        ↓
OTP verified
        ↓
JWT generated
        ↓
Frontend stores JWT
        ↓
Protected APIs become accessible

JWT is sent in protected requests using:

Authorization: Bearer <token>

Main API Endpoints

Authentication

Method

Endpoint

Access

Description

POST

/auth/register

Public

Register user

POST

/auth/login

Public

Verify credentials and send OTP

POST

/auth/verify-login-otp

Public

Verify OTP and generate JWT

GET

/auth/me

Authenticated

Get current user profile

PUT

/auth/update

Authenticated

Update user profile

DELETE

/auth/delete

Authenticated

Delete user account

Driver

Method

Endpoint

Access

Description

POST

/driver/register

Public

Register driver

POST

/driver/login

Public

Driver login

GET

/driver/profile

Authenticated

Get driver profile

PUT

/driver/update

Authenticated

Update driver profile

DELETE

/driver/delete

Authenticated

Delete driver

POST

/driver/status

Authenticated

Update availability

GET

/driver/checkStatus

Authenticated

Check availability

Shipment

Method

Endpoint

Access

Description

POST

/shipment

Authenticated

Create shipment

GET

/shipment

Authenticated

Get shipments

GET

/shipment/{trackingNumber}/updates

Authenticated

Get shipment updates

PATCH

/shipment/status

Authenticated

Update shipment status

POST

/shipment/{id}/location

Authenticated

Update shipment location

Public Tracking

Method

Endpoint

Access

Description

GET

/track/{trackingNumber}

Public

Track shipment

Admin

Method

Endpoint

Access

Description

POST

/admin/login

Public

Admin login

GET

/admin/dashboard

ADMIN

Dashboard statistics

GET

/admin/shipments

ADMIN

View all shipments

PUT

/admin/assign/{shipmentId}/{driverId}

ADMIN

Assign driver

PUT

/admin/driver-status/{driverId}

ADMIN

Change driver status

Security

LogiTrack uses Spring Security with stateless JWT authentication.

Public routes include:

/auth/login
/auth/register
/auth/verify-login-otp
/driver/login
/driver/register
/admin/login
/track/**

All other API routes require authentication unless explicitly configured otherwise.

Production protections include:

CSRF disabled for stateless REST authentication

HTTP Basic disabled

Default Spring login form disabled

JWT filter added before UsernamePasswordAuthenticationFilter

Role-based authorization

User registration role enforced by the backend

Driver availability changes tied to the authenticated driver

Shipment location updates restricted to authorized users

Admin shipment responses use DTOs instead of exposing JPA entities directly

Environment Variables

Do not commit passwords, JWT secrets, database credentials, or email app passwords to Git.

PORT=8090
SPRING_DATASOURCE_URL=jdbc:mysql://HOST:PORT/DATABASE
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
SPRING_JPA_HIBERNATE_DDL_AUTO=update
JWT_SECRET=your_secure_jwt_secret
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_email_app_password

Example application.properties references:

server.port=${PORT:8090}
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
jwt.secret=${JWT_SECRET}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

Run Locally

Requirements

Java 17+ or the Java version configured by the project

MySQL

Maven or Maven Wrapper

Node.js 20+ if rebuilding the frontend

Backend

cd demo
./mvnw clean spring-boot:run

Open:

http://localhost:8090

Build the Frontend

npm install
npm run build

Copy the Vite production build into Spring Boot:

rm -rf src/main/resources/static
mkdir -p src/main/resources/static
cp -R /path/to/frontend/dist/. src/main/resources/static/

The final index.html and hashed assets must come from the same Vite build.

Run Tests

./mvnw clean test
./mvnw clean package
java -jar target/*.jar

Production Architecture

Browser
   ↓
HTTPS
   ↓
Spring Boot
   ├── React static frontend
   ├── REST APIs
   ├── JWT authentication
   ├── OTP email service
   └── Shipment / driver services
            ↓
          MySQL

Suggested Production Test Flow

USER
Register
→ Login
→ Receive OTP
→ Verify OTP
→ JWT
→ Profile
→ Create shipment
→ View shipment
→ Track shipment

DRIVER
Register
→ Login
→ Profile
→ Set availability
→ View assigned shipment
→ Start delivery
→ Update location
→ Mark delivered

ADMIN
Login
→ Dashboard
→ View shipments
→ Assign driver
→ Manage driver status

PUBLIC
Enter tracking number
→ View shipment progress

Git Workflow

git status
git add .
git commit -m "Your commit message"
git pull --rebase origin main
git push origin main

Avoid force-pushing unless you fully understand the impact.

Future Improvements

Refresh tokens

Password reset flow

Registration email verification

Live driver location using WebSocket

Real-time notifications

Pagination and filtering

Audit logs

Docker Compose for local development

GitHub Actions CI/CD

Automated integration tests

OpenAPI/Swagger documentation

Cloud monitoring and structured logging

Author

Ansh Dhama

GitHub: Ansh-dhama

License

This project is intended for learning, development, portfolio, and deployment purposes. Add an explicit open-source license if you plan to distribute or allow reuse of the source code.
