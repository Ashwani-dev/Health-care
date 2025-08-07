# Healthcare Management System - Quick Reference

A quick reference guide for developers working with the Healthcare Management System.

## 🚀 Quick Start Commands

### Build and Run
```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring.profiles.active=dev

# Run tests
mvn test

# Run specific test class
mvn test -Dtest=AppointmentServiceTest
```

### Database Commands
```bash
# Connect to PostgreSQL
psql -h localhost -U healthcare_user -d healthcare_db

# Create database
createdb healthcare_db

# Drop database
dropdb healthcare_db

# Backup database
pg_dump healthcare_db > backup.sql

# Restore database
psql healthcare_db < backup.sql
```

### Docker Commands
```bash
# Build and run with Docker Compose
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Rebuild and restart
docker-compose up -d --build
```

## 📋 Environment Variables

### Required Environment Variables
```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/healthcare_db
DB_USERNAME=healthcare_user
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION_MS=86400000

# Email
EMAIL_ID=your_email@gmail.com
EMAIL_PASSWORD=your_app_password

# Cashfree Payment
APP_ID=your_cashfree_app_id
SECRET_KEY=your_cashfree_secret_key

# Application
SERVER_PORT=8080
LOGGING_LEVEL=INFO
```

### Environment-Specific Files
- `.env.dev` - Development environment
- `.env.staging` - Staging environment  
- `.env.prod` - Production environment

## 🔗 API Endpoints Quick Reference

### Authentication
```
POST   /api/auth/register     - Register user
POST   /api/auth/login        - Login user
POST   /api/auth/refresh      - Refresh token
```

### Appointments
```
POST   /api/appointments/book                    - Book appointment
DELETE /api/appointments/{id}                    - Cancel appointment
GET    /api/appointments/doctor/{doctorId}       - Get doctor appointments
GET    /api/appointments/patient/{patientId}     - Get patient appointments
GET    /api/appointments/availability/{doctorId} - Get available slots
```

### Doctors
```
GET    /api/doctors                    - Get all doctors
GET    /api/doctors/{id}               - Get doctor by ID
PUT    /api/doctors/{id}               - Update doctor profile
POST   /api/doctors/availability       - Set availability
```

### Patients
```
GET    /api/patients/{id}              - Get patient by ID
PUT    /api/patients/{id}              - Update patient profile
```

### Payments
```
POST   /api/payments/create            - Create payment order
POST   /api/payments/webhook           - Payment webhook
```

### Video Calls
```
POST   /api/video/create-session       - Create video session
POST   /api/video/join-session         - Join video session
```

## 🗄️ Database Schema

### Key Tables
```sql
-- Users and authentication
users                    - User accounts
doctor_entities          - Doctor information
patient_entities         - Patient information

-- Appointments
appointment_entities     - Appointment records
doctor_availabilities    - Doctor availability schedules

-- Payments and video calls
payments                 - Payment records
video_call_sessions      - Video call sessions
video_call_events        - Video call events
twilio_webhook_events    - Twilio webhook events
```

### Common Queries
```sql
-- Get appointments for a doctor
SELECT * FROM appointment_entities 
WHERE doctor_id = ? AND appointment_date >= CURRENT_DATE
ORDER BY appointment_date, start_time;

-- Get available time slots
SELECT * FROM doctor_availabilities 
WHERE doctor_id = ? AND date = ? AND is_available = true;

-- Get payment status
SELECT * FROM payments 
WHERE appointment_id = ? ORDER BY created_at DESC LIMIT 1;
```

## 🔧 Configuration Files

### Application Properties
```properties
# Database
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update

# Security
jwt.secret=${JWT_SECRET}
jwt.expiration.ms=${JWT_EXPIRATION_MS}

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_ID}
spring.mail.password=${EMAIL_PASSWORD}

# Payment
cashfree.env=SANDBOX
cashfree.appId=${APP_ID}
cashfree.secretKey=${SECRET_KEY}

# Server
server.port=${SERVER_PORT}
server.address=0.0.0.0

# Logging
logging.level.root=${LOGGING_LEVEL}
logging.file.name=logs/application.log
```

### Logback Configuration
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

## 🧪 Testing

### Test Commands
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AppointmentServiceTest

# Run tests with coverage
mvn test jacoco:report

# Run integration tests
mvn test -Dtest=*IntegrationTest

# Run tests in parallel
mvn test -Dparallel=methods -DthreadCount=4
```

### Test Data Setup
```java
// Test data builders
public class TestDataBuilder {
    
