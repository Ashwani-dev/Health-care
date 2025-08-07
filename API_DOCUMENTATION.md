# Healthcare Management System - API Documentation

## Base URL
```
http://localhost:8080/api
```

## Authentication
All protected endpoints require a valid JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## 🔐 Authentication Endpoints

### Register User
**POST** `/auth/register`

Register a new user (doctor or patient).

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "role": "DOCTOR" // or "PATIENT"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "email": "user@example.com",
  "role": "DOCTOR",
  "message": "User registered successfully"
}
```

### Login User
**POST** `/auth/login`

Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_here",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "email": "user@example.com",
    "role": "DOCTOR"
  }
}
```

### Refresh Token
**POST** `/auth/refresh`

Refresh expired JWT token.

**Request Body:**
```json
{
  "refreshToken": "refresh_token_here"
}
```

**Response (200 OK):**
```json
{
  "token": "new_jwt_token_here",
  "expiresIn": 86400000
}
```

---

## 🏥 Appointment Endpoints

### Book Appointment
**POST** `/appointments/book`

Book a new appointment with a doctor.

**Request Body:**
```json
{
  "patientId": 1,
  "doctorId": 2,
  "date": "2024-01-15",
  "startTime": "10:00:00",
  "reason": "Regular checkup"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "patientId": 1,
  "doctorId": 2,
  "appointmentDate": "2024-01-15",
  "startTime": "10:00:00",
  "endTime": "10:30:00",
  "status": "SCHEDULED",
  "reason": "Regular checkup",
  "createdAt": "2024-01-10T10:00:00Z"
}
```

### Cancel Appointment
**DELETE** `/appointments/{appointmentId}`

Cancel an existing appointment.

**Response (200 OK):**
```json
{
  "message": "Appointment cancelled successfully",
  "appointmentId": 1
}
```

**Response (400 Bad Request) - Already Cancelled:**
```json
{
  "error": "Appointment already cancelled",
  "appointmentId": 1,
  "status": "CANCELLED"
}
```

### Get Doctor Appointments
**GET** `/appointments/doctor/{doctorId}`

Get all appointments for a specific doctor with pagination and filtering.

**Query Parameters:**
- `appointmentDate` (optional): Filter by date (ISO format: YYYY-MM-DD)
- `startTime` (optional): Filter by start time (ISO format: HH:MM:SS)
- `status` (optional): Filter by status (SCHEDULED, COMPLETED, CANCELLED)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort field (default: appointmentDate,asc)

**Response (200 OK):**
```json
{
  "_embedded": {
    "patientAppointmentResponseList": [
      {
        "id": 1,
        "patientName": "John Doe",
        "patientEmail": "john@example.com",
        "appointmentDate": "2024-01-15",
        "startTime": "10:00:00",
        "endTime": "10:30:00",
        "status": "SCHEDULED",
        "reason": "Regular checkup"
      }
    ]
  },
  "_links": {
    "self": { "href": "/api/appointments/doctor/2?page=0&size=20" },
    "next": { "href": "/api/appointments/doctor/2?page=1&size=20" }
  },
  "page": {
    "size": 20,
    "totalElements": 50,
    "totalPages": 3,
    "number": 0
  }
}
```

### Get Patient Appointments
**GET** `/appointments/patient/{patientId}`

Get all appointments for a specific patient with pagination and filtering.

**Query Parameters:** Same as doctor appointments

**Response (200 OK):**
```json
{
  "_embedded": {
    "patientAppointmentResponseList": [
      {
        "id": 1,
        "doctorName": "Dr. Smith",
        "doctorEmail": "smith@example.com",
        "appointmentDate": "2024-01-15",
        "startTime": "10:00:00",
        "endTime": "10:30:00",
        "status": "SCHEDULED",
        "reason": "Regular checkup"
      }
    ]
  },
  "_links": {
    "self": { "href": "/api/appointments/patient/1?page=0&size=20" }
  },
  "page": {
    "size": 20,
    "totalElements": 10,
    "totalPages": 1,
    "number": 0
  }
}
```

