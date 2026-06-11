# Healthcare Management System - Changelog

All notable changes to the Healthcare Management System will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Comprehensive documentation suite with 11 documentation files
- API documentation with all current endpoints and examples
- Deployment configuration guide with profile-based setup
- Developer guidelines and coding standards
- Quick reference guide with all endpoints
- Spring Boot Actuator for health checks and monitoring
- Environment variable template (`env.example`)
- Profile-based configuration (dev, docker, prod)
- Appointment hold functionality for payment workflow
- Paginated payment endpoints with filtering
- Video call session management endpoints
- Doctor search and filter endpoints
- Availability management endpoints
- **Appointment Filtering Enhancements**:
  - Date range filtering with `appointmentStartDate` and `appointmentEndDate` parameters
  - Time range filtering with `startTime` and `endTime` parameters
  - Support for flexible querying (single date, date range, time range, or combinations)
  - Enhanced specifications for efficient database queries with JPA Criteria API

### Changed
- Updated all documentation to match current codebase
- Enhanced error handling across all endpoints
- Improved logging configuration
- **Appointment API**:
  - Updated `/api/appointments/doctor/{doctorId}` to support date and time range filtering
  - Updated `/api/appointments/patient/{patientId}` to support date and time range filtering
  - Replaced single `appointmentDate` parameter with `appointmentStartDate` and `appointmentEndDate`
  - Enhanced `startTime` parameter to work with `endTime` for time range queries
- **Authentication API**:
  - Separate endpoints for patient and doctor registration/login
  - `/api/auth/patient/register`, `/api/auth/patient/login`
  - `/api/auth/doctor/register`, `/api/auth/doctor/login`
- **Payment API**:
  - Endpoint: `/api/payments/initiate`
  - Webhook: `/api/payments/webhook/cashfree`
  - Added `/api/payments/status/{orderId}`
  - Added `/api/payments/payment-details/{id}` with pagination
- **Video Call API**:
  - Updated to `/api/video-call/session/{appointmentId}`
  - Added `/api/video-call/token/{appointmentId}`
  - Added `/api/video-call/end/{appointmentId}`
  - Added `/api/video-call/webhook`
- **Doctor API**:
  - Changed to `/api/doctor/profile` (authenticated)
  - Added `/api/doctor/search` and `/api/doctor/filter`
- **Patient API**:
  - Changed to `/api/patient/profile` (authenticated)
- **Availability API**:
  - New endpoints: `/api/availability/{doctorId}`
  - Added delete slot endpoint

### Fixed
- Documentation formatting and consistency across all files
- API endpoint paths to match actual implementation
- Environment variable references
- Configuration file organization

---

## [1.0.0] - 2024-01-15

### Added
- **Core Application Features**
  - User authentication and authorization with JWT
  - Doctor and patient registration and profile management
  - Appointment booking and management system
  - Doctor availability scheduling
  - Payment integration with Cashfree
  - Video calling integration with Twilio
  - Email notification system

- **API Endpoints**
  - Authentication endpoints (register, login, refresh token)
  - Appointment management (book, cancel, view appointments)
  - Doctor management (CRUD operations, availability)
  - Patient management (profile operations)
  - Payment processing (create orders, webhook handling)
  - Video call session management

- **Technical Features**
  - Spring Boot 3.4.4 with Java 17
  - PostgreSQL database with JPA/Hibernate
  - Spring Security with JWT authentication
  - RESTful API with HATEOAS support
  - Pagination for large datasets
  - Input validation and error handling
  - Comprehensive logging system

- **Security Features**
  - JWT-based authentication
  - Role-based access control
  - Input validation and sanitization
  - SQL injection prevention
  - XSS protection

- **Integration Features**
  - Cashfree paymentEntity gateway integration
  - Twilio video calling integration
  - Email service with Thymeleaf templates
  - Database connection pooling (HikariCP)

### Technical Specifications
- **Backend**: Spring Boot 3.4.4, Java 17
- **Database**: PostgreSQL 12+
- **Security**: Spring Security, JWT
- **Payment**: Cashfree Payment Gateway
- **Video Calling**: Twilio Video API
- **Email**: Spring Mail with Thymeleaf
- **Build Tool**: Maven
- **Architecture**: Layered architecture with DTO pattern

### Database Schema
- `users` - User authentication and roles
- `doctor_entities` - Doctor information and profiles
- `patient_entities` - Patient information and profiles
- `appointment_entities` - Appointment records
- `doctor_availabilities` - Doctor availability schedules
- `payments` - Payment records and status
- `video_call_sessions` - Video call session management
- `video_call_events` - Video call event tracking
- `twilio_webhook_events` - Twilio webhook event logs

---

## [0.9.0] - 2024-01-10

### Added
- Initial project setup
- Basic Spring Boot configuration
- Database entity models
- Repository layer implementation
- Service layer business logic
- Controller layer REST endpoints
- Basic authentication system

