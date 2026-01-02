# Healthcare Management System

A comprehensive Spring Boot application for managing healthcare appointments, doctor-patient interactions, and telemedicine services.

## 🏥 Features

### Core Functionality
- **Appointment Management**: Book, cancel, and manage appointments between patients and doctors
- **Doctor Management**: Doctor registration, profile management, and availability scheduling
- **Patient Management**: Patient registration and profile management
- **Video Calling**: Integrated telemedicine with Twilio video calling
- **Payment Processing**: Secure paymentEntity integration with Cashfree
- **Email Notifications**: Automated email notifications for appointments and updates
- **Authentication & Authorization**: JWT-based secure authentication system

### Technical Features
- **RESTful API**: Comprehensive REST API with HATEOAS support
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: Spring Security with JWT tokens
- **Pagination**: Efficient data pagination for large datasets
- **Validation**: Input validation and error handling
- **Logging**: Comprehensive application logging

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ (local or cloud)
- RabbitMQ (local or cloud)
- Node.js (for frontend, if applicable)

### Quick Setup (5 Minutes)

#### Step 1: Clone and Setup Environment

```bash
# Clone the repository
git clone <repository-url>
cd HealthCare

# Copy environment template
cp env.example .env
```

#### Step 2: Configure Environment Variables

Edit `.env` file with your values. **Minimum required variables**:

```env
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/healthcare_db
DB_USERNAME=your_username
DB_PASSWORD=your_password

# JWT Secret (generate: openssl rand -base64 32)
JWT_SECRET=your-secret-key-here

# RabbitMQ Configuration
RABBITMQ_URL=amqp://localhost:5672
RABBITMQ_USERNAME=rabbitmq_user
RABBITMQ_PASSWORD=rabbitmq_password

# Email Configuration (Gmail example)
EMAIL_ID=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

# Twilio Configuration (get from https://console.twilio.com/)
TWILIO_ACCOUNT_SID=your-account-sid
TWILIO_AUTH_TOKEN=your-auth-token
TWILIO_API_KEY=your-api-key
TWILIO_API_SECRET=your-api-secret

# Cashfree Configuration (get from https://www.cashfree.com/)
APP_ID=your-app-id
SECRET_KEY=your-secret-key

# Application URLs
FRONTEND_BASE_URL=http://localhost:5173
BACKEND_BASE_URL=http://localhost:8080
```

#### Step 3: Set Up Database

**Option A: Local PostgreSQL**
```bash
createdb healthcare_db
# Or using psql:
psql -U postgres -c "CREATE DATABASE healthcare_db;"
```

**Option B: Docker PostgreSQL**
```bash
docker run -d --name postgres \
  -e POSTGRES_DB=healthcare_db \
  -e POSTGRES_USER=healthcare_user \
  -e POSTGRES_PASSWORD=your_password \
  -p 5432:5432 \
  postgres:15-alpine
```

#### Step 4: Set Up RabbitMQ

**Docker RabbitMQ (Recommended)**
```bash
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=rabbitmq_user \
  -e RABBITMQ_DEFAULT_PASS=rabbitmq_password \
  rabbitmq:3-management-alpine
```

#### Step 5: Run Application

**Development Mode**:
```bash
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
```

**Or Build and Run**:
```bash
mvn clean package
java -jar target/HealthCare-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

#### Step 6: Verify Installation

- **Application**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **RabbitMQ Management**: http://localhost:15672 (user: `rabbitmq_user`, pass: `rabbitmq_password`)

### Docker Quick Start

```bash
# Configure environment
cp env.example .env
# Edit .env with your values

# Run with Docker Compose
docker-compose up -d --build

# Check status
docker-compose ps
docker-compose logs -f healthcare-app
```

### Configuration Profiles

The application supports three profiles:

| Profile | Use Case | Activation |
|---------|----------|------------|
| `dev` | Local development | `SPRING_PROFILES_ACTIVE=dev` |
| `docker` | Docker containers | `SPRING_PROFILES_ACTIVE=docker` |
| `prod` | Production | `SPRING_PROFILES_ACTIVE=prod` |

For detailed configuration, see [DEPLOYMENT_GUIDE.md](./DEPLOYMENT_GUIDE.md).

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/auth/patient/register` - Register a new patient
- `POST /api/auth/patient/login` - Patient login
- `POST /api/auth/doctor/register` - Register a new doctor
- `POST /api/auth/doctor/login` - Doctor login

