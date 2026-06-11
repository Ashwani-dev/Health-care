# Healthcare Management System - API Documentation

## Base URL
```
http://localhost:8080/api
```

## 📋 Table of Contents

1. [Authentication](#-authentication-endpoints)
   - [Traditional Authentication](#traditional-authentication)
   - [TOTP/MFA (Two-Factor Authentication)](#totp-authenticator-app-authentication)
   - [Password Reset](#password-reset)
2. [Appointment Management](#-appointment-endpoints)
3. [Doctor Management](#-doctor-endpoints)
4. [Patient Management](#-patient-endpoints)
5. [Availability Management](#-availability-endpoints)
6. [Payment Processing](#-payment-endpoints)
7. [Video Call/Telemedicine](#-video-call-endpoints)
8. [Error Responses](#-error-responses)

## Authentication
All protected endpoints require a valid JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

---

## 🔐 Authentication Endpoints

### Traditional Authentication

### Register Patient
**POST** `/api/auth/patient/register`

Register a new patient account.

**Request Body:**
```json
{
  "name": "John Doe",
  "email": "patient@example.com",
  "password": "securePassword123",
  "phone": "+1234567890",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE",
  "address": "123 Main St, City, State"
}
```

**Response (200 OK):**
```
Patient registered successfully
```

### Login Patient
**POST** `/api/auth/patient/login`

Authenticate a patient and receive JWT token.

**Request Body:**
```json
{
  "email": "patient@example.com",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "email": "patient@example.com",
    "role": "PATIENT"
  }
}
```

### Register Doctor
**POST** `/api/auth/doctor/register`

Register a new doctor account.

**Request Body:**
```json
{
  "name": "Dr. John Smith",
  "email": "doctor@example.com",
  "password": "securePassword123",
  "phone": "+1234567890",
  "specialization": "Cardiology",
  "experience": 10,
  "consultationFee": 150.00,
  "bio": "Experienced cardiologist",
  "address": "123 Medical Center Dr"
}
```

**Response (200 OK):**
```
Doctor registered successfully
```

### Login Doctor
**POST** `/api/auth/doctor/login`

Authenticate a doctor and receive JWT token.

**Request Body:**
```json
{
  "email": "doctor@example.com",
  "password": "securePassword123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "email": "doctor@example.com",
    "role": "DOCTOR"
  }
}
```

### Forgot Password - Patient
**POST** `/api/auth/patient/forgot-password`

Request a password reset for a patient account. An email will be sent with a reset link containing a token.

**Request Body:**
```json
{
  "email": "patient@example.com"
}
```

**Response (200 OK):**
```
Password reset link has been sent to your email address
```

**Error Responses:**
- `400 Bad Request` - Invalid email format
- `400 Bad Request` - No account found with this email address

### Forgot Password - Doctor
**POST** `/api/auth/doctor/forgot-password`

Request a password reset for a doctor account. An email will be sent with a reset link containing a token.

**Request Body:**
```json
{
  "email": "doctor@example.com"
}
```

**Response (200 OK):**
```
Password reset link has been sent to your email address
```

**Error Responses:**
- `400 Bad Request` - Invalid email format
- `400 Bad Request` - No account found with this email address

### Reset Password
**POST** `/api/auth/reset-password`

Reset password using the token received via email. Works for both patient and doctor accounts.

**Request Body:**
```json
{
  "token": "550e8400-e29b-41d4-a716-446655440000",
  "newPassword": "newSecurePassword123"
}
```

**Response (200 OK):**
```
Password has been reset successfully. You can now login with your new password
```

**Error Responses:**
- `400 Bad Request` - Invalid token format or password too short (minimum 6 characters)
- `400 Bad Request` - "Invalid or expired reset token"
- `400 Bad Request` - "This reset token has already been used"
- `400 Bad Request` - "This reset token has expired. Please request a new one"

**Notes:**
- Tokens expire after 60 minutes (configurable via `password.reset.token.expiry.minutes` property)
- Tokens are one-time use only
- Password must be at least 6 characters long

---

## 🔒 TOTP (Authenticator App) Authentication

### Login with Password (Universal)
**POST** `/api/auth/login/password?userType=PATIENT|DOCTOR`

Universal password login endpoint for both patients and doctors.

**Query Parameters:**
- `userType` - Either "PATIENT" or "DOCTOR"

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
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "PATIENT",
  "userId": 1,
  "loginMethod": "PASSWORD"
}
```

**Error Responses:**
- `400 Bad Request` - LOGIN_METHOD_MISMATCH: "This account uses authenticator login. Please use TOTP code."
- `401 Unauthorized` - Invalid credentials

### Login with TOTP (Universal)
**POST** `/api/auth/login/totp?userType=PATIENT|DOCTOR`

Universal TOTP login endpoint for both patients and doctors using authenticator app codes.

**Query Parameters:**
- `userType` - Either "PATIENT" or "DOCTOR"

**Request Body:**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "DOCTOR",
  "userId": 5,
  "loginMethod": "TOTP"
}
```

**Error Responses:**
- `400 Bad Request` - TOTP_NOT_ENABLED: "TOTP is not enabled for this account"
- `401 Unauthorized` - INVALID_TOTP_CODE: "Invalid TOTP code"

### Setup TOTP
**POST** `/api/auth/totp/setup`

**Authentication Required:** Yes (JWT Bearer token)

Generate a TOTP secret and QR code for the authenticated user. Scan the QR code with Google Authenticator, Microsoft Authenticator, or any TOTP-compatible app.

**Response (200 OK):**
```json
{
  "qrCodeImage": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
  "secret": "JBSWY3DPEHPK3PXP"
}
```

**Response Fields:**
- `qrCodeImage` - Base64-encoded PNG QR code (data URI format) - display this for users to scan
- `secret` - Raw secret for manual entry if QR scanning fails

**Error Responses:**
- `409 Conflict` - TOTP_ALREADY_ENABLED: "TOTP is already enabled for this account"

**Usage:**
1. Display the QR code to the user
2. User scans with authenticator app
3. User calls `/api/auth/totp/confirm` with the code from app

### Confirm TOTP Setup
**POST** `/api/auth/totp/confirm`

**Authentication Required:** Yes (JWT Bearer token)

Verify the TOTP code and enable TOTP authentication for the user. After confirmation, the user's login method changes to TOTP and password login is disabled.

**Request Body:**
```json
{
  "secret": "JBSWY3DPEHPK3PXP",
  "code": "123456"
}
```

**Response (200 OK):**
```json
{
  "message": "TOTP enabled successfully",
  "loginMethod": "TOTP"
}
```

**Error Responses:**
- `401 Unauthorized` - INVALID_TOTP_CODE: "Invalid TOTP code"

**Important:** After confirmation, user must use `/api/auth/login/totp` for future logins.

### Disable TOTP
**POST** `/api/auth/totp/disable`

**Authentication Required:** Yes (JWT Bearer token)

Disable TOTP authentication and revert to password-based login.

**Response (200 OK):**
```json
{
  "message": "TOTP disabled successfully",
  "loginMethod": "PASSWORD"
}
```

**Error Responses:**
- `400 Bad Request` - TOTP_NOT_ENABLED: "TOTP is not enabled for this account"

---

## 🏥 Appointment Endpoints

### Book Appointment
**POST** `/api/appointments/book`

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

### Create Appointment Hold
**POST** `/api/appointments/hold`

Create a temporary appointment hold (useful during payment workflow).

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
```
"hold_123456789"
```

### Cancel Appointment
**DELETE** `/api/appointments/{appointmentId}`

Cancel an existing appointment. Requires authentication and ownership verification.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

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

**Response (403 Forbidden) - Access Denied:**
```json
{
  "error": "You can only cancel your own appointments",
  "appointmentId": 1
}
```

### Get Doctor Appointments
**GET** `/api/appointments/doctor/{doctorId}`

Get all appointments for a specific doctor with pagination and filtering.

**Query Parameters:**
- `appointmentStartDate` (optional): Filter by start date (ISO format: YYYY-MM-DD)
- `appointmentEndDate` (optional): Filter by end date (ISO format: YYYY-MM-DD)
- `startTime` (optional): Filter by start time (ISO format: HH:MM:SS)
- `endTime` (optional): Filter by end time (ISO format: HH:MM:SS)
- `status` (optional): Filter by status (SCHEDULED, COMPLETED, CANCELLED)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort field (default: appointmentDate,asc)

**Example Requests:**
```
# Get all appointments for a specific date
GET /api/appointments/doctor/2?appointmentStartDate=2026-01-15&appointmentEndDate=2026-01-15

# Get appointments in a date range
GET /api/appointments/doctor/2?appointmentStartDate=2026-01-01&appointmentEndDate=2026-01-31

# Get appointments in a time range
GET /api/appointments/doctor/2?startTime=09:00:00&endTime=17:00:00

# Combine date range, time range, and status
GET /api/appointments/doctor/2?appointmentStartDate=2026-01-01&appointmentEndDate=2026-01-31&startTime=09:00:00&endTime=12:00:00&status=SCHEDULED

# Pagination
GET /api/appointments/doctor/2?page=0&size=10&sort=appointmentDate,desc
```

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
**GET** `/api/appointments/patient/{patientId}`

Get all appointments for a specific patient with pagination and filtering.

**Query Parameters:**
- `appointmentStartDate` (optional): Filter by start date (ISO format: YYYY-MM-DD)
- `appointmentEndDate` (optional): Filter by end date (ISO format: YYYY-MM-DD)
- `startTime` (optional): Filter by start time (ISO format: HH:MM:SS)
- `endTime` (optional): Filter by end time (ISO format: HH:MM:SS)
- `status` (optional): Filter by status (SCHEDULED, COMPLETED, CANCELLED)
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `sort` (optional): Sort field (default: appointmentDate,asc)

**Example Requests:**
```
# Get all appointments for a specific date
GET /api/appointments/patient/1?appointmentStartDate=2026-01-15&appointmentEndDate=2026-01-15

# Get appointments for the next 7 days
GET /api/appointments/patient/1?appointmentStartDate=2026-01-02&appointmentEndDate=2026-01-09

# Get morning appointments only
GET /api/appointments/patient/1?startTime=09:00:00&endTime=12:00:00

# Get scheduled appointments for January 2026
GET /api/appointments/patient/1?appointmentStartDate=2026-01-01&appointmentEndDate=2026-01-31&status=SCHEDULED

# Pagination with sorting
GET /api/appointments/patient/1?page=0&size=20&sort=appointmentDate,desc
```

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
**GET** `/api/appointments/availability/{doctorId}`

Get available time slots for a doctor on a specific date.

**Query Parameters:**
- `date` (required): Date in ISO format (YYYY-MM-DD)

**Example:**
```
GET /api/appointments/availability/2?date=2024-01-15
```

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

### Get Doctor Profile
**GET** `/api/doctor/profile`

Get the authenticated doctor's profile. Requires authentication.

**Headers:**
```
Authorization: Bearer <jwt-token>
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
  "rating": 4.5,
  "consultationFee": 100.00,
  "bio": "Experienced cardiologist with expertise in...",
  "address": "123 Medical Center Dr, City, State",
  "profileImageUrl": "/uploads/profile/doctor-1.jpg"
}
```

### Update Doctor Profile
**PUT** `/api/doctor/profile`

Update the authenticated doctor's profile. Requires authentication.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

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
  "profileImageUrl": "/uploads/profile/doctor-1.jpg",
  "consultationFee": 100.00,
  "bio": "Updated bio information",
  "address": "123 Medical Center Dr, City, State"
}
```

### Patch Doctor Profile Image
**PATCH** `/api/doctor/profile`

Request S3 presigned upload URL, confirm S3 upload, or remove the doctor's profile image URL. Requires authentication.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Step 1: Request Presigned Upload URL**
To request a temporary S3 URL to upload an image directly to S3:

**Request Body:**
```json
{
  "profileImageUrl": null
}
```

**Response (200 OK):**
```json
{
  "presignedUploadUrl": "https://healthcare-images.s3.ap-south-1.amazonaws.com/profile-images/doctor/1/20260607011129-avatar.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&...",
  "s3ObjectKey": "profile-images/doctor/1/20260607011129-avatar.jpg",
  "expirationTimeMinutes": 15
}
```

**Step 2: Upload Image to S3**
The client performs an HTTP `PUT` request containing the image binary directly to the `presignedUploadUrl`.

**Step 3: Confirm Upload**
After successful upload to S3, send the `s3ObjectKey` to save it in the database:

**Request Body:**
```json
{
  "profileImageUrl": "profile-images/doctor/1/20260607011129-avatar.jpg"
}
```

**Response (200 OK):**
```json
{
  "presignedUploadUrl": null,
  "s3ObjectKey": "profile-images/doctor/1/20260607011129-avatar.jpg",
  "expirationTimeMinutes": null
}
```

**Step 4: Remove Profile Image**
To remove the stored profile image path and set it to null in the database:

**Request Body:**
```json
{
  "profileImageUrl": "remove"
}
```

**Response (200 OK):**
```json
{
  "presignedUploadUrl": null,
  "s3ObjectKey": null,
  "expirationTimeMinutes": null
}
```

### Search Doctors
**GET** `/api/doctor/search`

Search doctors using a single free-text query.

**Query Parameters:**
- `q` (optional): Search string (searches name, specialization, etc.)

**Example:**
```
GET /api/doctor/search?q=cardiology
```

**Response (200 OK):**
```json
[
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
```

### Filter Doctors
**GET** `/api/doctor/filter`

Filter doctors by multiple fields.

**Query Parameters:**
- `specialization` (optional): Filter by specialization

**Example:**
```
GET /api/doctor/filter?specialization=Cardiology
```

**Response (200 OK):**
```json
[
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
```

## 📅 Availability Endpoints

### Set Doctor Availability
**POST** `/api/availability/{doctorId}`

Set availability slots for a doctor. Requires authentication and doctor must be updating their own availability.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Request Body:**
```json
[
  {
    "date": "2024-01-15",
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "isAvailable": true
  }
]
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "doctorId": 1,
    "date": "2024-01-15",
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "isAvailable": true
  }
]
```

### Get Doctor Availability
**GET** `/api/availability/{doctorId}`

Get availability slots for a doctor.

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "doctorId": 1,
    "date": "2024-01-15",
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "isAvailable": true
  }
]
```

### Delete Availability Slot
**DELETE** `/api/availability/{doctorId}/{slotId}`

Delete a specific availability slot. Requires authentication and doctor must be deleting their own slot.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response (204 No Content):**
```
(Empty response body)
```

---

## 👤 Patient Endpoints

### Get Patient Profile
**GET** `/api/patient/profile`

Get the authenticated patient's profile. Requires authentication.

**Headers:**
```
Authorization: Bearer <jwt-token>
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
  "medicalHistory": "No significant medical history",
  "profileImageUrl": "/uploads/profile/patient-1.jpg"
}
```

### Update Patient Profile
**PUT** `/api/patient/profile`

Update the authenticated patient's profile. Requires authentication.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

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
  "profileImageUrl": "/uploads/profile/patient-1.jpg"
}
```

### Patch Patient Profile Image
**PATCH** `/api/patient/profile`

Request S3 presigned upload URL, confirm S3 upload, or remove the patient's profile image URL. Requires authentication.

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Step 1: Request Presigned Upload URL**
To request a temporary S3 URL to upload an image directly to S3:

**Request Body:**
```json
{
  "profileImageUrl": null
}
```

**Response (200 OK):**
```json
{
  "presignedUploadUrl": "https://healthcare-images.s3.ap-south-1.amazonaws.com/profile-images/patient/1/20260607011129-avatar.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&...",
  "s3ObjectKey": "profile-images/patient/1/20260607011129-avatar.jpg",
  "expirationTimeMinutes": 15
}
```

**Step 2: Upload Image to S3**
The client performs an HTTP `PUT` request containing the image binary directly to the `presignedUploadUrl`.

**Step 3: Confirm Upload**
After successful upload to S3, send the `s3ObjectKey` to save it in the database:

**Request Body:**
```json
{
  "profileImageUrl": "profile-images/patient/1/20260607011129-avatar.jpg"
}
```

**Response (200 OK):**
```json
{
  "presignedUploadUrl": null,
  "s3ObjectKey": "profile-images/patient/1/20260607011129-avatar.jpg",
  "expirationTimeMinutes": null
}
```

**Step 4: Remove Profile Image**
To remove the stored profile image path and set it to null in the database:

**Request Body:**
```json
{
  "profileImageUrl": "remove"
}
```

**Response (200 OK):**
```json
{
  "presignedUploadUrl": null,
  "s3ObjectKey": null,
  "expirationTimeMinutes": null
}
```

---

## 💳 Payment Endpoints

### Initiate Payment
**POST** `/api/payments/initiate`

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
**POST** `/api/payments/webhook/cashfree`

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
(Empty response body)
```

### Get Payment Status
**GET** `/api/payments/status/{orderId}`

Get the latest known status for a payment order.

**Response (200 OK):**
```
Your payment status is <STATUS>
```

### Debug Orders
**GET** `/api/payments/debug/orders`

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

### Get Paginated Payments for Patient
**GET** `/api/payments/payment-details/{id}`

Retrieve paginated payments for a specific patient with optional filters. Defaults to page size 10.

**Path Parameters:**
- `id` (required): Patient ID

**Query Parameters:**
- `page` (optional): Page number (0-based, default: 0)
- `size` (optional): Page size (default: 10)
- `status` (optional): Filter by status, e.g., PAID, PENDING, FAILED
- `paymentMode` (optional): Filter by mode, e.g., UPI, CARD
- `minAmount` (optional): Minimum order amount
- `maxAmount` (optional): Maximum order amount

**Examples:**
- Basic: `GET /api/payments/payment-details/123`
- Paged: `GET /api/payments/payment-details/123?page=1&size=5`
- Filtered: `GET /api/payments/payment-details/123?status=PAID&paymentMode=UPI`

**Response (200 OK):**
```json
{
  "_embedded": {
    "paymentEntityList": [
      {
        "id": 1,
        "orderId": "order_123456789",
        "status": "PAID",
        "referenceId": "ref_123456789",
        "paymentMode": "UPI",
        "orderAmount": 1500.00,
        "appointmentId": 1
      }
    ]
  },
  "_links": {
    "self": { "href": "/api/payments/payment-details/123?page=0&size=10" },
    "next": { "href": "/api/payments/payment-details/123?page=1&size=10" }
  },
  "page": {
    "size": 10,
    "totalElements": 45,
    "totalPages": 5,
    "number": 0
  }
}
```

---

## 📹 Video Call Endpoints

### Create Video Session
**POST** `/api/video-call/session/{appointmentId}`

Create a new video call session for an appointment.

**Response (200 OK):**
```json
{
  "sessionId": "session_123456789",
  "accessToken": "twilio_access_token_here",
  "roomName": "appointment_1_room",
  "status": "CREATED",
  "expiresAt": "2024-01-10T11:00:00Z"
}
```

### Get Video Session
**GET** `/api/video-call/session/{appointmentId}`

Get an existing video call session for an appointment.

**Response (200 OK):**
```json
{
  "sessionId": "session_123456789",
  "accessToken": "twilio_access_token_here",
  "roomName": "appointment_1_room",
  "status": "ACTIVE",
  "expiresAt": "2024-01-10T11:00:00Z"
}
```

### Get Access Token
**GET** `/api/video-call/token/{appointmentId}`

Generate an access token for joining a video call.

**Query Parameters:**
- `userType` (required): User type (DOCTOR or PATIENT)
- `userId` (required): User ID

**Example:**
```
GET /api/video-call/token/1?userType=DOCTOR&userId=2
```

**Response (200 OK):**
```
"twilio_access_token_string_here"
```

### End Video Session
**POST** `/api/video-call/end/{appointmentId}`

End an existing video call session.

**Response (200 OK):**
```
(Empty response body)
```

### Twilio Webhook
**POST** `/api/video-call/webhook`

Handle incoming Twilio webhook events.

**Request Body:**
```json
{
  "eventType": "participant-connected",
  "eventData": {
    "sessionId": "session_123456789",
    "participantName": "Dr. Smith"
  }
}
```

**Response (200 OK):**
```
(Empty response body)
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