### Changed
- Project structure organization
- Package naming conventions

---

## [0.8.0] - 2024-01-05

### Added
- Project initialization
- Maven configuration
- Basic Spring Boot application
- PostgreSQL database setup

## Quarterly Refactoring & Migration History

### 2025 Q4 — Containerization, Payments, Email, and Configuration Hardening
**Motivation:** Move the backend toward deployable environments, align payment flows with webhooks, and separate profile-specific configuration.

**Before / after metrics:**
- Before: single-path configuration and weaker environment separation.
- After: `application.properties` plus `dev` / `docker` / `prod` profile overrides; `Dockerfile` and `docker-compose.yml` added; payment and email flows became profile-driven.
- Exact latency / throughput change: TBD (no benchmark report found).

**Files touched:**
- `Dockerfile`
- `docker-compose.yml`
- `env.example`
- `src/main/resources/application*.properties`
- `Service/Payment/*`
- `Service/Communication/EmailService.java`
- `Controllers/PaymentController.java`
- `README.md`, `DEPLOYMENT_GUIDE.md`, `PAYMENT_API_GUIDE.md`

**Lessons learned:**
- Profile-based configuration reduced environment drift.
- Webhooks need explicit debug and validation modes during gateway onboarding.
- Email content should be template-driven rather than hard-coded.

### 2026 Q1 — Auth Refactor, TOTP, Idempotency, and Exception Restructuring
**Motivation:** Strengthen authentication, introduce MFA, make booking safer under webhook retries, and centralize error handling.

**Before / after metrics:**
- Before: password-only auth, less structured exception handling, direct booking flow, and weaker retry safety.
- After: TOTP flows, `GlobalExceptionHandler`, hold-based booking, and `PaymentEventListener` idempotency check.
- Exact before/after line delta: TBD.
- Measurable code shape today: 8 controllers, 15 services, 10 entities, 10 repositories, 24 DTOs.

**Files touched:**
- `Controllers/AuthController.java`
- `Controllers/MfaController.java`
- `Service/Auth/AuthService.java`
- `Service/Auth/MfaService.java`
- `Service/Auth/PasswordResetService.java`
- `ExceptionHandlers/GlobalExceptionHandler.java`
- `Service/Appointment/AppointmentService.java`
- `Service/Payment/Event/PaymentEventListener.java`
- `Controllers/DoctorController.java`
- `DTO/Doctor/DoctorProfile.java`
- `Entity/Patient.java`, `Entity/Doctor.java`

**Lessons learned:**
- Appointment creation must be idempotent when webhooks retry.
- MFA state belongs in the user entity and must update the login method explicitly.
- Centralized error mapping is easier to maintain than controller-local `try/catch` blocks.

### 2026 Q1 — Appointment Filtering, Rescheduling, and Metadata
**Motivation:** Make appointment retrieval scalable and add richer querying around dates, times, and status.

**Before / after metrics:**
- Before: simpler appointment queries with less filtering.
- After: date-range and time-range specification predicates, pagination, and fetch-join optimization.
- Exact latency change: TBD.

**Files touched:**
- `specifications/AppointmentSpecifications.java`
- `Controllers/AppointmentController.java`
- `Service/Appointment/AppointmentService.java`
- `Entity/Appointment.java`
- `Config/AuditingConfig.java`
- `application*.properties`

**Lessons learned:**
- Pagination plus fetch joins prevents the appointment list endpoints from becoming N+1-heavy.
- Auditing metadata should be consistent across entities to support troubleshooting.

### 2026 Q1 — Doctor Search / Filter and Profile Enrichment
**Motivation:** Improve discoverability of doctors and expose richer profile metadata.

**Before / after metrics:**
- Before: more limited doctor lookup.
- After: `search` and `filter` endpoints, plus `totpEnabled` exposure in doctor profile DTOs.
- Exact search-performance numbers: TBD.

**Files touched:**
- `Controllers/DoctorController.java`
- `Service/Doctor/DoctorService.java`
- `specifications/DoctorSpecifications.java`
- `DTO/Doctor/*`

**Lessons learned:**
- Search and filter logic is easier to maintain when expressed as JPA Specifications.
- Profile DTOs should be explicit about security-relevant flags like TOTP status.

### 2026 Q1 — Documentation Alignment
**Motivation:** Bring docs closer to the actual codebase after endpoint and profile changes.

**Before / after metrics:**
- Before: endpoint and enum examples in docs drifted from code.
- After: documentation index, API guide, and deployment guide were updated, though some drift remains.
- Exact doc-line reductions or rewrite metrics: TBD.

**Files touched:**
- `README.md`
- `DOCUMENTATION_INDEX.md`
- `API_DOCUMENTATION.md`
- `DEPLOYMENT_GUIDE.md`
- `DEVELOPER_GUIDE.md`
- `CHANGELOG.md`

