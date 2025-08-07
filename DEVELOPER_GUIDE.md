# Healthcare Management System - Developer Guide

This guide provides comprehensive information for developers working on the Healthcare Management System, including coding standards, architecture patterns, and development workflows.

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Architecture Patterns](#architecture-patterns)
3. [Coding Standards](#coding-standards)
4. [Development Workflow](#development-workflow)
5. [Testing Guidelines](#testing-guidelines)
6. [Database Design](#database-design)
7. [Security Guidelines](#security-guidelines)
8. [Performance Optimization](#performance-optimization)
9. [Troubleshooting](#troubleshooting)

---

## 🏗️ Project Overview

### Technology Stack
- **Backend Framework**: Spring Boot 3.4.4
- **Language**: Java 17
- **Database**: PostgreSQL with JPA/Hibernate
- **Security**: Spring Security with JWT
- **Build Tool**: Maven
- **Payment Gateway**: Cashfree
- **Video Calling**: Twilio
- **Email**: Spring Mail with Thymeleaf

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
├── Filter/          # Security filters
├── ExceptionHandlers/ # Global exception handling
└── specifications/  # JPA specifications
```

---

## 🏛️ Architecture Patterns

### 1. Layered Architecture
The application follows a traditional layered architecture:

```
┌─────────────────────────────────────┐
│           Controllers               │ ← REST API Layer
├─────────────────────────────────────┤
│            Services                 │ ← Business Logic Layer
├─────────────────────────────────────┤
│          Repositories               │ ← Data Access Layer
├─────────────────────────────────────┤
│           Entities                  │ ← Data Model Layer
└─────────────────────────────────────┘
```

### 2. DTO Pattern
Use DTOs to separate internal entities from external API contracts:

```java
// Entity (Internal)
@Entity
public class DoctorEntity {
    @Id
    private Long id;
    private String name;
    private String email;
    // ... other fields
}

// DTO (External API)
public class DoctorDto {
    private Long id;
    private String name;
    private String specialization;
    private Double rating;
    // ... only necessary fields
}
```

### 3. Repository Pattern
Use Spring Data JPA repositories for data access:

```java
@Repository
public interface DoctorRepository extends JpaRepository<DoctorEntity, Long> {
    List<DoctorEntity> findBySpecialization(String specialization);
    Optional<DoctorEntity> findByEmail(String email);
}
```

### 4. Service Layer Pattern
Business logic should be in service classes:

```java
@Service
@Transactional
public class DoctorService {
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    public DoctorDto getDoctorById(Long id) {
        DoctorEntity doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        return mapToDto(doctor);
    }
}
```

---

## 📝 Coding Standards

### 1. Java Coding Standards

#### Naming Conventions
```java
// Classes: PascalCase
public class AppointmentController { }

// Methods: camelCase
public ResponseEntity<?> bookAppointment() { }

// Variables: camelCase
private String patientName;

// Constants: UPPER_SNAKE_CASE
public static final String DEFAULT_TIMEZONE = "UTC";

// Packages: lowercase
package com.ashwani.HealthCare.Controllers;
```

#### Code Organization
```java
@RestController
@RequestMapping("api/appointments")
public class AppointmentController {
    
    // 1. Dependencies (with @Autowired)
    @Autowired
    private AppointmentService appointmentService;
    
    // 2. Constants
    private static final String DEFAULT_PAGE_SIZE = "20";
    
    // 3. Public methods
    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody BookAppointmentRequest request) {
        // Implementation
    }
    
    // 4. Private helper methods
    private void validateRequest(BookAppointmentRequest request) {
        // Validation logic
    }
}
```

#### Exception Handling
```java
// Use specific exceptions
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

// Handle exceptions in controllers
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
    ErrorResponse error = new ErrorResponse("NOT_FOUND", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
}
```

### 2. Spring Boot Best Practices

#### Controller Guidelines
```java
@RestController
@RequestMapping("api/appointments")
@Validated
public class AppointmentController {
    
    // Use @Valid for request validation
    @PostMapping("/book")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request) {
        // Implementation
    }
    
    // Use proper HTTP status codes
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable Long id) {
        AppointmentResponse appointment = appointmentService.getAppointment(id);
        return ResponseEntity.ok(appointment);
    }
}
```

#### Service Guidelines
```java
@Service
@Transactional
@Slf4j
public class AppointmentService {
    
    // Use constructor injection
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    
    public AppointmentService(AppointmentRepository appointmentRepository,
                            DoctorService doctorService) {
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
    }
    
    // Use meaningful method names
    public AppointmentEntity bookAppointment(Long patientId, Long doctorId, 
                                           LocalDate date, LocalTime startTime, 
                                           String reason) {
        log.info("Booking appointment for patient: {}, doctor: {}, date: {}", 
                patientId, doctorId, date);
        
        // Business logic
        validateAppointmentRequest(patientId, doctorId, date, startTime);
        
        AppointmentEntity appointment = createAppointment(patientId, doctorId, 
                                                        date, startTime, reason);
        
        log.info("Appointment booked successfully with ID: {}", appointment.getId());
        return appointment;
    }
}
```

### 3. Database Guidelines

#### Entity Design
```java
@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "patient_id", nullable = false)
    private Long patientId;
    
    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;
    
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppointmentStatus status;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = AppointmentStatus.SCHEDULED;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

---

## 🔄 Development Workflow

### 1. Git Workflow

#### Branch Naming Convention
```
feature/appointment-booking
bugfix/paymentEntity-webhook-fix
hotfix/security-vulnerability
release/v1.2.0
```

#### Commit Message Format
```
feat: add appointment booking functionality
fix: resolve paymentEntity webhook issue
docs: update API documentation
test: add unit tests for appointment service
refactor: improve error handling in controllers
```

### 2. Development Process

#### 1. Feature Development
```bash
# Create feature branch
git checkout -b feature/new-feature

# Make changes and commit
git add .
git commit -m "feat: implement new feature"

# Push to remote
git push origin feature/new-feature

# Create pull request
# Review and merge
```

#### 2. Code Review Checklist
- [ ] Code follows coding standards
- [ ] Proper error handling implemented
- [ ] Unit tests written and passing
- [ ] Integration tests updated
- [ ] Documentation updated
- [ ] Security considerations addressed
- [ ] Performance impact assessed

### 3. Environment Setup

#### Local Development
```bash
# Clone repository
git clone <repository-url>
cd HealthCare

# Setup database
createdb healthcare_dev

# Configure environment
cp .env.example .env
# Edit .env with local settings

# Install dependencies and run
mvn clean install
mvn spring-boot:run
```

---

## 🧪 Testing Guidelines

### 1. Unit Testing

#### Service Layer Tests
```java
@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {
    
    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private DoctorService doctorService;
    
    @InjectMocks
    private AppointmentService appointmentService;
    
    @Test
    @DisplayName("Should book appointment successfully")
    void shouldBookAppointmentSuccessfully() {
        // Given
        Long patientId = 1L;
        Long doctorId = 2L;
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);
        String reason = "Regular checkup";
        
        AppointmentEntity expectedAppointment = new AppointmentEntity();
        expectedAppointment.setId(1L);
        expectedAppointment.setPatientId(patientId);
        expectedAppointment.setDoctorId(doctorId);
        expectedAppointment.setAppointmentDate(date);
        expectedAppointment.setStartTime(startTime);
        expectedAppointment.setStatus(AppointmentStatus.SCHEDULED);
        
        when(appointmentRepository.save(any(AppointmentEntity.class)))
            .thenReturn(expectedAppointment);
        
        // When
        AppointmentEntity result = appointmentService.bookAppointment(
            patientId, doctorId, date, startTime, reason);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        
        verify(appointmentRepository).save(any(AppointmentEntity.class));
    }
    
    @Test
    @DisplayName("Should throw exception when doctor is not available")
    void shouldThrowExceptionWhenDoctorNotAvailable() {
        // Given
        Long patientId = 1L;
        Long doctorId = 2L;
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);
        String reason = "Regular checkup";
        
        when(doctorService.isDoctorAvailable(doctorId, date, startTime))
            .thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> appointmentService.bookAppointment(
            patientId, doctorId, date, startTime, reason))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Doctor is not available at the specified time");
    }
}
```

#### Controller Tests
```java
@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private AppointmentService appointmentService;
    
    @Test
    @DisplayName("Should book appointment successfully")
    void shouldBookAppointmentSuccessfully() throws Exception {
        // Given
        BookAppointmentRequest request = new BookAppointmentRequest();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setDate(LocalDate.now().plusDays(1));
        request.setStartTime(LocalTime.of(10, 0));
        request.setReason("Regular checkup");
        
        AppointmentEntity appointment = new AppointmentEntity();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        when(appointmentService.bookAppointment(any(), any(), any(), any(), any()))
            .thenReturn(appointment);
        
        // When & Then
        mockMvc.perform(post("/api/appointments/book")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }
}
```

### 2. Integration Testing

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AppointmentIntegrationTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Test
    @DisplayName("Should create and retrieve appointment")
    void shouldCreateAndRetrieveAppointment() {
        // Given
        Long patientId = 1L;
        Long doctorId = 2L;
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(10, 0);
        String reason = "Regular checkup";
        
        // When
        AppointmentEntity appointment = appointmentService.bookAppointment(
            patientId, doctorId, date, startTime, reason);
        
        // Then
        assertThat(appointment).isNotNull();
        assertThat(appointment.getId()).isNotNull();
        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        
        // Verify in database
        AppointmentEntity savedAppointment = entityManager.find(
            AppointmentEntity.class, appointment.getId());
        assertThat(savedAppointment).isNotNull();
        assertThat(savedAppointment.getPatientId()).isEqualTo(patientId);
    }
}
```

### 3. Test Coverage Requirements

- **Unit Tests**: Minimum 80% line coverage
- **Integration Tests**: All critical business flows
- **API Tests**: All public endpoints
- **Security Tests**: Authentication and authorization

---

## 🗄️ Database Design

### 1. Entity Relationships

```java
// One-to-Many: Doctor to Appointments
@Entity
public class DoctorEntity {
    @OneToMany(mappedBy = "doctorId", cascade = CascadeType.ALL)
    private List<AppointmentEntity> appointments;
}

// One-to-Many: Patient to Appointments
@Entity
public class PatientEntity {
    @OneToMany(mappedBy = "patientId", cascade = CascadeType.ALL)
    private List<AppointmentEntity> appointments;
}

// One-to-Many: Doctor to Availability
@Entity
public class DoctorEntity {
    @OneToMany(mappedBy = "doctorId", cascade = CascadeType.ALL)
    private List<DoctorAvailability> availabilities;
}
```

### 2. Indexing Strategy

```java
@Entity
@Table(name = "appointments", indexes = {
    @Index(name = "idx_appointment_doctor_date", 
           columnList = "doctor_id, appointment_date"),
    @Index(name = "idx_appointment_patient_date", 
           columnList = "patient_id, appointment_date"),
    @Index(name = "idx_appointment_status", 
           columnList = "status")
})
public class AppointmentEntity {
    // Entity fields
}
```

### 3. Database Migrations

Use Flyway or Liquibase for database migrations:

```sql
-- V1__Create_appointments_table.sql
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_appointment_doctor_date ON appointments(doctor_id, appointment_date);
CREATE INDEX idx_appointment_patient_date ON appointments(patient_id, appointment_date);
```

---

## 🔒 Security Guidelines

### 1. Authentication & Authorization

```java
// JWT Token Validation
@Component
public class JwtFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String token = extractToken(request);
        
        if (token != null && jwtUtility.validateToken(token)) {
            String userId = jwtUtility.getUserIdFromToken(token);
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userId, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### 2. Input Validation

```java
// Request Validation
public class BookAppointmentRequest {
    
    @NotNull(message = "Patient ID is required")
    private Long patientId;
    
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    
    @NotNull(message = "Appointment date is required")
    @Future(message = "Appointment date must be in the future")
    private LocalDate date;
    
    @NotNull(message = "Start time is required")
    private LocalTime startTime;
    
    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;
}
```

### 3. SQL Injection Prevention

```java
// Use JPA repositories (prepared statements)
@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {
    
    // Safe - uses prepared statements
    List<AppointmentEntity> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);
    
    // Safe - uses @Query with parameters
    @Query("SELECT a FROM AppointmentEntity a WHERE a.doctorId = :doctorId AND a.status = :status")
    List<AppointmentEntity> findByDoctorIdAndStatus(@Param("doctorId") Long doctorId, 
                                                   @Param("status") AppointmentStatus status);
}
```

---

## ⚡ Performance Optimization

### 1. Database Optimization

```java
// Use pagination for large datasets
@GetMapping("/doctor/{doctorId}")
public ResponseEntity<PagedModel<EntityModel<PatientAppointmentResponse>>> 
    getDoctorAppointments(@PathVariable Long doctorId,
                         @PageableDefault(sort = "appointmentDate", direction = Sort.Direction.ASC) 
                         Pageable pageable) {
    
    Page<PatientAppointmentResponse> appointments = 
        appointmentService.getDoctorAppointments(doctorId, pageable);
    
    return ResponseEntity.ok(assembler.toModel(appointments));
}

// Use specifications for dynamic queries
public class AppointmentSpecifications {
    
    public static Specification<AppointmentEntity> hasDoctor(Long doctorId) {
        return (root, query, cb) -> cb.equal(root.get("doctorId"), doctorId);
    }
    
    public static Specification<AppointmentEntity> hasDate(LocalDate date) {
        return (root, query, cb) -> cb.equal(root.get("appointmentDate"), date);
    }
    
    public static Specification<AppointmentEntity> hasStatus(AppointmentStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
```

### 2. Caching Strategy

```java
// Service-level caching
@Service
@CacheConfig(cacheNames = "doctors")
public class DoctorService {
    
    @Cacheable(key = "#id")
    public DoctorDto getDoctorById(Long id) {
        // Implementation
    }
    
    @CacheEvict(key = "#id")
    public void updateDoctor(Long id, DoctorProfileUpdateRequest request) {
        // Implementation
    }
}

// Configuration
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("doctors"),
            new ConcurrentMapCache("patients"),
            new ConcurrentMapCache("appointments")
        ));
        return cacheManager;
    }
}
```

### 3. Connection Pooling

```properties
# application.properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

---

## 🆘 Troubleshooting

### 1. Common Issues

#### Database Connection Issues
```bash
# Check database connectivity
psql -h localhost -U healthcare_user -d healthcare_db

# Check application logs
tail -f logs/application.log | grep -i database

# Verify environment variables
echo $DATABASE_URL
echo $DB_USERNAME
echo $DB_PASSWORD
```

#### JWT Token Issues
```java
// Debug JWT token
@Component
public class JwtUtility {
    
    public void debugToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            log.info("Token claims: {}", claims);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
        }
    }
}
```

#### Performance Issues
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

### 2. Debugging Tools

#### Application Properties for Debugging
```properties
# Enable debug logging
logging.level.com.ashwani.HealthCare=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# Enable actuator endpoints
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

#### Health Check Endpoint
```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // Check database connectivity
            // Check external services
            return Health.up()
                .withDetail("database", "UP")
                .withDetail("payment_gateway", "UP")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

---

## 📚 Additional Resources

### Useful Commands
```bash
# Build application
mvn clean install

# Run tests
mvn test

# Run with specific profile
mvn spring-boot:run -Dspring.profiles.active=dev

# Generate dependency tree
mvn dependency:tree

# Check for security vulnerabilities
mvn org.owasp:dependency-check-maven:check
```

### IDE Configuration
- **IntelliJ IDEA**: Import as Maven project
- **Eclipse**: Import existing Maven project
- **VS Code**: Install Java Extension Pack

### Useful Extensions
- **Lombok**: Reduces boilerplate code
- **MapStruct**: Object mapping
- **Spring Boot DevTools**: Development utilities
- **Spring Boot Actuator**: Monitoring and metrics

---

## 📞 Support

For development questions:
1. Check the existing documentation
2. Review similar implementations in the codebase
3. Create an issue in the repository
4. Contact the development team

Remember to follow the coding standards and testing guidelines to maintain code quality and consistency across the project. 