### Get Available Time Slots
**GET** `/appointments/availability/{doctorId}`

Get available time slots for a doctor on a specific date.

**Query Parameters:**
- `date` (required): Date in ISO format (YYYY-MM-DD)

**Response (200 OK):**
```json
[
  {
    "startTime": "09:00:00",
    "endTime": "09:30:00",
    "available": true
  },
  {
    "startTime": "09:30:00",
    "endTime": "10:00:00",
    "available": false
  },
  {
    "startTime": "10:00:00",
    "endTime": "10:30:00",
    "available": true
  }
]
```

---

## 👨‍⚕️ Doctor Endpoints

### Get All Doctors
**GET** `/doctors`

Get all doctors with pagination and filtering.

**Query Parameters:**
- `specialization` (optional): Filter by specialization
- `name` (optional): Filter by doctor name
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

**Response (200 OK):**
```json
{
  "_embedded": {
    "doctorDtoList": [
      {
        "id": 1,
        "name": "Dr. John Smith",
        "email": "smith@example.com",
        "specialization": "Cardiology",
        "experience": 10,
        "rating": 4.5,
        "consultationFee": 100.00
      }
    ]
  },
  "_links": {
    "self": { "href": "/api/doctors?page=0&size=20" }
  },
  "page": {
    "size": 20,
    "totalElements": 50,
    "totalPages": 3,
    "number": 0
  }
}
```

### Get Doctor by ID
**GET** `/doctors/{id}`

Get detailed information about a specific doctor.

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Dr. John Smith",
  "email": "smith@example.com",
  "phone": "+1234567890",
  "specialization": "Cardiology",
  "experience": 10,
  "rating": 4.5,
  "consultationFee": 100.00,
  "bio": "Experienced cardiologist with expertise in...",
  "address": "123 Medical Center Dr, City, State"
}
```

### Update Doctor Profile
**PUT** `/doctors/{id}`

Update doctor profile information.

**Request Body:**
```json
{
  "name": "Dr. John Smith",
  "phone": "+1234567890",
  "specialization": "Cardiology",
  "experience": 10,
  "consultationFee": 100.00,
  "bio": "Updated bio information",
  "address": "123 Medical Center Dr, City, State"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "Dr. John Smith",
  "email": "smith@example.com",
  "phone": "+1234567890",
  "specialization": "Cardiology",
  "experience": 10,
  "consultationFee": 100.00,
  "bio": "Updated bio information",
  "address": "123 Medical Center Dr, City, State",
  "message": "Profile updated successfully"
}
```

### Set Doctor Availability
**POST** `/doctors/availability`

Set doctor's availability for specific dates and time slots.

**Request Body:**
```json
{
  "doctorId": 1,
  "date": "2024-01-15",
  "startTime": "09:00:00",
  "endTime": "17:00:00",
  "isAvailable": true
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "doctorId": 1,
  "date": "2024-01-15",
  "startTime": "09:00:00",
  "endTime": "17:00:00",
  "isAvailable": true,
  "message": "Availability set successfully"
}
```

---

## 👤 Patient Endpoints

### Get Patient by ID
**GET** `/patients/{id}`

Get detailed information about a specific patient.

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "dateOfBirth": "1990-01-01",
  "gender": "MALE",
  "address": "123 Main St, City, State",
  "medicalHistory": "No significant medical history"
}
```

### Update Patient Profile
**PUT** `/patients/{id}`

Update patient profile information.

**Request Body:**
```json
{
  "name": "John Doe",
  "phone": "+1234567890",
  "dateOfBirth": "1990-01-01",
  "gender": "MALE",
  "address": "123 Main St, City, State",
  "medicalHistory": "Updated medical history"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+1234567890",
  "dateOfBirth": "1990-01-01",
  "gender": "MALE",
  "address": "123 Main St, City, State",
  "medicalHistory": "Updated medical history",
  "message": "Profile updated successfully"
}
```

---

## 💳 Payment Endpoints

### Initiate Payment
**POST** `/payments/initiate`

Create a new payment order for an appointment.

