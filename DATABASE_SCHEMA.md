# Healthcare Management System - Database Schema

This document provides a comprehensive overview of the database schema for the Healthcare Management System.

## 📋 Table of Contents

1. [Overview](#overview)
2. [Entity Relationship Diagram](#entity-relationship-diagram)
3. [Table Definitions](#table-definitions)
4. [Indexes and Performance](#indexes-and-performance)
5. [Data Types and Constraints](#data-types-and-constraints)
6. [Sample Data](#sample-data)
7. [Migration Scripts](#migration-scripts)

---

## 🏗️ Overview

The Healthcare Management System uses PostgreSQL as the primary database with the following characteristics:

- **Database Engine**: PostgreSQL 12+
- **Character Set**: UTF-8
- **Collation**: en_US.UTF-8
- **Connection Pool**: HikariCP
- **ORM**: JPA/Hibernate

### Database Statistics
- **Total Tables**: 9
- **Total Indexes**: 15
- **Total Constraints**: 25

---

## 🔗 Entity Relationship Diagram

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   users         │    │  doctor_entities │    │ patient_entities│
│                 │    │                  │    │                 │
│ id (PK)         │    │ id (PK)          │    │ id (PK)         │
│ email           │    │ name             │    │ name            │
│ password        │    │ email            │    │ email           │
│ role            │    │ specialization   │    │ phone           │
│ created_at      │    │ experience       │    │ date_of_birth   │
└─────────────────┘    │ consultation_fee │    │ gender          │
                       └──────────────────┘    └─────────────────┘
                                │                        │
                                │                        │
                                ▼                        ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │appointment_entities│   │doctor_availabilities│
                       │                  │    │                 │
                       │ id (PK)          │    │ id (PK)         │
                       │ patient_id (FK)  │    │ doctor_id (FK)  │
                       │ doctor_id (FK)   │    │ date            │
                       │ appointment_date │    │ start_time      │
                       │ start_time       │    │ end_time        │
                       │ end_time         │    │ is_available    │
                       │ status           │    └─────────────────┘
                       │ reason           │
                       └──────────────────┘
                                │
                                ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │    payments      │    │video_call_sessions│
                       │                  │    │                 │
                       │ id (PK)          │    │ id (PK)         │
                       │ appointment_id(FK)│   │ appointment_id(FK)│
                       │ order_id         │    │ session_id      │
                       │ amount           │    │ room_name       │
                       │ currency         │    │ status          │
                       │ status           │    │ created_at      │
                       │ created_at       │    └─────────────────┘
                       └──────────────────┘
                                │
                                ▼
                       ┌──────────────────┐    ┌─────────────────┐
                       │video_call_events │    │twilio_webhook_events│
                       │                  │    │                 │
                       │ id (PK)          │    │ id (PK)         │
                       │ session_id       │    │ event_type      │
                       │ event_type       │    │ event_data      │
                       │ event_data       │    │ processed_at    │
                       │ timestamp        │    │ created_at      │
                       └──────────────────┘    └─────────────────┘
```

---

## 📊 Table Definitions

### 1. users
Stores user authentication and authorization information.

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('DOCTOR', 'PATIENT', 'ADMIN')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
```

**Columns:**
- `id` - Primary key, auto-incrementing
- `email` - User's email address (unique)
- `password` - Encrypted password hash
- `role` - User role (DOCTOR, PATIENT, ADMIN)
- `created_at` - Account creation timestamp
- `updated_at` - Last update timestamp

### 2. doctor_entities
Stores doctor profile and professional information.

```sql
CREATE TABLE doctor_entities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    specialization VARCHAR(100),
    experience INTEGER,
    consultation_fee DECIMAL(10,2),
    rating DECIMAL(3,2) DEFAULT 0.0,
    bio TEXT,
    address TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_doctor_email ON doctor_entities(email);
CREATE INDEX idx_doctor_specialization ON doctor_entities(specialization);
CREATE INDEX idx_doctor_rating ON doctor_entities(rating);
```

**Columns:**
- `id` - Primary key, auto-incrementing
- `name` - Doctor's full name
- `email` - Doctor's email address (unique)
- `phone` - Contact phone number
- `specialization` - Medical specialization
- `experience` - Years of experience
- `consultation_fee` - Fee per consultation
- `rating` - Average rating (0.0 to 5.0)
- `bio` - Professional biography
- `address` - Practice address
- `created_at` - Profile creation timestamp
- `updated_at` - Last update timestamp

### 3. patient_entities
Stores patient profile and personal information.

```sql
CREATE TABLE patient_entities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    date_of_birth DATE,
    gender VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    address TEXT,
    medical_history TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Indexes
CREATE INDEX idx_patient_email ON patient_entities(email);
CREATE INDEX idx_patient_dob ON patient_entities(date_of_birth);
CREATE INDEX idx_patient_gender ON patient_entities(gender);
```

**Columns:**
- `id` - Primary key, auto-incrementing
- `name` - Patient's full name
- `email` - Patient's email address (unique)
- `phone` - Contact phone number
- `date_of_birth` - Date of birth
- `gender` - Gender (MALE, FEMALE, OTHER)
- `address` - Residential address
- `medical_history` - Medical history notes
- `created_at` - Profile creation timestamp
- `updated_at` - Last update timestamp

### 4. appointment_entities
Stores appointment scheduling and management information.

```sql
CREATE TABLE appointment_entities (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' 
        CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Foreign Key Constraints
    CONSTRAINT fk_appointment_patient 
        FOREIGN KEY (patient_id) REFERENCES patient_entities(id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_doctor 
        FOREIGN KEY (doctor_id) REFERENCES doctor_entities(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_appointment_doctor_date ON appointment_entities(doctor_id, appointment_date);
CREATE INDEX idx_appointment_patient_date ON appointment_entities(patient_id, appointment_date);
CREATE INDEX idx_appointment_status ON appointment_entities(status);
CREATE INDEX idx_appointment_datetime ON appointment_entities(appointment_date, start_time);
```

**Columns:**
- `id` - Primary key, auto-incrementing
- `patient_id` - Foreign key to patient_entities
- `doctor_id` - Foreign key to doctor_entities
- `appointment_date` - Date of appointment
- `start_time` - Appointment start time
- `end_time` - Appointment end time
- `status` - Appointment status
- `reason` - Appointment reason/notes
- `created_at` - Appointment creation timestamp
- `updated_at` - Last update timestamp

### 5. doctor_availabilities
Stores doctor availability schedules.

```sql
CREATE TABLE doctor_availabilities (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Foreign Key Constraints
    CONSTRAINT fk_availability_doctor 
        FOREIGN KEY (doctor_id) REFERENCES doctor_entities(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_availability_doctor_date ON doctor_availabilities(doctor_id, date);
CREATE INDEX idx_availability_date ON doctor_availabilities(date);
CREATE INDEX idx_availability_available ON doctor_availabilities(is_available);
```

**Columns:**
- `id` - Primary key, auto-incrementing
- `doctor_id` - Foreign key to doctor_entities
- `date` - Availability date
- `start_time` - Start time of availability
- `end_time` - End time of availability
- `is_available` - Whether the slot is available
- `created_at` - Record creation timestamp
- `updated_at` - Last update timestamp

### 6. payments
Stores paymentEntity transaction information.

```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    order_id VARCHAR(255) UNIQUE,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    transaction_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Foreign Key Constraints
    CONSTRAINT fk_payment_appointment 
        FOREIGN KEY (appointment_id) REFERENCES appointment_entities(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_payment_appointment ON payments(appointment_id);
CREATE INDEX idx_payment_order_id ON payments(order_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_created ON payments(created_at);
```

**Columns:**
- `id` - Primary key, auto-incrementing
- `appointment_id` - Foreign key to appointment_entities
- `order_id` - Payment gateway order ID (unique)
- `amount` - Payment amount
- `currency` - Payment currency (default: INR)
- `status` - Payment status
- `payment_method` - Method used for paymentEntity
- `transaction_id` - Gateway transaction ID
- `created_at` - Payment creation timestamp
- `updated_at` - Last update timestamp

### 7. video_call_sessions
Stores video call session information.

```sql
CREATE TABLE video_call_sessions (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL,
    session_id VARCHAR(255) UNIQUE,
    room_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    access_token TEXT,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Foreign Key Constraints
    CONSTRAINT fk_video_session_appointment 
        FOREIGN KEY (appointment_id) REFERENCES appointment_entities(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_video_session_appointment ON video_call_sessions(appointment_id);
CREATE INDEX idx_video_session_id ON video_call_sessions(session_id);
CREATE INDEX idx_video_session_status ON video_call_sessions(status);
```

**Columns:**
- `id` - Primary key, auto-incrementing
- `appointment_id` - Foreign key to appointment_entities
- `session_id` - Video call session ID (unique)
- `room_name` - Video call room name
- `status` - Session status
- `access_token` - Twilio access token
- `expires_at` - Token expiration time
- `created_at` - Session creation timestamp
- `updated_at` - Last update timestamp

### 8. video_call_events
Stores video call event logs.

```sql
CREATE TABLE video_call_events (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB,
    participant_name VARCHAR(255),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key Constraints
    CONSTRAINT fk_video_event_session 
        FOREIGN KEY (session_id) REFERENCES video_call_sessions(session_id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_video_event_session ON video_call_events(session_id);
CREATE INDEX idx_video_event_type ON video_call_events(event_type);
CREATE INDEX idx_video_event_timestamp ON video_call_events(timestamp);
```

**Columns:**
- `id` - Primary key, auto-incrementing
- `session_id` - Foreign key to video_call_sessions
- `event_type` - Type of event (join, leave, etc.)
- `event_data` - JSON data containing event details
- `participant_name` - Name of the participant
- `timestamp` - Event occurrence timestamp

### 9. twilio_webhook_events
Stores Twilio webhook event logs.

```sql
CREATE TABLE twilio_webhook_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    event_data JSONB NOT NULL,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_webhook_event_type ON twilio_webhook_events(event_type);
CREATE INDEX idx_webhook_processed ON twilio_webhook_events(processed_at);
CREATE INDEX idx_webhook_created ON twilio_webhook_events(created_at);
```

**Columns:**
- `id` - Primary key, auto-incrementing
- `event_type` - Type of webhook event
- `event_data` - JSON data containing webhook payload
- `processed_at` - When the event was processed
- `created_at` - Event reception timestamp

---

## ⚡ Indexes and Performance

### Primary Indexes
All tables have primary key indexes on the `id` column.

### Secondary Indexes
```sql
-- User Management
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- Doctor Management
CREATE INDEX idx_doctor_email ON doctor_entities(email);
CREATE INDEX idx_doctor_specialization ON doctor_entities(specialization);
CREATE INDEX idx_doctor_rating ON doctor_entities(rating);

-- Patient Management
CREATE INDEX idx_patient_email ON patient_entities(email);
CREATE INDEX idx_patient_dob ON patient_entities(date_of_birth);
CREATE INDEX idx_patient_gender ON patient_entities(gender);

-- Appointment Management
CREATE INDEX idx_appointment_doctor_date ON appointment_entities(doctor_id, appointment_date);
CREATE INDEX idx_appointment_patient_date ON appointment_entities(patient_id, appointment_date);
CREATE INDEX idx_appointment_status ON appointment_entities(status);
CREATE INDEX idx_appointment_datetime ON appointment_entities(appointment_date, start_time);

-- Availability Management
CREATE INDEX idx_availability_doctor_date ON doctor_availabilities(doctor_id, date);
CREATE INDEX idx_availability_date ON doctor_availabilities(date);
CREATE INDEX idx_availability_available ON doctor_availabilities(is_available);

-- Payment Management
CREATE INDEX idx_payment_appointment ON payments(appointment_id);
CREATE INDEX idx_payment_order_id ON payments(order_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_payment_created ON payments(created_at);

-- Video Call Management
CREATE INDEX idx_video_session_appointment ON video_call_sessions(appointment_id);
CREATE INDEX idx_video_session_id ON video_call_sessions(session_id);
CREATE INDEX idx_video_session_status ON video_call_sessions(status);
CREATE INDEX idx_video_event_session ON video_call_events(session_id);
CREATE INDEX idx_video_event_type ON video_call_events(event_type);
CREATE INDEX idx_video_event_timestamp ON video_call_events(timestamp);

-- Webhook Management
CREATE INDEX idx_webhook_event_type ON twilio_webhook_events(event_type);
CREATE INDEX idx_webhook_processed ON twilio_webhook_events(processed_at);
CREATE INDEX idx_webhook_created ON twilio_webhook_events(created_at);
```

### Performance Optimization
```sql
-- Analyze tables for query optimization
ANALYZE users;
ANALYZE doctor_entities;
ANALYZE patient_entities;
ANALYZE appointment_entities;
ANALYZE doctor_availabilities;
ANALYZE payments;
ANALYZE video_call_sessions;
ANALYZE video_call_events;
ANALYZE twilio_webhook_events;

-- Vacuum tables to reclaim storage
VACUUM ANALYZE users;
VACUUM ANALYZE doctor_entities;
VACUUM ANALYZE patient_entities;
VACUUM ANALYZE appointment_entities;
VACUUM ANALYZE doctor_availabilities;
VACUUM ANALYZE payments;
VACUUM ANALYZE video_call_sessions;
VACUUM ANALYZE video_call_events;
VACUUM ANALYZE twilio_webhook_events;
```

---

## 📝 Data Types and Constraints

### Data Types Used
- **BIGSERIAL** - Auto-incrementing primary keys
- **VARCHAR(n)** - Variable-length strings with max length
- **TEXT** - Unlimited length text
- **DATE** - Date values
- **TIME** - Time values
- **TIMESTAMP** - Date and time values
- **DECIMAL(p,s)** - Fixed-point decimal numbers
- **INTEGER** - 32-bit integers
- **BOOLEAN** - True/false values
- **JSONB** - Binary JSON data

### Constraints
```sql
-- Check Constraints
ALTER TABLE users ADD CONSTRAINT chk_user_role 
    CHECK (role IN ('DOCTOR', 'PATIENT', 'ADMIN'));

ALTER TABLE patient_entities ADD CONSTRAINT chk_patient_gender 
    CHECK (gender IN ('MALE', 'FEMALE', 'OTHER'));

ALTER TABLE appointment_entities ADD CONSTRAINT chk_appointment_status 
    CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW'));

-- Not Null Constraints
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users ALTER COLUMN password SET NOT NULL;
ALTER TABLE users ALTER COLUMN role SET NOT NULL;

-- Unique Constraints
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);
ALTER TABLE doctor_entities ADD CONSTRAINT uk_doctor_email UNIQUE (email);
ALTER TABLE patient_entities ADD CONSTRAINT uk_patient_email UNIQUE (email);
ALTER TABLE payments ADD CONSTRAINT uk_payment_order_id UNIQUE (order_id);
ALTER TABLE video_call_sessions ADD CONSTRAINT uk_video_session_id UNIQUE (session_id);
```

---

## 📊 Sample Data

### Sample Users
```sql
INSERT INTO users (email, password, role) VALUES
('doctor1@example.com', '$2a$10$encrypted_password_hash', 'DOCTOR'),
('doctor2@example.com', '$2a$10$encrypted_password_hash', 'DOCTOR'),
('patient1@example.com', '$2a$10$encrypted_password_hash', 'PATIENT'),
('patient2@example.com', '$2a$10$encrypted_password_hash', 'PATIENT'),
('admin@example.com', '$2a$10$encrypted_password_hash', 'ADMIN');
```

### Sample Doctors
```sql
INSERT INTO doctor_entities (name, email, phone, specialization, experience, consultation_fee, rating, bio) VALUES
('Dr. John Smith', 'doctor1@example.com', '+1234567890', 'Cardiology', 10, 150.00, 4.5, 'Experienced cardiologist with expertise in heart diseases'),
('Dr. Sarah Johnson', 'doctor2@example.com', '+1234567891', 'Dermatology', 8, 120.00, 4.8, 'Specialized in skin conditions and treatments');
```

### Sample Patients
```sql
INSERT INTO patient_entities (name, email, phone, date_of_birth, gender, address, medical_history) VALUES
('John Doe', 'patient1@example.com', '+1234567892', '1990-01-15', 'MALE', '123 Main St, City, State', 'No significant medical history'),
('Jane Smith', 'patient2@example.com', '+1234567893', '1985-05-20', 'FEMALE', '456 Oak Ave, City, State', 'Allergic to penicillin');
```

### Sample Appointments
```sql
INSERT INTO appointment_entities (patient_id, doctor_id, appointment_date, start_time, end_time, status, reason) VALUES
(1, 1, '2024-01-20', '10:00:00', '10:30:00', 'SCHEDULED', 'Regular checkup'),
(2, 2, '2024-01-21', '14:00:00', '14:30:00', 'SCHEDULED', 'Skin consultation');
```

---

## 🔄 Migration Scripts

### Initial Schema Creation
```sql
-- Create database
CREATE DATABASE healthcare_db;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create tables (see table definitions above)
-- Create indexes (see indexes section above)
-- Create constraints (see constraints section above)
```

### Schema Updates
```sql
-- Add new columns
ALTER TABLE doctor_entities ADD COLUMN IF NOT EXISTS rating DECIMAL(3,2) DEFAULT 0.0;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50);

-- Add new indexes
CREATE INDEX IF NOT EXISTS idx_doctor_rating ON doctor_entities(rating);
CREATE INDEX IF NOT EXISTS idx_payment_method ON payments(payment_method);

-- Update constraints
ALTER TABLE appointment_entities DROP CONSTRAINT IF EXISTS chk_appointment_status;
ALTER TABLE appointment_entities ADD CONSTRAINT chk_appointment_status 
    CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'NO_SHOW'));
```

### Data Migration
```sql
-- Migrate existing data
UPDATE doctor_entities SET rating = 0.0 WHERE rating IS NULL;
UPDATE payments SET payment_method = 'UNKNOWN' WHERE payment_method IS NULL;

-- Clean up old data
DELETE FROM video_call_events WHERE created_at < CURRENT_DATE - INTERVAL '90 days';
DELETE FROM twilio_webhook_events WHERE created_at < CURRENT_DATE - INTERVAL '30 days';
```

---

## 📈 Monitoring and Maintenance

### Database Statistics
```sql
-- Table sizes
SELECT 
    schemaname,
    tablename,
    attname,
    n_distinct,
    correlation
FROM pg_stats 
WHERE schemaname = 'public'
ORDER BY tablename, attname;

-- Index usage
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;

-- Table statistics
SELECT 
    schemaname,
    tablename,
    n_tup_ins,
    n_tup_upd,
    n_tup_del,
    n_live_tup,
    n_dead_tup
FROM pg_stat_user_tables
ORDER BY n_live_tup DESC;
```

### Maintenance Tasks
```sql
-- Weekly maintenance
VACUUM ANALYZE;
REINDEX DATABASE healthcare_db;

-- Monthly maintenance
VACUUM FULL;
ANALYZE;
```

---

## 🔒 Security Considerations

### Data Protection
- All sensitive data is encrypted at rest
- Passwords are hashed using BCrypt
- Database connections use SSL/TLS
- Access is restricted to application users only

### Backup Strategy
```bash
# Daily backup
pg_dump healthcare_db > backup_$(date +%Y%m%d).sql

# Weekly full backup
pg_dump -Fc healthcare_db > backup_$(date +%Y%m%d).dump

# Restore from backup
pg_restore -d healthcare_db backup_20240115.dump
```

---

## 📞 Support

For database-related questions:
- Check the [Deployment Guide](./DEPLOYMENT_GUIDE.md)
- Review the [Developer Guide](./DEVELOPER_GUIDE.md)
- Contact the database administrator
- Create an issue in the repository 