    public static AppointmentEntity createTestAppointment() {
        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setPatientId(1L);
        appointment.setDoctorId(2L);
        appointment.setAppointmentDate(LocalDate.now().plusDays(1));
        appointment.setStartTime(LocalTime.of(10, 0));
        appointment.setEndTime(LocalTime.of(10, 30));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setReason("Test appointment");
        return appointment;
    }
    
    public static DoctorEntity createTestDoctor() {
        DoctorEntity doctor = new DoctorEntity();
        doctor.setName("Dr. Test");
        doctor.setEmail("test@example.com");
        doctor.setSpecialization("General Medicine");
        doctor.setExperience(5);
        doctor.setConsultationFee(100.0);
        return doctor;
    }
}
```

## 🔍 Debugging

### Log Levels
```properties
# Debug specific packages
logging.level.com.ashwani.HealthCare=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Debug security
logging.level.org.springframework.security=DEBUG
```

### Health Checks
```bash
# Application health
curl http://localhost:8080/actuator/health

# Database health
curl http://localhost:8080/actuator/health/db

# Custom health check
curl http://localhost:8080/actuator/health/custom
```

### Common Debug Commands
```bash
# Check running processes
ps aux | grep java

# Check port usage
netstat -tulpn | grep :8080

# Check memory usage
jstat -gc <pid>

# Check application logs
tail -f logs/application.log

# Check database connections
psql -c "SELECT * FROM pg_stat_activity WHERE datname = 'healthcare_db';"
```

## 🚨 Error Codes and Solutions

### Common Errors

#### Database Connection Error
```
Error: Could not create connection to database server
Solution: 
1. Check if PostgreSQL is running
2. Verify DATABASE_URL in environment
3. Check firewall settings
4. Verify database credentials
```

#### JWT Token Error
```
Error: JWT token is invalid or expired
Solution:
1. Check JWT_SECRET environment variable
2. Verify token expiration time
3. Check token format in Authorization header
4. Regenerate JWT secret if compromised
```

#### Payment Gateway Error
```
Error: Cashfree payment failed
Solution:
1. Verify APP_ID and SECRET_KEY
2. Check if using correct environment (SANDBOX/PRODUCTION)
3. Verify webhook URL configuration
4. Check payment amount and currency
```

#### Email Service Error
```
Error: Failed to send email
Solution:
1. Check EMAIL_ID and EMAIL_PASSWORD
2. Verify SMTP settings
3. Check if 2FA is enabled (use app password)
4. Verify email template configuration
```

## 📊 Monitoring

### Key Metrics to Monitor
```bash
# Application metrics
curl http://localhost:8080/actuator/metrics

# HTTP requests
curl http://localhost:8080/actuator/metrics/http.server.requests

# Database connections
curl http://localhost:8080/actuator/metrics/hikaricp.connections

# JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

### Performance Monitoring
```java
// Add performance logging
@Aspect
@Component
public class PerformanceAspect {
    
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object logPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        
        log.info("Method {} took {} ms", 
                joinPoint.getSignature().getName(), 
                endTime - startTime);
        
        return result;
    }
}
```

## 🔒 Security Checklist

### Pre-Deployment
- [ ] All secrets in environment variables
- [ ] HTTPS enabled in production
- [ ] JWT secret is strong and unique
- [ ] Database passwords are secure
- [ ] Input validation implemented
- [ ] SQL injection prevention
- [ ] XSS protection enabled

### Runtime Security
- [ ] Regular security updates
- [ ] Access logging enabled
- [ ] Rate limiting configured
- [ ] CORS properly configured
- [ ] Health checks monitoring
- [ ] Backup encryption

## 📞 Support Contacts

### Development Team
- **Lead Developer**: [Contact Information]
- **Backend Team**: [Contact Information]
- **DevOps Team**: [Contact Information]

### External Services
- **Cashfree Support**: [Support URL]
- **Twilio Support**: [Support URL]
- **PostgreSQL Support**: [Documentation URL]

### Emergency Contacts
- **Production Issues**: [Emergency Contact]
- **Security Issues**: [Security Contact]
- **Database Issues**: [DBA Contact]

## 📚 Useful Links

### Documentation
- [API Documentation](./API_DOCUMENTATION.md)
- [Deployment Guide](./DEPLOYMENT_GUIDE.md)
- [Developer Guide](./DEVELOPER_GUIDE.md)
- [README](./README.md)

### External Resources
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Cashfree API Documentation](https://docs.cashfree.com/)
- [Twilio Video API Documentation](https://www.twilio.com/docs/video)

### Tools
- [Postman Collection](./postman_collection.json)
- [Database Schema](./database_schema.sql)
- [Environment Templates](./env_templates/)

---

**Note**: This quick reference should be updated regularly as the application evolves. For detailed information, refer to the full documentation files. 