### Appointment Endpoints
- `POST /api/appointments/book` - Book a new appointment
- `POST /api/appointments/hold` - Create appointment hold (temporary reservation)
- `DELETE /api/appointments/{appointmentId}` - Cancel an appointment
- `GET /api/appointments/doctor/{doctorId}` - Get doctor's appointments (paginated, supports date/time range filtering)
- `GET /api/appointments/patient/{patientId}` - Get patient's appointments (paginated, supports date/time range filtering)
- `GET /api/appointments/availability/{doctorId}` - Get available time slots

### Doctor Endpoints
- `GET /api/doctor/profile` - Get authenticated doctor's profile
- `PUT /api/doctor/profile` - Update authenticated doctor's profile
- `GET /api/doctor/search` - Search doctors (query: `?q=`)
- `GET /api/doctor/filter` - Filter doctors (query: `?specialization=`)

### Patient Endpoints
- `GET /api/patient/profile` - Get authenticated patient's profile
- `PUT /api/patient/profile` - Update authenticated patient's profile

### Availability Endpoints
- `POST /api/availability/{doctorId}` - Set doctor availability
- `GET /api/availability/{doctorId}` - Get doctor availability
- `DELETE /api/availability/{doctorId}/{slotId}` - Delete availability slot

### Payment Endpoints
- `POST /api/payments/initiate` - Initiate payment order
- `POST /api/payments/webhook/cashfree` - Payment webhook handler
- `GET /api/payments/status/{orderId}` - Get payment status
- `GET /api/payments/debug/orders` - Debug orders (development)
- `GET /api/payments/payment-details/{id}` - Get paginated payments for patient

### Video Call Endpoints
- `POST /api/video-call/session/{appointmentId}` - Create video call session
- `GET /api/video-call/session/{appointmentId}` - Get video call session
- `GET /api/video-call/token/{appointmentId}` - Get access token for joining
- `POST /api/video-call/end/{appointmentId}` - End video call session
- `POST /api/video-call/webhook` - Twilio webhook handler

For detailed API documentation with request/response examples, see [API_DOCUMENTATION.md](./API_DOCUMENTATION.md).

## 🏗️ Architecture

### Project Structure
```
src/main/java/com/ashwani/HealthCare/
├── Config/           # Configuration classes
├── Controllers/      # REST API controllers
├── DTO/             # Data Transfer Objects
├── Entity/          # JPA entities
├── Repository/      # Data access layer
├── Service/         # Business logic layer
├── Utility/         # Utility classes
└── ExceptionHandlers/ # Global exception handling
```

### Technology Stack
- **Backend**: Spring Boot 3.4.4, Java 17
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: Spring Security, JWT
- **Payment**: Cashfree Payment Gateway
- **Video Calling**: Twilio Video API
- **Email**: Spring Mail with Thymeleaf templates
- **Build Tool**: Maven

## 🔧 Configuration

### Database Configuration
The application uses PostgreSQL as the primary database. Configure the connection in `application.properties`:

```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
```

### Security Configuration
JWT-based authentication is configured with configurable expiration time:

```properties
jwt.secret=${JWT_SECRET}
jwt.expiration.ms=86400000
```

### Payment Configuration
Cashfree paymentEntity gateway integration:

```properties
cashfree.env=SANDBOX
cashfree.appId=${APP_ID}
cashfree.secretKey=${SECRET_KEY}
```

## 🧪 Testing

Run the test suite:
```bash
mvn test
```

## 📝 Logging

Application logs are stored in `logs/application.log` with configurable log levels.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support

For support and questions, please contact the development team or create an issue in the repository.

---

**Note**: This is a healthcare application that handles sensitive patient data. Ensure proper security measures and compliance with healthcare data protection regulations (HIPAA, GDPR, etc.) before deploying to production.