**Lessons learned:**
- API docs must be regenerated or manually checked after auth or enum changes.
- A dedicated project-context document reduces future drift.

### 2026 Q2 — AWS S3 Profile Image Upload and Presigned URLs Integration
**Motivation:** Add support for uploading doctor and patient profile images securely and efficiently. By generating presigned S3 upload URLs, browser clients can upload binary data directly to S3. This avoids routing heavy file transfers through the Java backend application, saving memory and JVM resources.

**Before / after metrics:**
- Before: Profile images were not supported (or references were local placeholders in docs/DB).
- After: Complete flow for requesting S3 presigned upload URLs, confirming uploads, and removing profile image paths. Database schemas, profile responses/requests, and service layers updated.
- Exact performance latency delta: TBD.

**Files touched:**
- `src/main/java/com/ashwani/HealthCare/Config/AwsS3Config.java`
- `src/main/java/com/ashwani/HealthCare/Service/AwsS3Service.java`
- `src/main/java/com/ashwani/HealthCare/Controllers/DoctorController.java`
- `src/main/java/com/ashwani/HealthCare/Controllers/PatientController.java`
- `src/main/java/com/ashwani/HealthCare/Service/Doctor/DoctorService.java`
- `src/main/java/com/ashwani/HealthCare/Service/Patient/PatientService.java`
- `src/main/java/com/ashwani/HealthCare/DTO/Doctor/DoctorProfileImagePatchResponse.java`
- `src/main/java/com/ashwani/HealthCare/DTO/Doctor/DoctorProfilePatchRequest.java`
- `src/main/java/com/ashwani/HealthCare/DTO/Patient/PatientProfileImagePatchResponse.java`
- `src/main/java/com/ashwani/HealthCare/DTO/Patient/PatientProfilePatchRequest.java`
- `docker-compose.yml`, `env.example`, `pom.xml`, `application.properties`

**Lessons learned:**
- Offloading file uploads via S3 presigned URLs is much more performant than backend-mediated file uploading.
- Using PATCH endpoints to request, confirm, and remove profile images allows clean RESTful operations.

---

## Version History Summary

| Version | Release Date | Key Features |
|---------|-------------|--------------|
| 1.0.0   | 2024-01-15  | Full application with all core features |
| 0.9.0   | 2024-01-10  | Basic application structure and endpoints |
| 0.8.0   | 2024-01-05  | Project initialization and setup |

---

## Migration Guide

### Upgrading from 0.9.0 to 1.0.0

#### Database Changes
```sql
-- New tables added
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    order_id VARCHAR(255),
    amount DECIMAL(10,2),
    currency VARCHAR(3),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE video_call_sessions (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    session_id VARCHAR(255),
    room_name VARCHAR(255),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add indexes for performance
CREATE INDEX idx_appointment_doctor_date ON appointment_entities(doctor_id, appointment_date);
CREATE INDEX idx_appointment_patient_date ON appointment_entities(patient_id, appointment_date);
```

#### Configuration Changes
```properties
# New environment variables required
CASHFREE_APP_ID=your_app_id
CASHFREE_SECRET_KEY=your_secret_key
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
EMAIL_ID=your_email@gmail.com
EMAIL_PASSWORD=your_app_password
```

#### API Changes
- New paymentEntity endpoints added
- Video call endpoints added
- Enhanced error responses
- Improved pagination support

---

## Future Roadmap

### Version 1.1.0 (Planned)
- [ ] Real-time notifications with WebSocket
- [ ] Advanced appointment scheduling with recurring appointments
- [ ] Prescription management system
- [ ] Medical records management
- [ ] Multi-language support
- [ ] Mobile app API endpoints

### Version 1.2.0 (Planned)
- [ ] Analytics and reporting dashboard
- [ ] Advanced search and filtering
- [ ] Bulk operations support
- [ ] API rate limiting
- [ ] Enhanced security features
- [ ] Performance optimizations

### Version 2.0.0 (Planned)
- [ ] Microservices architecture
- [ ] Event-driven architecture
- [ ] Advanced caching with Redis
- [ ] Message queue integration
- [ ] Advanced monitoring and alerting
- [ ] Multi-tenant support

---

## Contributing

When contributing to this project, please:

1. Follow the existing code style and conventions
2. Add appropriate tests for new features
3. Update documentation for any API changes
4. Update this changelog with your changes
5. Use conventional commit messages

### Commit Message Format
```
type(scope): description

feat: add new appointment booking feature
fix: resolve paymentEntity webhook issue
docs: update API documentation
test: add unit tests for appointment service
refactor: improve error handling in controllers
```

---

## Support

For questions about version changes or migration issues:
- Check the [Deployment Guide](./DEPLOYMENT_GUIDE.md)
- Review the [API Documentation](./API_DOCUMENTATION.md)
- Contact the development team
- Create an issue in the repository 