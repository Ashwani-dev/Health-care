# Healthcare Management System - Changelog

All notable changes to the Healthcare Management System will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Comprehensive documentation suite
- API documentation with examples
- Deployment guides for multiple environments
- Developer guidelines and coding standards
- Quick reference guide
- Debug endpoint for payment orders (`/api/payments/debug/orders`)

### Changed
- Updated README with detailed project information
- Enhanced error handling across all endpoints
- Improved logging configuration
- **Payment API Updates**:
  - Changed `/api/payments/create` to `/api/payments/initiate`
  - Updated `/api/payments/webhook` to `/api/payments/webhook/cashfree`
  - Updated payment request/response structure
  - Added webhook signature validation (configurable)

### Fixed
- Documentation formatting and consistency
- Payment webhook endpoint path consistency

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