**Request Body:**
```json
{
  "customerId": "CUST_123456",
  "customerName": "John Doe",
  "customerPhone": "+1234567890",
  "customerEmail": "patient@example.com",
  "amount": 100.00
}
```

**Response (200 OK):**
```json
{
  "orderId": "order_123456789",
  "paymentSessionId": "session_123456789"
}
```

### Payment Webhook
**POST** `/payments/webhook/cashfree`

Handle payment status updates from Cashfree.

**Headers:**
```
x-webhook-signature: <signature> (optional, configurable)
```

**Request Body:**
```json
{
  "orderId": "order_123456789",
  "orderAmount": 100.00,
  "referenceId": "ref_123456789",
  "orderStatus": "SUCCESS",
  "paymentMode": "UPI",
  "txMsg": "Transaction successful",
  "txTime": "2024-01-10T10:00:00Z"
}
```

**Response (200 OK):**
```
Webhook processed successfully
```

### Debug Orders
**GET** `/payments/debug/orders`

Get all payment orders (for debugging purposes).

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "orderId": "order_123456789",
    "status": "SUCCESS",
    "referenceId": "ref_123456789",
    "paymentMode": "UPI",
    "createdAt": "2024-01-10T10:00:00Z"
  }
]
```

---

## 📹 Video Call Endpoints

### Create Video Session
**POST** `/video/create-session`

Create a new video call session for an appointment.

**Request Body:**
```json
{
  "appointmentId": 1,
  "participantName": "Dr. Smith"
}
```

**Response (200 OK):**
```json
{
  "sessionId": "session_123456789",
  "accessToken": "twilio_access_token_here",
  "roomName": "appointment_1_room",
  "expiresAt": "2024-01-10T11:00:00Z"
}
```

### Join Video Session
**POST** `/video/join-session`

Join an existing video call session.

**Request Body:**
```json
{
  "sessionId": "session_123456789",
  "participantName": "John Doe"
}
```

**Response (200 OK):**
```json
{
  "sessionId": "session_123456789",
  "accessToken": "twilio_access_token_here",
  "roomName": "appointment_1_room",
  "expiresAt": "2024-01-10T11:00:00Z"
}
```

---

## 📊 Error Responses

### Common Error Codes

**400 Bad Request**
```json
{
  "error": "Validation failed",
  "message": "Invalid input data",
  "timestamp": "2024-01-10T10:00:00Z"
}
```

**401 Unauthorized**
```json
{
  "error": "Unauthorized",
  "message": "Invalid or expired token",
  "timestamp": "2024-01-10T10:00:00Z"
}
```

**403 Forbidden**
```json
{
  "error": "Access denied",
  "message": "Insufficient permissions",
  "timestamp": "2024-01-10T10:00:00Z"
}
```

**404 Not Found**
```json
{
  "error": "Resource not found",
  "message": "Requested resource does not exist",
  "timestamp": "2024-01-10T10:00:00Z"
}
```

**500 Internal Server Error**
```json
{
  "error": "Internal server error",
  "message": "An unexpected error occurred",
  "timestamp": "2024-01-10T10:00:00Z"
}
```

---

## 📝 Data Models

### Appointment Status Enum
```java
public enum AppointmentStatus {
    SCHEDULED,
    COMPLETED,
    CANCELLED,
    NO_SHOW
}
```

### User Role Enum
```java
public enum UserRole {
    DOCTOR,
    PATIENT,
    ADMIN
}
```

### Gender Enum
```java
public enum Gender {
    MALE,
    FEMALE,
    OTHER
}
```

---

## 🔒 Security Considerations

1. **JWT Token Expiration**: Tokens expire after 24 hours
2. **HTTPS Required**: All production endpoints should use HTTPS
3. **Input Validation**: All inputs are validated and sanitized
4. **Rate Limiting**: Consider implementing rate limiting for production
5. **Audit Logging**: All sensitive operations are logged

---

## 📞 Support

For API support and questions:
- Create an issue in the repository
- Contact the development team
- Check the application logs for detailed error information 