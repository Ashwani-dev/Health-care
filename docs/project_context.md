# Healthcare Management System

**Document Version:** 1.1.0  
**Last Updated:** 2026-06-07  
**Project Status:** Active development

## Table of Contents

- [1. Executive Summary](#1-executive-summary)
- [2. Technology Stack](#2-technology-stack)
- [3. Application Architecture](#3-application-architecture)
- [4. Core Features](#4-core-features)
- [5. Data Flow & State Management](#5-data-flow--state-management)
- [6. API Integration](#6-api-integration)
- [7. Performance Optimizations](#7-performance-optimizations)
- [8. Refactoring Guidelines / Coding Standards](#8-refactoring-guidelines--coding-standards)
- [9. Component Reference](#9-component-reference)
- [10. Service Layer](#10-service-layer)
- [11. Known Issues & Solutions](#11-known-issues--solutions)
- [12. Deployment & Development](#12-deployment--development)
- [13. Appendix: File Reference](#13-appendix-file-reference)

## 1. Executive Summary

### Project Overview
The repository contains a Spring Boot backend for a healthcare workflow system that manages patient and doctor accounts, appointments, availability, payment collection, password resets, video-call sessions, and email notifications. The code is structured around REST controllers, service-layer business logic, JPA entities, and Spring Data repositories, with JWT-based authentication and optional TOTP/MFA for both patient and doctor accounts.

The application is built for API consumers rather than a browser UI. No frontend source code is present in the workspace; the backend emits JSON responses, HTML email templates, and Twilio/Cashfree integration payloads for downstream clients.

### Key Capabilities
- JWT-based authentication with role claims for `PATIENT` and `DOCTOR`
- Optional TOTP/MFA enrollment, confirmation, disablement, and TOTP login
- Patient and doctor profile management with role-based access controls and S3 profile image uploads
- AWS S3 Integration for direct client-to-S3 profile image uploads via presigned URLs
- Appointment hold creation, booking, update, cancellation, and availability lookup
- Dynamic appointment filtering by date range, time range, and status
- Payment initiation, webhook processing, status lookup, and paginated payment history
- Twilio-backed video-call session creation, token issuance, webhook processing, and event logging
- HTML email notifications for appointment confirmation and password reset

### Current Metrics
| Metric | Value |
|---|---:|
| Java source files | 109 |
| Test source files | 1 |
| Controllers | 8 |
| Services | 16 |
| Entities | 10 |
| Repositories | 10 |
| DTO classes | 28 |
| Email templates | 3 |
| Primary database | PostgreSQL |
| Test database | H2 (`test` scope) |
| Cache hit rate | TBD |
| Active users | TBD |
| SLA / latency target | TBD |
| Data volume | TBD |
| Deployment target(s) | Local JVM, Docker Compose, container image, cloud hosts (manual) |

## 2. Technology Stack

### Backend
```yaml
framework:
  spring_boot: 3.4.4
  spring_boot_parent_pom: org.springframework.boot:spring-boot-starter-parent:3.4.4
language:
  java: 21
runtime:
  jre: eclipse-temurin:21-jre-jammy
  build_image: maven:3.9-eclipse-temurin-21
package_manager:
  maven: mvnw / Maven CLI
auth:
  mechanism: JWT (HS256) + role claim + optional TOTP/MFA
  security_filter: JwtFilter
  password_hashing: BCryptPasswordEncoder
  mfa: dev.samstevens.totp:totp-spring-boot-starter:1.7.1
persistence:
  primary_database: PostgreSQL
  dev_test_database: H2 (test scope)
  orm: Spring Data JPA / Hibernate
storage:
  provider: AWS S3 (direct-to-S3 uploads via presigned URLs)
messaging:
  broker: RabbitMQ (Spring AMQP)
email:
  sender: Spring Mail
  templates: Thymeleaf HTML templates
video:
  provider: Twilio Video SDK 9.14.1
payments:
  primary_gateway: Cashfree 5.0.1
  alternate_gateway_code: Paytm gateway class exists but is disabled by configuration/commentary
key_libraries:
  jjwt_api: 0.11.5
  jjwt_impl: 0.11.5
  jjwt_jackson: 0.11.5
  modelmapper: 3.2.0
  okhttp: 4.10.0
  spring_dotenv: 4.0.0
  aws_sdk_s3: 2.25.0
  spring_boot_starter_hateoas: managed by Spring Boot parent
  spring_boot_starter_actuator: managed by Spring Boot parent
build_tool:
  maven_compiler_plugin: configured for Java 21
  spring_boot_maven_plugin: packages executable jar
linter:
  status: TBD (no linter configuration file found)
test_runner:
  junit: Jupiter via spring-boot-starter-test
ci:
  status: TBD (no CI pipeline file found)
os_shell:
  os: macOS
  shell: zsh
```

### Frontend
```yaml
status: Not present in this workspace
base_url_hint: http://localhost:5173 (from env.example and application.properties)
client_state_library: TBD
frontend_auth_storage: TBD
```

### Dev Tooling
```yaml
configuration_profiles:
  dev: application-dev.properties
  docker: application-docker.properties
  prod: application-prod.properties
  base: application.properties
containerization:
  dockerfile: multi-stage build with Maven + Temurin 21
  compose: docker-compose.yml
logging:
  file: logs/application.log
  root_level: INFO by default, DEBUG in dev profile
static_docs:
  api_docs: API_DOCUMENTATION.md
  db_docs: DATABASE_SCHEMA.md
  deployment_docs: DEPLOYMENT_GUIDE.md
  dev_guide: DEVELOPER_GUIDE.md
```

## 3. Application Architecture

### Directory Structure
```text
Healthcare-backend/ # Spring Boot backend repository
├── README.md # High-level overview, setup, endpoint list, and quick start
├── CHANGELOG.md # Release notes and feature history
├── DATABASE_SCHEMA.md # Schema documentation and SQL snippets
├── DEPLOYMENT_GUIDE.md # Environment, Docker, cloud, and production setup
├── DEVELOPER_GUIDE.md # Coding standards, patterns, testing, and troubleshooting
├── API_DOCUMENTATION.md # Endpoint reference and payload examples
├── QUICK_REFERENCE.md # Copy-paste commands and endpoint cheat sheet
├── PAYMENT_API_GUIDE.md # Payment-specific pagination and filtering notes
├── DOCUMENTATION_INDEX.md # Navigation index for docs
├── env.example # Environment-variable template
├── Dockerfile # Multi-stage container build
├── docker-compose.yml # Local docker orchestration for app + RabbitMQ
├── .gitignore # Ignores target/, logs/, .env, IDE files, docs/*, etc.
├── .dockerignore # Excludes build artifacts, docs, logs, .env, tests in image builds
├── docs/ # Documentation workspace (ignored by default except project_context.md)
│   └── project_context.md # Authoritative project context document
├── logs/ 🗄️ # Runtime logs; gitignored
├── target/ 🗄️ # Maven build outputs; gitignored
└── src/
    ├── main/
    │   ├── java/com/ashwani/HealthCare/ # Application root package
    │   │   ├── HealthCareApplication.java # Bootstraps Spring Boot, async, scheduling, transactions
    │   │   ├── Config/ # Bean wiring, security, messaging, payments, auditing, Twilio, AWS S3
    │   │   ├── Controllers/ # REST entry points for auth, appointment, doctor, patient, payment, availability, video call, MFA, Profile Image
    │   │   ├── DTO/ # Request/response contracts grouped by feature (Auth, Appointment, Doctor, Patient, Payment, Video)
    │   │   ├── Entity/ # JPA entities and workflow state tables
    │   │   ├── Enums/ # Appointment, gender, and login method enums
    │   │   ├── ExceptionHandlers/ # Domain-specific exceptions and global error mapping
    │   │   ├── Filter/ # JWT request filter
    │   │   ├── Repository/ # Spring Data JPA repositories and custom queries
    │   │   ├── Service/ # Business logic, integrations (Twilio, Cashfree, AWS S3), and event listeners
    │   │   ├── Utility/ # JWT helper and time-slot helper
    │   │   └── specifications/ # JPA Specification predicates for dynamic search/filtering
    │   └── resources/
    │       ├── application.properties # Base config shared by all profiles
    │       ├── application-dev.properties # Local-development overrides
    │       ├── application-docker.properties # Container-specific overrides
    │       ├── application-prod.properties # Production overrides
    │       └── templates/
    │           └── email/ # Thymeleaf HTML templates for transactional email
    │               ├── patient-appointment.html # Patient appointment confirmation email
    │               ├── doctor-notification.html # Doctor new-appointment email
    │               └── password-reset.html # Password reset email
    └── test/
        └── java/com/ashwani/HealthCare/ # Test sources
            └── HealthCareApplicationTests.java # Smoke test for Spring context
```

### Design Patterns
1. **Layered Architecture** — Separates controllers, services, repositories, and entities; each layer has a narrow responsibility in the codebase.
2. **Feature-Based Organization** — Packages under `DTO`, `Service`, `Controllers`, and `Repository` are grouped by business domain (`Appointment`, `Payment`, `Doctor`, `Patient`, `Auth`, `Communication`).
3. **DTO Pattern** — External API contracts are represented as DTOs/records instead of exposing entities directly, e.g. `AuthResponse`, `PatientProfile`, `PaymentResponse`.
4. **Repository Pattern** — Data access is centralized in Spring Data interfaces such as `DoctorRepository`, `AppointmentRepository`, and `PaymentRepository`.
5. **Service Layer Pattern** — Business rules live in services such as `AppointmentService`, `PaymentService`, and `VideoCallService` rather than controllers.
6. **Specification Pattern** — Dynamic filtering is implemented via `AppointmentSpecifications`, `DoctorSpecifications`, and `PaymentSpecifications`.
7. **Strategy / Factory Pattern** — `PaymentGatewayFactory` selects the active `PaymentGateway` implementation based on the injected bean/profile.
8. **Filter Chain Pattern** — `JwtFilter` adds security context population before the Spring Security authorization layer.
9. **Global Exception Handling** — `GlobalExceptionHandler` normalizes exceptions into consistent JSON error responses.
10. **Asynchronous Side-Effect Processing** — `EmailService` uses `@Async`; `PaymentEventListener` consumes payment-completed events off the queue.
11. **Event-Driven Workflow** — Payment completion publishes `PaymentCompletedEvent` to RabbitMQ, where the appointment is created later by a listener.
12. **Auditing Pattern** — Entities use `@CreatedDate`, `@LastModifiedDate`, and `AuditingConfig` to capture metadata automatically.
13. **Builder Pattern** — `VideoCallSessions`, `VideoCallEvent`, and `TwilioWebhookEvent` use Lombok builders.
14. **Record-Based Immutable DTOs** — Several request/response objects are Java records (`AuthResponse`, `TotpConfirmRequest`, `DoctorProfileUpdateRequest`).
15. **Profile-Based Configuration** — `dev`, `docker`, and `prod` profiles override base properties without code changes.
16. **HATEOAS Pagination** — Appointment and payment list endpoints return `PagedModel<EntityModel<...>>` rather than raw lists.
17. **Direct S3 Presigned URL Upload Pattern** — Offloads heavy file uploads by generating temporary upload URLs, allowing clients to PUT binaries directly to S3.

## 4. Core Features

### 4.1 Authentication, JWT, Password Reset, and MFA
**Purpose:** Authenticate patients and doctors, issue JWTs, and support password reset plus TOTP enrollment/login.

**Entry components/routes:** `AuthController`, `MfaController`, `JwtFilter`, `JWTUtility`.

**Fields / inputs**

| Field | Type | Required | Source |
|---|---|---:|---|
| `email` | `String` | Yes | API |
| `password` | `String` | Yes for password login | API |
| `code` | `String` (6 digits) | Yes for TOTP login/confirm | API |
| `secret` | `String` | Yes for TOTP confirm | API |
| `token` | `String` | Yes for password reset | API |
| `newPassword` | `String` | Yes for reset | API |
| `userType` | `String` (`PATIENT` / `DOCTOR`) | Yes | Query param |

**Step-by-step user flow**
1. Register a patient or doctor via `/api/auth/patient/register` or `/api/auth/doctor/register`.
2. For password login, call `/api/auth/login/password?userType=...` with email/password.
3. For MFA enrollment, send the JWT in `Authorization: Bearer ...`, call `/api/auth/totp/setup`, scan the QR code, then call `/api/auth/totp/confirm` with the secret + code.
4. After MFA is enabled, log in with `/api/auth/login/totp?userType=...`.
5. For password recovery, call the relevant `forgot-password` endpoint, then `POST /api/auth/reset-password` with the token from email.

**Business rules**
- Email and username must be unique for both patients and doctors.
- Doctors also have unique `license_number` validation.
- If an account has `loginMethod == TOTP`, password login is blocked with `LoginMethodMismatchException`.
- TOTP setup is denied if TOTP is already enabled.
- TOTP login requires `totpEnabled == true` and a valid 6-digit code.
- Reset tokens are one-time use and expire based on `password.reset.token.expiry.minutes`.
- JWT claims include `sub=userId` and `role`.

**Color coding / UX rules**
- Backend returns standard JSON error payloads; no numeric UI threshold logic exists in this repo.
- Email templates use visual success/warning styling, but frontend thresholds are TBD.

**Cascading flow**
```text
Client login
  -> AuthController
  -> AuthService
  -> Repository lookup
  -> BCrypt verification / TOTP verification
  -> JWTUtility.generateToken()
  -> AuthResponse
  -> Client stores token (frontend responsibility: TBD)
```

**Persistence keys**
- No server-side localStorage/sessionStorage/cookie keys are used.
- Persisted server records: `patients`, `doctors`, `password_reset_tokens`.

**Non-obvious logic snippet**
```java
public AuthResponse loginPatient(String email, String password) {
    Patient patient = patientRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", email));

    if (patient.getLoginMethod() == LoginMethod.TOTP) {
        throw new LoginMethodMismatchException(
                "This account uses authenticator login only. Please use TOTP code.");
    }

    if (!passwordEncoder.matches(password, patient.getPassword())) {
        throw new InvalidCredentialsException();
    }

    String token = jwtUtility.generateToken(patient.getId().toString(), "PATIENT");
    return new AuthResponse(true, token, "PATIENT", patient.getId(), patient.getLoginMethod().name());
}
```

### 4.2 Doctor and Patient Profiles
**Purpose:** Allow authenticated users to read and update their own profile data, request presigned URLs for profile picture S3 upload, confirm or remove profile pictures, plus public doctor lookup by ID.

**Entry components/routes:** `DoctorController`, `PatientController`, `DoctorService`, `PatientService`, `AwsS3Service`.
- `GET /api/doctor/profile` and `GET /api/patient/profile`
- `PUT /api/doctor/profile` and `PUT /api/patient/profile`
- `PATCH /api/doctor/profile` and `PATCH /api/patient/profile` (Profile image upload management)

**Fields / inputs**

| Field | Type | Required | Source | Description / Format |
|---|---|---:|---|---|
| `full_name` | `String` | Yes for updates | API | User's displayed full name |
| `medical_experience` | `Integer` | Yes for doctor update | API | Years of practice |
| `license_number` | `String` | Yes for doctor update | API | Medical license key |
| `address` | `String` | Optional for patient update | API | User's mailing/clinic address |
| `specialization` | `String` | Static/persisted | DB | Doctor's department |
| `gender` | `Gender` enum | Persisted | DB | Gender |
| `profileImageUrl` | `String` | Optional | API | S3 Object Key or image location. Max 500 chars. |

**Step-by-step user flow**
1. Authenticate and send a JWT.
2. Call `/api/doctor/profile` or `/api/patient/profile` to fetch the mapped DTO.
3. Send `PUT /api/doctor/profile` or `PUT /api/patient/profile` with updated allowed fields only.
4. For public doctor lookup, call `GET /api/doctor/{id}`.

**Profile Image Upload User Flow (Direct-to-S3 Presigned URL):**
1. **Request Presigned Upload URL:** Client sends a `PATCH` request to `/api/doctor/profile` or `/api/patient/profile` with body `{"profileImageUrl": null}` (or omitting it).
2. **Retrieve S3 URL:** The backend generates an S3 object key formatted as `profile-images/{type}/{userId}/{timestamp}-avatar.jpg` and requests a temporary upload URL from `S3Presigner` valid for 15 minutes. It returns:
   ```json
   {
     "presignedUploadUrl": "https://...",
     "s3ObjectKey": "profile-images/...",
     "expirationTimeMinutes": 15
   }
   ```
3. **Client Upload:** Client performs a direct HTTP `PUT` request containing the image binary to `presignedUploadUrl`.
4. **Confirm Upload:** Client sends a `PATCH` request with body `{"profileImageUrl": "<s3ObjectKey>"}` using the key returned in step 2. The backend stores the object key in the database and returns the response DTO.
5. **Remove Image:** Client sends a `PATCH` request with body `{"profileImageUrl": "remove"}`. The backend deletes the database value (sets it to `null`).

**Business rules**
- Doctor profile update validates unique `license_number` before save.
- Patient profile updates only `full_name` and `address`.
- Authentication principal is parsed as numeric user ID.
- `DoctorProfile` includes `totpEnabled`; the change history shows this field was explicitly added.
- The `profileImageUrl` length is validated to not exceed 500 characters.
- Direct uploads bypass the Spring Boot backend completely to conserve JVM memory.

**Cascading flow**
```text
Profile GET/PUT:
  Controller reads Principal -> Repository lookup -> DTO construction -> Return DTO

Presigned URL Generation:
  PATCH (image = null) -> AwsS3Service -> S3Presigner -> Return presigned URL + S3 key

Direct S3 Upload:
  Client -> HTTP PUT binary -> AWS S3 Bucket

Confirm Upload:
  PATCH (image = key) -> Repository save -> Update profileImageUrl column -> Return success DTO

Remove Profile Image:
  PATCH (image = "remove") -> Repository save -> Set profileImageUrl column to null -> Return success DTO
```

**Persistence keys**
- `doctors`, `patients` (columns: `profile_image_url`)

**Non-obvious logic snippet**
```java
if (updateRequest.license_number() != null && !updateRequest.license_number().trim().isEmpty()) {
    if (doctor.getLicense_number() == null ||
            !doctor.getLicense_number().equals(updateRequest.license_number())) {
        doctorRepository.findByLicenseNumber(updateRequest.license_number())
                .ifPresent(existingDoctor -> {
                    if (!existingDoctor.getId().equals(doctorId)) {
                        throw new DuplicateResourceException("Doctor", "license_number");
                    }
                });
    }
}
```

### 4.3 Availability Management
**Purpose:** Let doctors define their weekly availability and allow patients to query available time slots.

**Entry components/routes:** `AvailabilityController`, `AvailabilityService`, `DoctorAvailabilityRepository`.

**Fields / inputs**

| Field | Type | Required | Source |
|---|---|---:|---|
| `dayOfWeek` | `DayOfWeek` | Yes | API |
| `startTime` | `LocalTime` | Yes | API |
| `endTime` | `LocalTime` | Yes | API |
| `isAvailable` | `Boolean` | Optional, defaults `true` | API |
| `doctorId` | `Long` | Yes | Path |
| `slotId` | `Long` | Yes for delete | Path |

**Step-by-step user flow**
1. Doctor authenticates with a JWT and posts one or more availability ranges to `/api/availability/{doctorId}`.
2. The controller verifies the principal owns that doctor ID before saving.
3. Patients call `GET /api/availability/{doctorId}` or appointment slot lookup endpoints.
4. Doctors can delete a single slot using `DELETE /api/availability/{doctorId}/{slotId}`.

**Business rules**
- A doctor can only mutate their own availability.
- Deletion returns `404` if the slot does not exist.
- Availability data is modeled as a weekly schedule, not a date-specific calendar entry.

**Color coding / UX rules**
- Backend has no color-threshold logic.
- The frontend may color available/unavailable slots, but that implementation is not present here.

**Cascading diagram**
```text
Doctor availability change
  -> AvailabilityController
  -> Principal ID check
  -> AvailabilityService
  -> DoctorAvailabilityRepository
  -> doctoravailability table
```

**Persistence keys**
- `doctoravailability`

**Non-obvious logic snippet**
```java
if(!principal.getName().equals(doctorId.toString())){
    throw new AccessDeniedException("You can only update your own availability");
}

List<AvailabilityResponseDto> availabilities = availabilityService.setAvailability(doctorId, requests);
return ResponseEntity.ok(availabilities);
```

### 4.4 Appointment Hold, Booking, Update, Cancellation, and Slot Lookup
**Purpose:** Reserve a slot during payment, create the appointment after payment success, and expose doctor/patient appointment history with filtering.

**Entry components/routes:** `AppointmentController`, `AppointmentService`, `AppointmentEventListener`, `AppointmentRepository`, `AppointmentHoldRepository`.

**Fields / inputs**

| Field | Type | Required | Source |
|---|---|---:|---|
| `patientId` | `Long` | Yes | API / event |
| `doctorId` | `Long` | Yes | API / event |
| `date` | `LocalDate` | Yes for hold | API |
| `startTime` | `LocalTime` | Yes | API |
| `reason` | `String` | Yes for hold | API |
| `appointmentStartDate` | `LocalDate` | Optional | Query |
| `appointmentEndDate` | `LocalDate` | Optional | Query |
| `endTime` | `LocalTime` | Optional | Query/update |
| `status` | `String` | Optional | Query |
| `holdReference` | `String` | Event-driven | DB/event |

**Step-by-step user flow**
1. The client creates an appointment hold with `POST /api/appointments/hold`.
2. The hold is saved for 15 minutes and returns a generated `hold_...` reference.
3. The client completes payment using the returned order/session ID.
4. Cashfree webhook updates payment status; the payment-completed event is published to RabbitMQ.
5. `PaymentEventListener` consumes the event, validates the hold, creates the appointment, and deletes the hold.
6. Appointment history endpoints return paginated HATEOAS responses.
7. Cancellation is allowed for the patient or doctor, but patients must respect the 24-hour rule.
8. Appointment update is only allowed while status is `SCHEDULED`.

**Business rules**
- `POST /api/appointments/book` is deprecated and currently disabled in code.
- Holds expire after 15 minutes.
- A payment must have status `SUCCESS` before booking proceeds.
- Duplicate booking is blocked by slot checks and event idempotency (`findByPaymentId`).
- Patients cannot cancel inside the 24-hour window; doctors can cancel anytime.
- Update requests only apply to `SCHEDULED` appointments.
- Available slots are generated in 30-minute increments.

**Color coding / UX rules**
- Email templates display appointment confirmation and reminder states using green/blue/orange visual cues, but those are email-only.
- Backend status values are textual (`SCHEDULED`, `CANCELLED`, `COMPLETED`).

**Cascading diagram**
```text
Hold -> Payment -> Event -> Appointment
  POST /hold
      -> AppointmentHold
      -> Cashfree initiate
      -> webhook updates Payment
      -> RabbitMQ payment.completed
      -> PaymentEventListener
      -> bookAppointment()
      -> Appointment
```

**Persistence keys**
- `appointment_hold`, `appointments`

**Non-obvious logic snippet**
```java
if (holdReference == null) {
    validateSlotNotBooked(doctor, date, startTime);
} else {
    log.info("Booking from hold reference: {}, skipping duplicate slot check", holdReference);
}

validateDoctorAvailability(doctor, date, startTime);

LocalTime endTime = startTime.plusMinutes(30);
Appointment appointment = new Appointment();
appointment.setPatient(patient);
appointment.setDoctor(doctor);
appointment.setAppointmentDate(date);
appointment.setStartTime(startTime);
appointment.setEndTime(endTime);
appointment.setStatus("SCHEDULED");
appointment.setPaymentDetails(payment);
```

### 4.5 Payments
**Purpose:** Create payment orders, handle Cashfree/Paytm webhook callbacks, and expose filtered payment history.

**Entry components/routes:** `PaymentController`, `PaymentService`, `PaymentGatewayFactory`, `CashfreePaymentGateway`, `PaytmPaymentGateway`, `PaymentEventListener`.

**Fields / inputs**

| Field | Type | Required | Source |
|---|---|---:|---|
| `customerId` | `String` | Yes | API |
| `customerName` | `String` | Yes | API |
| `customerPhone` | `String` | Yes | API |
| `customerEmail` | `String` | Yes | API |
| `amount` | `BigDecimal` | Yes | API |
| `appointmentHoldReference` | `String` | Yes in hold-based flow | API |
| `status` | `String` | Optional filter | Query |
| `paymentMode` | `String` | Optional filter | Query |
| `minAmount` / `maxAmount` | `BigDecimal` | Optional filter | Query |

**Step-by-step user flow**
1. Create a hold for the appointment slot.
2. Call `POST /api/payments/initiate` to create the gateway order.
3. Save the returned order/session token in the client and redirect to the gateway.
4. Gateway webhooks hit `/api/payments/webhook/cashfree` or `/api/payments/webhook/paytm`.
5. `PaymentService.handleWebhook()` updates the payment row and publishes `PaymentCompletedEvent` if the status becomes successful.
6. `PaymentEventListener` creates the appointment and deletes the hold.
7. Patient payment history is available through `/api/payments/payment-details/{id}`.

**Business rules**
- Active gateway is selected by Spring profile and injected bean wiring.
- Webhook signature validation is configurable and disabled by default in the current properties.
- Test webhooks are detected heuristically and skipped.
- Payment status defaults to `PENDING` when not found.
- `PaymentEventListener` avoids duplicate appointment creation by checking `findByPaymentId()` first.

**Color coding / UX rules**
- Gateway debug logs use emoji markers (`✅`, `❌`, `⚠️`) in backend logs only.
- No frontend payment status color rules are implemented in this repo.

**Cascading diagram**
```text
POST /api/payments/initiate
  -> PaymentController
  -> PaymentService
  -> PaymentGatewayFactory
  -> CashfreePaymentGateway / PaytmPaymentGateway
  -> payment row saved as PENDING
  -> webhook -> payment row updated
  -> RabbitMQ event -> appointment creation
```

**Persistence keys**
- `payments`

**Non-obvious logic snippet**
```java
if (isSuccessfulStatus(newStatus) && !isSuccessfulStatus(previousStatus)){
    PaymentCompletedEvent event = new PaymentCompletedEvent();
    event.setOrderId(payload.getOrderId());
    event.setReferenceId(payload.getReferenceId());
    event.setCustomerId(payment.getPatientId().toString());
    event.setOrderAmount(payload.getOrderAmount());
    event.setPaymentMode(payload.getPaymentMode());
    event.setAppointmentHoldReference(payment.getAppointmentHoldReference());
    event.setPaymentId(payment.getId());

    MessageProperties props = MessagePropertiesBuilder.newInstance()
            .setContentType(MessageProperties.CONTENT_TYPE_JSON)
            .setExpiration("600000")
            .build();

    Message message = rabbitTemplate.getMessageConverter().toMessage(event, props);
    rabbitTemplate.send("payment.completed", message);
}
```

### 4.6 Video Calls and Webhooks
**Purpose:** Create Twilio rooms for appointments, issue access tokens, and persist video-session lifecycle events.

**Entry components/routes:** `VideoCallController`, `VideoCallService`, `TwilioConfig`, `VideoCallSessionsRepository`, `VideoCallEventRepository`, `TwilioWebhookEventRepository`.

**Fields / inputs**

| Field | Type | Required | Source |
|---|---|---:|---|
| `appointmentId` | `Long` | Yes | Path |
| `userType` | `String` (`PATIENT` / `DOCTOR`) | Yes for token | Query |
| `userId` | `Long` | Yes for token | Query |
| `roomStatus` | Enum | Derived | DB |
| `participantIdentity` | `String` | Derived | Webhook |

**Step-by-step user flow**
1. Create a session with `POST /api/video-call/session/{appointmentId}`.
2. The service creates a Twilio `GROUP` room with max 2 participants.
3. A patient or doctor requests an access token via `GET /api/video-call/token/{appointmentId}?userType=...&userId=...`.
4. Twilio webhooks are stored and processed through `/api/video-call/webhook`.
5. Joining/leaving updates the `VideoCallSessions` row and creates `VideoCallEvent` records.
6. Ending the session updates room status to `COMPLETED` and attempts to complete the Twilio room.

**Business rules**
- A session is unique per appointment; existing sessions are returned rather than re-created.
- Access token issuance checks both session existence and appointment ownership.
- Participant identities must start with `PATIENT-` or `DOCTOR-` to update join state.
- Test webhooks are ignored when room SID or event type is missing or malformed.

**Color coding / UX rules**
- Backend logs use success/warning/error markers; no frontend video-call UI logic is stored in this repo.

**Cascading diagram**
```text
Appointment -> Twilio room -> access tokens -> webhook events -> session state
```

**Persistence keys**
- `video_call_sessions`, `video_call_events`, `twilio_webhook_events`

**Non-obvious logic snippet**
```java
if (!videoCallSessionsRepository.existsByAppointmentIdAndUserId(appointmentId, userId)) {
    throw new UnauthorizedAccessException("You don't have access to this appointment", userId, "Appointment");
}

return switch (userType) {
    case "PATIENT" -> videoCallSessionsRepository.findPatientAccessToken(appointmentId, userId)
            .orElseThrow(() -> new UnauthorizedAccessException(
                    "Patient access denied for this video session", userId, "VideoSession"));
    case "DOCTOR" -> videoCallSessionsRepository.findDoctorAccessToken(appointmentId, userId)
            .orElseThrow(() -> new UnauthorizedAccessException(
                    "Doctor access denied for this video session", userId, "VideoSession"));
    default -> throw new IllegalArgumentException("Invalid user type: " + userType);
};
```

## 5. Data Flow & State Management

> No frontend Context/Store/Reducer implementation exists in this workspace. The project’s stateful workflows are server-side and are persisted via JPA entities/repositories. The sections below document the actual state containers that matter operationally.

### 5.1 Authentication Session State
**Purpose:** Hold the authenticated principal and JWT-derived role claim in Spring Security.

**State shape**
```js
{
  principal: {
    userId: "<string>",
    authorities: ["ROLE_PATIENT" | "ROLE_DOCTOR"]
  },
  tokenClaims: {
    sub: "<userId>",
    role: "PATIENT" | "DOCTOR",
    iat: "<date>",
    exp: "<date>"
  }
}
```

**Exported functions**
- `JWTUtility.generateToken(String userId, String role): String`
- `JWTUtility.validateToken(String token): Claims`
- `JWTUtility.getUserIdFromToken(String token): String`
- `JWTUtility.getRoleFromToken(String token): String`
- `JwtFilter.doFilterInternal(...)`

**Data flow**
```text
Authorization header -> JwtFilter -> JWTUtility.validateToken()
  -> SecurityContextHolder -> controller Principal -> service authorization
```

### 5.2 Appointment Hold State
**Purpose:** Preserve a temporary reservation while payment is in progress.

**State shape**
```js
{
  id: 0,
  holdReference: "hold_xxxxxxxx",
  patientId: 0,
  doctorId: 0,
  date: "YYYY-MM-DD",
  startTime: "HH:mm:ss",
  reason: "string",
  expiresAt: "ISO-8601 datetime",
  createdAt: "ISO-8601 datetime",
  updatedAt: "ISO-8601 datetime"
}
```

**Exported functions**
- `AppointmentService.createAppointmentHold(...)`
- `AppointmentHoldRepository.findByHoldReference(String holdReference)`

**Data flow**
```text
Hold request -> AppointmentService -> availability validation
  -> AppointmentHold entity -> payment flow -> listener consumes hold
```

### 5.3 Password Reset Token State
**Purpose:** Support secure password reset with one-time UUID tokens.

**State shape**
```js
{
  id: 0,
  token: "uuid",
  email: "user@example.com",
  userType: "PATIENT" | "DOCTOR",
  expiryDate: "ISO-8601 datetime",
  used: false,
  createdAt: "ISO-8601 datetime",
  updatedAt: "ISO-8601 datetime"
}
```

**Exported functions**
- `PasswordResetService.requestPatientPasswordReset(...)`
- `PasswordResetService.requestDoctorPasswordReset(...)`
- `PasswordResetService.resetPassword(...)`
- `PasswordResetService.cleanupExpiredTokens()`

**Data flow**
```text
Forgot password -> token creation -> email send -> reset endpoint -> password update -> mark token used
```

### 5.4 Payment Workflow State
**Purpose:** Track order creation, webhook updates, and completion events.

**State shape**
```js
{
  id: 0,
  orderId: "string",
  status: "PENDING" | "SUCCESS" | "PAID" | "FAILED" | null,
  referenceId: "string",
  paymentMode: "string",
  transactionTime: "string",
  orderAmount: 0.00,
  patientId: 0,
  appointmentHoldReference: "string"
}
```

**Exported functions**
- `PaymentService.initiatePayment(PaymentRequest): PaymentResponse`
- `PaymentService.handleWebhook(PaymentWebhookPayload, String, String): void`
- `PaymentService.getPaymentStatus(String): String`
- `PaymentService.getPaginatedPayments(...)`

**Data flow**
```text
initiatePayment -> order row saved as PENDING -> webhook updates row -> publish event -> create appointment
```

### 5.5 Video Session State
**Purpose:** Persist Twilio room identity, access tokens, participant status, and call lifecycle.

**State shape**
```js
{
  id: 0,
  appointment: { id: 0 },
  twilioRoomSid: "string",
  twilioRoomName: "string",
  roomStatus: "CREATED" | "IN_PROGRESS" | "COMPLETED" | "FAILED",
  patientAccessToken: "string",
  doctorAccessToken: "string",
  patientJoined: false,
  doctorJoined: false,
  patientJoinedAt: null,
  doctorJoinedAt: null,
  callStartedAt: null,
  callEndedAt: null,
  recordingEnabled: false,
  recordingSid: null,
  maxParticipants: 2,
  createdAt: "ISO-8601 datetime",
  updatedAt: "ISO-8601 datetime"
}
```

**Exported functions**
- `VideoCallService.createVideoSession(Long): VideoSession`
- `VideoCallService.getVideoSession(Long): VideoSession`
- `VideoCallService.getAccessToken(Long, String, Long): String`
- `VideoCallService.endVideoSession(Long): void`
- `VideoCallService.processTwilioWebhook(TwilioWebhookEvent): void`

**Data flow**
```text
appointment -> room creation -> participant webhook -> session update -> room completion
```

### 5.6 Profile Image Upload State
**Purpose:** Handle the temporary presigned S3 url, object key generation, and confirmation state.

**State shape**
```js
{
  presignedUploadUrl: "https://[bucket-name].s3.[region].amazonaws.com/profile-images/...",
  s3ObjectKey: "profile-images/[doctor|patient]/[userId]/[timestamp]-avatar.jpg",
  expirationTimeMinutes: 15
}
```

**Exported functions**
- `AwsS3Service.generateDoctorProfileImagePresignedUrl(Long): PresignedUrlResponse`
- `AwsS3Service.generatePatientProfileImagePresignedUrl(Long): PresignedUrlResponse`
- `AwsS3Service.generateS3ObjectKey(String, Long, String): String`
- `DoctorService.patchDoctorProfileImage(Long, DoctorProfilePatchRequest): DoctorProfileImagePatchResponse`
- `PatientService.patchPatientProfileImage(Long, PatientProfilePatchRequest): PatientProfileImagePatchResponse`

**Data flow**
```text
PATCH profile request (null/remove/key) -> Doctor/Patient Service 
  -> (If null) generate key -> call S3Presigner -> return presigned upload URL
  -> (If remove) update DB to null -> return empty DTO
  -> (If key) update DB to key -> return DTO containing key
```

## 6. API Integration

### Base URLs Per Environment
| Environment | Backend Base URL | Notes |
|---|---|---|
| Local dev | `http://localhost:8080` | Default server port in `application.properties` |
| Docker | `http://localhost:8080` | Container port mapping in `docker-compose.yml` |
| Prod | `TBD` | Controlled via `BACKEND_BASE_URL` / reverse proxy |

### Auth Endpoints
| Verb | Path | Purpose |
|---|---|---|
| POST | `/api/auth/patient/register` | Register patient account |
| POST | `/api/auth/doctor/register` | Register doctor account |
| POST | `/api/auth/login/password` | Universal password login |
| POST | `/api/auth/login/totp` | Universal TOTP login |
| POST | `/api/auth/patient/forgot-password` | Request patient reset email |
| POST | `/api/auth/doctor/forgot-password` | Request doctor reset email |
| POST | `/api/auth/reset-password` | Consume reset token and set password |
| POST | `/api/auth/totp/setup` | Generate TOTP secret + QR code |
| POST | `/api/auth/totp/confirm` | Enable TOTP after code verification |
| POST | `/api/auth/totp/disable` | Disable TOTP and revert to password login |

### Endpoint Summary by Domain
| Domain | Verb | Path | Purpose |
|---|---|---|---|
| Appointment | POST | `/api/appointments/hold` | Create 15-minute appointment hold |
| Appointment | DELETE | `/api/appointments/{appointmentId}` | Cancel appointment |
| Appointment | PUT | `/api/appointments/{appointmentId}` | Update date/time for scheduled appointment |
| Appointment | GET | `/api/appointments/doctor/{doctorId}` | Paginated doctor appointments with filters |
| Appointment | GET | `/api/appointments/patient/{patientId}` | Paginated patient appointments with filters |
| Appointment | GET | `/api/appointments/availability/{doctorId}` | Available 30-minute slots for date |
| Doctor | GET | `/api/doctor/profile` | Authenticated doctor profile |
| Doctor | PUT | `/api/doctor/profile` | Update authenticated doctor profile |
| Doctor | PATCH | `/api/doctor/profile` | Request, confirm, or remove S3 profile image URL |
| Doctor | GET | `/api/doctor/search` | Free-text search |
| Doctor | GET | `/api/doctor/filter` | Filter by specialization and gender |
| Doctor | GET | `/api/doctor/{id}` | Public read-only doctor profile |
| Patient | GET | `/api/patient/profile` | Authenticated patient profile |
| Patient | PUT | `/api/patient/profile` | Update authenticated patient profile |
| Patient | PATCH | `/api/patient/profile` | Request, confirm, or remove S3 profile image URL |
| Availability | POST | `/api/availability/{doctorId}` | Set weekly availability |
| Availability | GET | `/api/availability/{doctorId}` | Get weekly availability |
| Availability | DELETE | `/api/availability/{doctorId}/{slotId}` | Delete a slot |
| Payments | POST | `/api/payments/initiate` | Start gateway order |
| Payments | POST | `/api/payments/webhook/cashfree` | Cashfree webhook |
| Payments | POST | `/api/payments/webhook/paytm` | Paytm webhook |
| Payments | GET | `/api/payments/status/{orderId}` | Read current order status |
| Payments | GET | `/api/payments/debug/orders` | Debug all payment rows |
| Payments | GET | `/api/payments/debug/config` | Debug gateway configuration |
| Payments | GET | `/api/payments/payment-details/{id}` | Paginated patient payment history |
| Video Call | POST | `/api/video-call/session/{appointmentId}` | Create Twilio room/session |
| Video Call | GET | `/api/video-call/session/{appointmentId}` | Get session |
| Video Call | GET | `/api/video-call/token/{appointmentId}` | Get access token |
| Video Call | POST | `/api/video-call/end/{appointmentId}` | End session |
| Video Call | POST | `/api/video-call/webhook` | Twilio webhook handler |

### Sample Request / Response Payloads

#### Authentication
```json
// POST /api/auth/login/password?userType=PATIENT
{
  "email": "patient@example.com",
  "password": "securePassword123"
}
```
```json
{
  "success": true,
  "token": "<jwt>",
  "role": "PATIENT",
  "userId": 1,
  "loginMethod": "PASSWORD"
}
```

#### TOTP Setup
```json
// POST /api/auth/totp/setup -> 200
{
  "qrCodeImage": "data:image/png;base64,...",
  "secret": "JBSWY3DPEHPK3PXP"
}
```

#### Profile Image Upload Management
```json
// PATCH /api/doctor/profile (Step 1: request presigned upload URL)
// Request Body:
{
  "profileImageUrl": null
}

// Response (200 OK):
{
  "presignedUploadUrl": "https://healthcare-images.s3.ap-south-1.amazonaws.com/profile-images/doctor/1/20260607011129-avatar.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&...",
  "s3ObjectKey": "profile-images/doctor/1/20260607011129-avatar.jpg",
  "expirationTimeMinutes": 15
}

// PATCH /api/doctor/profile (Step 3: confirm uploaded key after direct PUT to S3)
// Request Body:
{
  "profileImageUrl": "profile-images/doctor/1/20260607011129-avatar.jpg"
}

// Response (200 OK):
{
  "presignedUploadUrl": null,
  "s3ObjectKey": "profile-images/doctor/1/20260607011129-avatar.jpg",
  "expirationTimeMinutes": null
}

// PATCH /api/doctor/profile (Step 4: remove profile image)
// Request Body:
{
  "profileImageUrl": "remove"
}

// Response (200 OK):
{
  "presignedUploadUrl": null,
  "s3ObjectKey": null,
  "expirationTimeMinutes": null
}
```

#### Appointment Hold
```json
// POST /api/appointments/hold
{
  "patientId": 1,
  "doctorId": 2,
  "date": "2026-01-15",
  "startTime": "10:00:00",
  "reason": "Regular checkup"
}
```
```text
hold_a1b2c3d4
```

#### Payment Initiation
```json
// POST /api/payments/initiate
{
  "customerId": "1",
  "customerName": "Jane Doe",
  "customerPhone": "9876543210",
  "customerEmail": "jane@example.com",
  "amount": 500.00,
  "appointmentHoldReference": "hold_a1b2c3d4"
}
```
```json
{
  "orderId": "ORDER_1730000000000_AB12CD34",
  "paymentSessionId": "<gateway-session-token>"
}
```

#### Cashfree Webhook Normalization
```json
// Raw webhook body contains nested order/payment payload
{
  "data": {
    "order": { "order_id": "ORDER_...", "order_amount": 500.0 },
    "payment": {
      "payment_status": "SUCCESS",
      "cf_payment_id": 123456789,
      "payment_group": "UPI",
      "payment_time": "2026-01-15T10:30:00Z"
    }
  }
}
```

#### Video Call Session
```json
// POST /api/video-call/session/123
{
  "id": 1,
  "appointmentId": 123,
  "twilioRoomSid": "RMxxxx",
  "twilioRoomName": "healthcare-123",
  "roomStatus": "CREATED",
  "patientJoined": false,
  "doctorJoined": false,
  "patientAccessToken": "<jwt-like-twilio-token>",
  "doctorAccessToken": "<jwt-like-twilio-token>"
}
```

### Response Transformations
| Raw source | Normalized client shape | Where |
|---|---|---|
| JWT claims | `{ userId, role }` principal + Spring authority | `JwtFilter` |
| Cashfree nested webhook JSON | `PaymentWebhookPayload.getOrderId()/getOrderStatus()/getReferenceId()/getPaymentMode()` | `PaymentWebhookPayload` |
| `Doctor` entity | `DoctorDto`, `DoctorProfile`, `DoctorProfileById` | `DoctorService`, `DoctorController` |
| `Patient` entity | `PatientProfile` | `PatientService`, `PatientController` |
| `Appointment` entity | `PatientAppointmentResponse` | `AppointmentService` |
| `VideoCallSessions` entity | `VideoSession` | `VideoCallService` |

### Caching Strategy
| Area | Implemented? | TTL / Key Structure | In-flight Dedup / Invalidation | Notes |
|---|---|---|---|---|
| CORS preflight | Yes | 3600s via `corsConfigurationSource().setMaxAge(3600L)` | Browser-managed | Helps repeated OPTIONS traffic |
| Appointment queries | No explicit cache | N/A | N/A | Uses specifications + pagination instead |
| Payment webhook handling | No cache, but idempotency check | N/A | `findByPaymentId()` prevents duplicate appointment creation | Request dedup implemented at event level |
| Password reset token cleanup | No cache | N/A | `@Scheduled` hourly cleanup invalidates expired rows | Persistent token lifecycle, not cache |
| HikariCP pool | Yes (connection pooling) | Pool settings in properties | Pool reuse/invalidation handled by Hikari | Operational optimization, not application cache |
| Response cache | Not implemented | TBD | TBD | No Spring Cache/Redis config found |
| Performance numbers | TBD | TBD | TBD | No benchmark file or profiling report found |

### Error Handling Conventions
| Condition | HTTP status | Error code | Response shape |
|---|---:|---|---|
| Bean validation failure | 400 | `VALIDATION_ERROR` | `{ timestamp, status, error, message, fieldErrors }` |
| Illegal argument | 400 | `INVALID_ARGUMENT` | `{ timestamp, status, error, message }` |
| Resource not found | 404 | `RESOURCE_NOT_FOUND` | same shape |
| Duplicate resource | 409 | `DUPLICATE_RESOURCE` | same shape |
| Invalid credentials | 401 | `INVALID_CREDENTIALS` | same shape |
| Unauthorized access | 403 | `UNAUTHORIZED_ACCESS` | same shape |
| TOTP not enabled | 400 | `TOTP_NOT_ENABLED` | same shape |
| Invalid TOTP code | 401 | `INVALID_TOTP_CODE` | same shape |
| Token expired | 410 | `TOKEN_EXPIRED` | same shape |
| Slot not available | 409 | `SLOT_NOT_AVAILABLE` | same shape |
| Payment error | 400 | `PAYMENT_ERROR` | same shape |
| Email failure | 500 | `EMAIL_SENDING_FAILED` | same shape |
| Generic runtime error | 500 | `INTERNAL_ERROR` | same shape |

## 7. Performance Optimizations

| Optimization | Problem | Solution | Code evidence | Measured result |
|---|---|---|---|---|
| Request deduplication / idempotency | Duplicate appointment creation could happen when payment webhooks retried | `PaymentEventListener` checks `findByPaymentId()` before booking | `PaymentEventListener.java` | TBD (no benchmark recorded) |
| S3 Direct Uploads (Presigned URLs) | Routing heavy file transfers through the Java backend consumes JVM thread pool and memory | Use presigned PUT URLs to upload binaries directly from client to S3, bypassing backend | `AwsS3Service.java`, `AwsS3Config.java` | TBD |
| Payment hold reservation | Slot could be double-booked during payment latency | `AppointmentHold` reserves slot for 15 minutes before final booking | `AppointmentService.java`, `AppointmentHold.java` | TBD |
| N+1 query avoidance | Appointment and session lookups can trigger repeated lazy loads | `root.fetch(...)`, `query.distinct(true)`, and `@EntityGraph` | `AppointmentSpecifications.java`, `AppointmentRepository.java`, `VideoCallSessionsRepository.java` | TBD |
| Pagination | Large appointment/payment lists can be expensive | `Pageable`, `PagedResourcesAssembler`, and `JpaSpecificationExecutor` | Controllers + repositories | TBD |
| Async email dispatch | Email sending can block booking flow | `@Async` on appointment/password reset emails | `EmailService.java` | TBD |
| RabbitMQ offload | Appointment creation should not block webhook response | Publish `PaymentCompletedEvent` to `payment.completed` queue | `PaymentService.java`, `RabbitMQConfig.java` | TBD |
| JVM/Hibernate batching | Writes may be chatty under load | `hibernate.jdbc.batch_size=20`, ordered inserts/updates | `application-prod.properties` | TBD |
| Open-session reduction | Lazy loading via open session can hide expensive access patterns | `spring.jpa.open-in-view=false` | `application.properties` | TBD |
| Connection pooling | Repeated DB connects are wasteful | HikariCP pool tuning per profile | `application*.properties` | TBD |
| Token cleanup | Stale reset tokens accumulate | Hourly scheduled cleanup job | `PasswordResetService.java` | TBD |
| Webhook preflight caching | CORS OPTIONS traffic can repeat unnecessarily | `CorsConfiguration#setMaxAge(3600L)` | `SecurityConfig.java` | TBD |
| Logging noise reduction | Production logs can be verbose and slow | INFO/WARN levels in prod profile; rolling log settings | `application-prod.properties` | TBD |

### Not implemented / not found
- Lazy-loaded component code splitting: **not applicable**; there is no frontend bundle in this repo.
- Memoization: **not found**.
- Optimistic UI updates: **not found**.
- Virtualization: **not found**.
- Debouncing: **not found**.

## 8. Refactoring Guidelines/Coding Standards

1. **Prefer clear layered separation.**
   - Bad:
     ```java
     @RestController
     class X {
       @PostMapping("/book")
       public void book() { /* DB + email + payment logic here */ }
     }
     ```
   - Good:
     ```java
     @RestController
     class AppointmentController {
         @PostMapping("/hold")
         public ResponseEntity<?> createAppointmentHold(@RequestBody BookAppointmentRequest request) {
             return ResponseEntity.ok(appointmentService.createAppointmentHold(...));
         }
     }
     ```

2. **Use constructor injection for dependencies.**
   - Bad:
     ```java
     @Autowired
     private PaymentService paymentService;
     ```
   - Good:
     ```java
     @RequiredArgsConstructor
     class PaymentController {
         private final PaymentService paymentService;
     }
     ```

3. **Keep API contracts in DTOs or records, not entities.**
   - Bad: returning `Doctor` or `Patient` directly.
   - Good:
     ```java
     public record TotpConfirmRequest(String secret, String code) {}
     ```

4. **Validate inputs at the boundary.**
   - Bad: accepting arbitrary strings and checking late.
   - Good:
     ```java
     public record PasswordLoginRequest(
         @NotBlank @Email String email,
         @NotBlank @Size(min = 6) String password
     ) {}
     ```

5. **Use specific exceptions and centralize mappings.**
   - Bad: `throw new RuntimeException("error")`.
   - Good: domain exceptions plus `GlobalExceptionHandler`.

6. **Prefer modern Java syntax where it improves clarity.**
   - Bad: boilerplate DTO classes with only getters/setters.
   - Good: records, `switch` expressions, `Optional`, `var` where locally obvious.

7. **Keep methods small and single-purpose.**
   - Bad: one method that validates, persists, emails, and publishes events.
   - Good: separate `validateSlotNotBooked`, `validateDoctorAvailability`, `sendAppointmentConfirmation`, `handleWebhook`.

8. **Remove dead code and deprecated endpoints quickly.**
   - Bad: leaving old `@PostMapping("/book")` active alongside hold/payment flow.
   - Good: comment out deprecated mapping and return `410 GONE` with a migration hint.

9. **Avoid debug logs and sensitive values in production.**
   - Bad: logging secrets, raw tokens, or full webhook signatures.
   - Good: mask values and log only status/context.

10. **Use consistent indentation and formatting within each file.**
   - Bad: mixed tabs/spaces and inconsistent brace placement.
   - Good: preserve local file style and keep new code aligned with the surrounding module.

11. **Add onboarding clarity in comments/Javadocs for non-obvious flows.**
   - Bad: silent event listeners and magic queue names.
   - Good: Javadocs on payment, appointment, and MFA endpoints, plus comments explaining hold-based booking.

12. **Use profile-specific configuration for environment differences.**
   - Bad: hard-coding URLs, secrets, or port numbers in code.
   - Good: `application-dev.properties`, `application-docker.properties`, `application-prod.properties`, and `.env` values.

13. **Create requested markdown plans directly in `docs/` without asking first.**
    - Bad: pausing to ask whether a plan file should be created before writing it.
    - Good: create the `.md` file in `docs/` immediately, then share the path and summary.

14. **Do not re-ask implementation questions once the contract is confirmed.**
    - Bad: repeatedly asking whether a field is nullable, update-only, exposed in responses, or string/path-based after the user has already confirmed it.
    - Good: proceed directly with implementation using the confirmed contract and document the choice in the relevant plan or context file.

15. **Keep profile image patch semantics simple and consistent.**
    - For `profileImageUrl`, use a string/path value for add or update and `null` for remove.
    - Apply the same rule to both doctor and patient profile flows so the API remains predictable.

## 9. Component Reference

| File path | Purpose | Features | Props / Inputs | Internal state | Persistence | Styling notes |
|---|---|---|---|---|---|---|
| `src/main/java/com/ashwani/HealthCare/HealthCareApplication.java` | Spring Boot entrypoint | Enables async, scheduling, transaction management | `main(String[] args)` | None | None | N/A |
| `src/main/java/com/ashwani/HealthCare/Controllers/AuthController.java` | Auth endpoints | Register, universal login, forgot/reset password | Patient/Doctor entities, login DTOs | None | Delegates to auth services | N/A |
| `src/main/java/com/ashwani/HealthCare/Controllers/MfaController.java` | TOTP lifecycle endpoints | Setup, confirm, disable | JWT bearer token + TOTP DTOs | None | Delegates to `Patient` / `Doctor` rows | N/A |
| `src/main/java/com/ashwani/HealthCare/Controllers/AppointmentController.java` | Appointment APIs | Hold, cancel, update, list, slot lookup | Booking DTOs + filters | None | `appointments`, `appointment_hold` | N/A |
| `src/main/java/com/ashwani/HealthCare/Controllers/DoctorController.java` | Doctor profile/search APIs | Profile read/update, search, filter, public lookup | Principal, `DoctorProfileUpdateRequest` | None | `doctors` | N/A |
| `src/main/java/com/ashwani/HealthCare/Controllers/PatientController.java` | Patient profile APIs | Profile read/update | Principal, `PatientProfileUpdateRequest` | None | `patients` | N/A |
| `src/main/java/com/ashwani/HealthCare/Controllers/AvailabilityController.java` | Availability APIs | Set/get/delete doctor availability | Principal + `AvailabilityRequestDto` list | None | `doctoravailability` | N/A |
| `src/main/java/com/ashwani/HealthCare/Controllers/PaymentController.java` | Payment APIs | Initiate, webhook, status, debug, paginated list | `PaymentRequest`, webhook headers | None | `payments` | N/A |
| `src/main/java/com/ashwani/HealthCare/Controllers/VideoCallController.java` | Telemedicine APIs | Create/get session, token, end, webhook | appointment/user query params | None | `video_call_sessions`, `video_call_events`, `twilio_webhook_events` | N/A |
| `src/main/java/com/ashwani/HealthCare/Config/AwsS3Config.java` | AWS S3 configuration | Configures the S3 client and S3Presigner for presigned URLs | S3 credentials | None | None | N/A |
| `src/main/java/com/ashwani/HealthCare/Config/SecurityConfig.java` | Spring Security setup | CORS, stateless auth, endpoint authorization | CORS env var | None | None | N/A |
| `src/main/java/com/ashwani/HealthCare/Config/CashfreeConfig.java` | Cashfree SDK bean | Validates app ID/secret, environment selection | `cashfree.*` props | Mutable local config vars during bean init | None | N/A |
| `src/main/java/com/ashwani/HealthCare/Config/RabbitMQConfig.java` | RabbitMQ bean setup | JSON converter, RabbitAdmin, listener factory | ConnectionFactory | None | Queue broker | N/A |
| `src/main/java/com/ashwani/HealthCare/Config/TwilioConfig.java` | Twilio bootstrap | Initializes Twilio SDK on startup | Twilio env vars | Holds account SID/key/token fields | None | N/A |
| `src/main/java/com/ashwani/HealthCare/Config/AuditingConfig.java` | JPA auditing | AuditorAware from SecurityContext | SecurityContextHolder | None | `createdAt`, `updatedAt` fields | N/A |
| `src/main/java/com/ashwani/HealthCare/Config/FilterConfig.java` | Utility beans | BCrypt, ModelMapper, RestTemplate, ObjectMapper | None | None | None | N/A |
| `src/main/java/com/ashwani/HealthCare/Filter/JwtFilter.java` | Security filter | Extracts bearer token and sets authentication | HTTP headers | None | None | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Auth/AuthService.java` | Auth business logic | Register/login for both roles | Entity objects + login strings | None | `patients`, `doctors` | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Auth/MfaService.java` | TOTP business logic | Secret generation, QR generation, enable/disable, verify | `userId`, `userType`, TOTP DTOs | None | `patients`, `doctors` | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Auth/PasswordResetService.java` | Reset token workflow | Request reset, reset password, hourly cleanup | Reset DTOs | None | `password_reset_tokens` | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/AwsS3Service.java` | S3 URL generation | Generates presigned S3 upload URLs for profile images | Doctor/Patient IDs | None | None | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Doctor/DoctorService.java` | Doctor profile/query logic | Search/filter/update/profile lookup | Query strings, gender enum | None | `doctors` | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Patient/PatientService.java` | Patient profile logic | Update patient profile | Update request DTO | None | `patients` | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Availability/AvailabilityService.java` | Availability logic | Save, fetch, delete slots | Request DTO list | None | `doctoravailability` | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Appointment/AppointmentService.java` | Appointment workflow | Hold, book, list, update, cancel, slot generation | IDs, dates, times, status | None | `appointments`, `appointment_hold` | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Communication/EmailService.java` | Email notifications | Appointment confirmations, password resets | Doctor/Patient entities | None | SMTP only | HTML emails use gradient cards and CTA buttons |
| `src/main/java/com/ashwani/HealthCare/Service/Communication/VideoCallService.java` | Twilio workflow | Room creation, token issuance, lifecycle, webhook processing | appointment/user identifiers | None | Video session tables | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Payment/PaymentService.java` | Payment orchestration | Gateway selection, order creation, webhook handling, event publishing | `PaymentRequest`, webhook payload | None | `payments` | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Payment/Factory/PaymentGatewayFactory.java` | Gateway selector | Returns active gateway bean | none | None | None | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Payment/Gateway/CashfreePaymentGateway.java` | Cashfree implementation | Order creation, signature validation, config status | Cashfree properties | None | External API | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Payment/Gateway/PaytmPaymentGateway.java` | Paytm implementation | UPI transaction flow and checksum logic | Paytm properties | None | External API | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Payment/Event/PaymentEventListener.java` | Event consumer | Creates appointment from paid order | `PaymentCompletedEvent` | None | `appointments`, `appointment_hold` | N/A |
| `src/main/java/com/ashwani/HealthCare/Entity/*.java` | Persisted workflow model | Patients, doctors, appointments, payments, reset tokens, holds, video session tables | ORM fields | JPA-managed state | PostgreSQL | N/A |
| `src/main/java/com/ashwani/HealthCare/DTO/**/*.java` | API contracts | Request/response payloads and event DTOs | JSON inputs/outputs | Immutable or simple bean state | Serialized over REST / RabbitMQ | N/A |
| `src/main/resources/templates/email/*.html` | Transactional email views | Appointment confirmation, doctor notification, password reset | Thymeleaf context variables | Template-local variables | SMTP output only | Gradient cards, CTAs, and responsive blocks |

## 10. Service Layer

| File path | Purpose | Exported functions | Cache configuration objects | Response normalization helpers |
|---|---|---|---|---|
| `src/main/java/com/ashwani/HealthCare/Service/Auth/AuthService.java` | Authentication and registration | `registerPatient(Patient): String`; `loginPatient(String,String): AuthResponse`; `registerDoctor(Doctor): String`; `loginDoctor(String,String): AuthResponse`; `loginPatientWithTotp(String,String): AuthResponse`; `loginDoctorWithTotp(String,String): AuthResponse` | None | Builds `AuthResponse` and JWTs |
| `src/main/java/com/ashwani/HealthCare/Service/Auth/MfaService.java` | TOTP setup and verification | `setupTotpByUserId(Long,String): TotpSetupResponse`; `confirmTotpByUserId(Long,String,String,String): MfaResponse`; `disableTotpByUserId(Long,String): MfaResponse`; `verifyTotpCode(String,String,String): boolean` | None | Returns QR data URI and `MfaResponse` |
| `src/main/java/com/ashwani/HealthCare/Service/Auth/PasswordResetService.java` | Reset token lifecycle | `requestPatientPasswordReset(PasswordResetRequestDTO): String`; `requestDoctorPasswordReset(PasswordResetRequestDTO): String`; `resetPassword(PasswordResetDTO): String`; `cleanupExpiredTokens(): void` | None | None |
| `src/main/java/com/ashwani/HealthCare/Service/AwsS3Service.java` | S3 URL generation | `generateDoctorProfileImagePresignedUrl(Long): PresignedUrlResponse`; `generatePatientProfileImagePresignedUrl(Long): PresignedUrlResponse`; `generateS3ObjectKey(String,Long,String): String` | None | None |
| `src/main/java/com/ashwani/HealthCare/Service/Doctor/DoctorService.java` | Doctor search and profile updates | `searchDoctors(String,String,Gender): List<DoctorDto>`; `updateDoctorProfile(Long,DoctorProfileUpdateRequest): DoctorProfile`; `patchDoctorProfileImage(Long,DoctorProfilePatchRequest): DoctorProfileImagePatchResponse`; `getDoctorProfileById(Long): DoctorProfileById` | None | `convertToDto(Doctor)`, `DoctorProfileById` construction |
| `src/main/java/com/ashwani/HealthCare/Service/Patient/PatientService.java` | Patient profile updates | `updatePatientProfile(Long,PatientProfileUpdateRequest): PatientProfile`; `patchPatientProfileImage(Long,PatientProfilePatchRequest): PatientProfileImagePatchResponse` | None | `ModelMapper` to `PatientProfile` |
| `src/main/java/com/ashwani/HealthCare/Service/Availability/AvailabilityService.java` | Availability persistence | `getDoctorAvailability(Long): List<AvailabilityResponseDto>`; `setAvailability(Long,List<AvailabilityRequestDto>): List<AvailabilityResponseDto>`; `deleteAvailabilitySlot(Long,Long): void` | None | `convertToResponse(DoctorAvailability)` |
| `src/main/java/com/ashwani/HealthCare/Service/Appointment/AppointmentService.java` | Appointment workflow | `createAppointmentHold(...)`; `bookAppointment(...)`; `getPatientAppointments(...)`; `getDoctorAppointments(...)`; `getAvailableSlots(...)`; `cancelAppointment(...)`; `updateAppointment(...)` | None | `convertToResponse(Appointment)`, `processAppointmentPage(...)` |
| `src/main/java/com/ashwani/HealthCare/Service/Communication/EmailService.java` | Email delivery | `sendAppointmentConfirmation(...)`; `sendPasswordResetEmail(...)` | None | Thymeleaf `Context` rendering, join-link generation |
| `src/main/java/com/ashwani/HealthCare/Service/Communication/VideoCallService.java` | Twilio room lifecycle | `createVideoSession(Long): VideoSession`; `getVideoSession(Long): VideoSession`; `getAccessToken(Long,String,Long): String`; `endVideoSession(Long): void`; `handleParticipantJoined(...)`; `handleParticipantLeft(...)`; `processTwilioWebhook(TwilioWebhookEvent): void` | None | `mapToVideoSession(VideoCallSessions)` |
| `src/main/java/com/ashwani/HealthCare/Service/Payment/PaymentService.java` | Payment orchestration | `initiatePayment(PaymentRequest): PaymentResponse`; `getPaymentStatus(String): String`; `handleWebhook(PaymentWebhookPayload,String,String): void`; `getAllOrders(): List<Payment>`; `getPaginatedPayments(...)`; `getConfigStatus(): Map<String,Object>` | None | `PaymentWebhookPayload` getters normalize nested webhook data |
| `src/main/java/com/ashwani/HealthCare/Service/Payment/Factory/PaymentGatewayFactory.java` | Gateway selection | `getPaymentGateway(): PaymentGateway`; `getActiveGatewayName(): String` | None | None |
| `src/main/java/com/ashwani/HealthCare/Service/Payment/Gateway/PaymentGateway.java` | Payment gateway contract | `initiatePayment(...)`; `validateWebhookSignature(...)`; `isTestWebhook(...)`; `getConfigStatus()`; `getGatewayName()` | N/A | N/A |
| `src/main/java/com/ashwani/HealthCare/Service/Payment/Gateway/CashfreePaymentGateway.java` | Cashfree implementation | `initiatePayment(...)`; `validateWebhookSignature(...)`; `isTestWebhook(...)`; `getConfigStatus()`; `getGatewayName()` | None | `getConfigStatus()` masks secrets, `hmacSha256(...)` for signatures |
| `src/main/java/com/ashwani/HealthCare/Service/Payment/Gateway/PaytmPaymentGateway.java` | Paytm implementation | same interface methods | None | JSON body parsing and checksum generation |
| `src/main/java/com/ashwani/HealthCare/Service/Payment/Event/PaymentEventListener.java` | RabbitMQ consumer | `handlePaymentCompletedEvent(PaymentCompletedEvent): void` | None | None |

## 11. Known Issues & Solutions

| Issue | Root cause | Workaround / Fix |
|---|---|---|
| `docs/project_context.md` would be ignored by Git | `.gitignore` contains `docs/*` | Add the exception `!docs/project_context.md` (done in this workspace) |
| README/API docs list `/api/auth/patient/login` and `/api/auth/doctor/login`, but the controller exposes universal login endpoints | Auth refactor consolidated password login into `/api/auth/login/password?userType=...` and TOTP login into `/api/auth/login/totp?userType=...` | Update docs or reintroduce legacy wrappers if backward compatibility is needed |
| README says Java 17, while the build/runtime use Java 21 | Documentation drift after the Java upgrade commit | Align README/deployment docs with `pom.xml` and `Dockerfile` |
| `pom.xml` has `spring-boot.version=3.2.5` while the parent POM is 3.4.4 | Stale/unreferenced property | Remove the unused property or standardize the version strategy |
| `Appointment.getAppointmentDateTime()` returns `LocalDateTime.now()` instead of the computed appointment datetime | Implementation bug in entity helper | Return the computed `appointmentDateTime` value instead of `now()` |
| `doctor-notification.html` contains a stray `T` after `margin: 0 auto;` | Template typo | Remove the extra character; CSS is otherwise valid |
| Paytm gateway implementation is disabled | Class is commented out and the current flow uses Cashfree | Keep Cashfree active, or enable Paytm after validating credentials and checksum logic |
| API docs mention `Gender.OTHER` / `NO_SHOW`, but code defines `NON_BINARY`, `PREFER_NOT_TO_SAY`, and `AppointmentStatus` without `NO_SHOW` | Documentation/model drift | Update docs to match `Enums/Gender.java` and `AppointmentStatus.java` |
| There is no frontend source in the workspace | Repository is backend-only | Treat client-side storage/persistence and rendering details as TBD |

## 12. Deployment & Development

### Status
- Local development: supported via Maven or Docker Compose
- Docker: supported via multi-stage `Dockerfile`
- Production: supported conceptually via profile files and docs, but no pipeline config is checked in
- Database migrations: manual SQL guidance exists; no Flyway/Liquibase config found

### Local Dev Setup Commands
```bash
cp env.example .env
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

### Common Local Infra
```bash
# PostgreSQL
createdb healthcare_db

# RabbitMQ (Docker)
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=rabbitmq_user \
  -e RABBITMQ_DEFAULT_PASS=rabbitmq_password \
  rabbitmq:3-management-alpine
```

### Environment Variables and Meaning
| Variable | Meaning |
|---|---|
| `DATABASE_URL` | JDBC URL for PostgreSQL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `RABBITMQ_URL` | AMQP URL for RabbitMQ |
| `RABBITMQ_USERNAME` | RabbitMQ username |
| `RABBITMQ_PASSWORD` | RabbitMQ password |
| `MAIL_FROM_DO_NOT_REPLY` | One-way "From" email address for sending transactional messages |
| `MAIL_SUPPORT` | Support email address |
| `JWT_SECRET` | JWT signing secret |
| `CASHFREE_ENV` | `SANDBOX` or `PRODUCTION` |
| `APP_ID` | Cashfree App ID |
| `SECRET_KEY` | Cashfree secret key |
| `FRONTEND_BASE_URL` | Used for email links and payment return URLs |
| `BACKEND_BASE_URL` | Used for webhook/callback URLs |
| `TWILIO_ACCOUNT_SID` | Twilio account SID |
| `TWILIO_AUTH_TOKEN` | Twilio auth token |
| `TWILIO_API_KEY` | Twilio API key |
| `TWILIO_API_SECRET` | Twilio API secret |
| `AWS_ACCESS_KEY_ID` | AWS access key ID |
| `AWS_SECRET_ACCESS_KEY` | AWS secret access key |
| `AWS_S3_BUCKET_NAME` | AWS S3 bucket name |
| `AWS_REGION` | AWS region (e.g. ap-south-1) |
| `CORS_ALLOWED_ORIGINS` | Allowed browser origins |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile |
| `SPRING_PROFILE` | Docker compose helper profile variable |

### Build Commands
```bash
mvn clean package
java -jar target/HealthCare-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Docker Commands
```bash
docker-compose up -d --build
docker-compose ps
docker-compose logs -f healthcare-app
docker-compose down
```

### Deployment Targets and Pipelines
- **Container image:** multi-stage build in `Dockerfile`
- **Local orchestration:** `docker-compose.yml` (app + RabbitMQ)
- **Manual JVM deploy:** `java -jar ...` under `dev`, `docker`, or `prod` profiles
- **Cloud examples in docs:** AWS EC2/RDS, GCP App Engine, Azure App Service
- **CI/CD pipeline file:** TBD (not present in workspace)

### Database Migration Commands
- No Flyway/Liquibase toolchain is configured.
- Runtime schema mode is profile-dependent:
  - `dev`: `spring.jpa.hibernate.ddl-auto=update`
  - `docker` / `prod`: `validate`
- Manual SQL snippets are documented in `DATABASE_SCHEMA.md`.

### Tests, Linters, Type Checks
```bash
mvn test
mvn -DskipTests compile
mvn clean package
```
- Linters: TBD / not configured in `pom.xml`
- Static type check: `mvn compile` (Java compile + annotation processing)
- Security scan example from docs: `mvn org.owasp:dependency-check-maven:check` (not configured by default)

## 13. Appendix: File Reference

| Path | Description |
|---|---|
| `API_DOCUMENTATION.md` | Detailed endpoint and payload guide |
| `CHANGELOG.md` | Human-readable release history |
| `DATABASE_SCHEMA.md` | Schema definitions, indexes, and migration SQL |
| `DEVELOPER_GUIDE.md` | Coding standards, patterns, testing, and troubleshooting |
| `DEPLOYMENT_GUIDE.md` | Local, Docker, cloud, and production deployment steps |
| `Dockerfile` | Multi-stage container image definition |
| `DOCUMENTATION_INDEX.md` | Index of all project documentation |
| `README.md` | High-level overview and quick start |
| `PAYMENT_API_GUIDE.md` | Payment pagination/filtering notes |
| `QUICK_REFERENCE.md` | Copy/paste commands and endpoint cheat sheet |
| `docker-compose.yml` | App + RabbitMQ local orchestration |
| `env.example` | Environment-variable template |
| `src/main/java/com/ashwani/HealthCare/HealthCareApplication.java` | Spring Boot entrypoint |
| `src/main/java/com/ashwani/HealthCare/Config/AwsS3Config.java` | AWS S3 configuration |
| `src/main/java/com/ashwani/HealthCare/Config/AuditingConfig.java` | JPA auditing configuration |
| `src/main/java/com/ashwani/HealthCare/Config/CashfreeConfig.java` | Cashfree SDK bean configuration |
| `src/main/java/com/ashwani/HealthCare/Config/FilterConfig.java` | BCrypt, ModelMapper, RestTemplate, ObjectMapper beans |
| `src/main/java/com/ashwani/HealthCare/Config/RabbitMQConfig.java` | RabbitMQ JSON and listener configuration |
| `src/main/java/com/ashwani/HealthCare/Config/SecurityConfig.java` | CORS and security filter chain |
| `src/main/java/com/ashwani/HealthCare/Config/TwilioConfig.java` | Twilio SDK initialization |
| `src/main/java/com/ashwani/HealthCare/Controllers/AppointmentController.java` | Appointment hold, update, cancel, list, and slot lookup |
| `src/main/java/com/ashwani/HealthCare/Controllers/AuthController.java` | Register/login/reset endpoints |
| `src/main/java/com/ashwani/HealthCare/Controllers/AvailabilityController.java` | Doctor availability endpoints |
| `src/main/java/com/ashwani/HealthCare/Controllers/DoctorController.java` | Doctor profile/search/filter endpoints |
| `src/main/java/com/ashwani/HealthCare/Controllers/MfaController.java` | TOTP setup/confirm/disable endpoints |
| `src/main/java/com/ashwani/HealthCare/Controllers/PatientController.java` | Patient profile endpoints |
| `src/main/java/com/ashwani/HealthCare/Controllers/PaymentController.java` | Payment and webhook endpoints |
| `src/main/java/com/ashwani/HealthCare/Controllers/VideoCallController.java` | Twilio session/token/webhook endpoints |
| `src/main/java/com/ashwani/HealthCare/DTO/Doctor/DoctorProfileImagePatchResponse.java` | DTO for doctor profile image response |
| `src/main/java/com/ashwani/HealthCare/DTO/Doctor/DoctorProfilePatchRequest.java` | DTO for doctor profile image request |
| `src/main/java/com/ashwani/HealthCare/DTO/Patient/PatientProfileImagePatchResponse.java` | DTO for patient profile image response |
| `src/main/java/com/ashwani/HealthCare/DTO/Patient/PatientProfilePatchRequest.java` | DTO for patient profile image request |
| `src/main/java/com/ashwani/HealthCare/DTO/**` | Other API request/response contracts and events |
| `src/main/java/com/ashwani/HealthCare/Entity/**` | Persisted domain and workflow state |
| `src/main/java/com/ashwani/HealthCare/Enums/**` | Domain enums for status, gender, and login method |
| `src/main/java/com/ashwani/HealthCare/ExceptionHandlers/GlobalExceptionHandler.java` | Central error mapping to JSON |
| `src/main/java/com/ashwani/HealthCare/Filter/JwtFilter.java` | JWT request authentication filter |
| `src/main/java/com/ashwani/HealthCare/Repository/**` | Spring Data repositories and custom queries |
| `src/main/java/com/ashwani/HealthCare/Service/AwsS3Service.java` | AWS S3 service (presigned URL generation) |
| `src/main/java/com/ashwani/HealthCare/Service/**` | Business logic, integrations, and event handling |
| `src/main/java/com/ashwani/HealthCare/Utility/JWTUtility.java` | JWT generation and validation helper |
| `src/main/java/com/ashwani/HealthCare/Utility/TimeSlot.java` | Available-slot value object |
| `src/main/java/com/ashwani/HealthCare/specifications/**` | Dynamic query predicates |
| `src/main/resources/application.properties` | Base runtime configuration |
| `src/main/resources/application-dev.properties` | Development profile overrides |
| `src/main/resources/application-docker.properties` | Docker profile overrides |
| `src/main/resources/application-prod.properties` | Production profile overrides |
| `src/main/resources/templates/email/patient-appointment.html` | Patient appointment confirmation email |
| `src/main/resources/templates/email/doctor-notification.html` | Doctor notification email |
| `src/main/resources/templates/email/password-reset.html` | Password reset email |
| `src/test/java/com/ashwani/HealthCare/HealthCareApplicationTests.java` | Spring context smoke test |
| `docs/project_context.md` | This authoritative project context document |


