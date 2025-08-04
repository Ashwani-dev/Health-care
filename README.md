# Healthcare Management System

A comprehensive Spring Boot application for managing healthcare appointments, doctor-patient interactions, and telemedicine services.

## 🏥 Features

### Core Functionality
- **Appointment Management**: Book, cancel, and manage appointments between patients and doctors
- **Doctor Management**: Doctor registration, profile management, and availability scheduling
- **Patient Management**: Patient registration and profile management
- **Video Calling**: Integrated telemedicine with Twilio video calling
- **Payment Processing**: Secure payment integration with Cashfree
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
- PostgreSQL 12+
- Node.js (for frontend, if applicable)

### Environment Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd HealthCare
   ```

2. **Create environment variables**
   Create a `.env` file in the root directory with the following variables:
   ```env
   # Database Configuration
   DATABASE_URL=jdbc:postgresql://localhost:5432/healthcare_db
   DB_USERNAME=your_db_username
   DB_PASSWORD=your_db_password
   
   # JWT Configuration
   JWT_SECRET=your_jwt_secret_key_here
   
   # Email Configuration
   EMAIL_ID=your_email@gmail.com
   EMAIL_PASSWORD=your_app_password
   
   # Cashfree Payment Configuration
   APP_ID=your_cashfree_app_id
   SECRET_KEY=your_cashfree_secret_key
   ```

3. **Build and run the application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the application**
   - Application URL: `http://localhost:8080`
   - API Base URL: `http://localhost:8080/api`

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh JWT token

### Appointment Endpoints
- `POST /api/appointments/book` - Book a new appointment
- `DELETE /api/appointments/{id}` - Cancel an appointment
- `GET /api/appointments/doctor/{doctorId}` - Get doctor's appointments
- `GET /api/appointments/patient/{patientId}` - Get patient's appointments
- `GET /api/appointments/availability/{doctorId}` - Get available time slots

### Doctor Endpoints
- `GET /api/doctors` - Get all doctors
- `GET /api/doctors/{id}` - Get doctor by ID
- `PUT /api/doctors/{id}` - Update doctor profile
- `POST /api/doctors/availability` - Set doctor availability

### Patient Endpoints
- `GET /api/patients/{id}` - Get patient by ID
- `PUT /api/patients/{id}` - Update patient profile

### Payment Endpoints
- `POST /api/payments/create` - Create payment order
- `POST /api/payments/webhook` - Payment webhook handler

### Video Call Endpoints
- `POST /api/video/create-session` - Create video call session
- `POST /api/video/join-session` - Join video call session

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
Cashfree payment gateway integration:

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