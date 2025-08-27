# Payment API Guide

## New Pagination Endpoints

### 1. Get Latest 10 Payments
**Endpoint:** `GET /api/payments/latest`

**Description:** Retrieves the 10 most recent payments without any pagination parameters.

**Response:** Returns a list of the latest 10 `PaymentEntity` objects, sorted by ID in descending order (newest first).

**Example:**
```bash
curl -X GET "http://localhost:8080/api/payments/latest"
```

### 2. Get Paginated Payments with Filtering
**Endpoint:** `GET /api/payments/list`

**Description:** Retrieves paginated payments with optional filtering capabilities.

**Query Parameters:**
- `page` (optional): Page number (0-based, defaults to 0)
- `size` (optional): Page size (defaults to 10)
- `status` (optional): Filter by payment status (e.g., "PAID", "PENDING", "FAILED")
- `paymentMode` (optional): Filter by payment mode (e.g., "UPI", "CARD")
- `patientId` (optional): Filter by specific patient ID
- `minAmount` (optional): Minimum order amount filter
- `maxAmount` (optional): Maximum order amount filter

**Response:** Returns a `Page<PaymentEntity>` object containing:
- `content`: List of payment entities for the current page
- `totalElements`: Total number of payments matching the filters
- `totalPages`: Total number of pages
- `size`: Page size
- `number`: Current page number
- `first`: Whether this is the first page
- `last`: Whether this is the last page

**Examples:**

**Basic pagination (first page, 10 items):**
```bash
curl -X GET "http://localhost:8080/api/payments/list"
```

**Second page with 5 items:**
```bash
curl -X GET "http://localhost:8080/api/payments/list?page=1&size=5"
```

**Filter by status:**
```bash
curl -X GET "http://localhost:8080/api/payments/list?status=PAID"
```

**Filter by patient ID:**
```bash
curl -X GET "http://localhost:8080/api/payments/list?patientId=123"
```

**Filter by amount range:**
```bash
curl -X GET "http://localhost:8080/api/payments/list?minAmount=100&maxAmount=500"
```

**Combined filters:**
```bash
curl -X GET "http://localhost:8080/api/payments/list?status=PAID&paymentMode=UPI&page=0&size=20"
```

## Implementation Details

### Specifications
The API uses Spring Data JPA Specifications (`PaymentSpecifications`) for dynamic filtering:

- **Status Filter**: Exact match (case-insensitive)
- **Payment Mode Filter**: Partial match (case-insensitive)
- **Patient ID Filter**: Exact match
- **Amount Range Filter**: Between min and max values

### Repository
The `PaymentRepository` extends both `JpaRepository` and `JpaSpecificationExecutor` to support:
- Basic CRUD operations
- Specification-based queries
- Pagination and sorting

### Service Layer
The `PaymentService.getPaginatedPayments()` method:
- Builds dynamic specifications based on filter parameters
- Applies pagination and sorting
- Returns paginated results ordered by ID descending (latest first)

### Controller
Two endpoints are provided:
1. `/latest` - Simple endpoint for getting the 10 most recent payments
2. `/list` - Full-featured endpoint with pagination and filtering

## Response Format

### Latest Payments Response
```json
[
  {
    "id": 100,
    "orderId": "order_123",
    "status": "PAID",
    "referenceId": "ref_456",
    "paymentMode": "UPI",
    "transactionTime": "2024-01-15T10:30:00",
    "orderAmount": 1500.00,
    "patientId": 123,
    "appointmentHoldReference": "app_789"
  }
  // ... more payment objects
]
```

### Paginated Response
```json
{
  "content": [
    // ... payment objects
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 45,
  "totalPages": 5,
  "last": false,
  "first": true,
  "sort": {
    "sorted": true,
    "unsorted": false
  },
  "numberOfElements": 10,
  "size": 10,
  "number": 0
}
```

## Error Handling

Both endpoints include proper error handling:
- Returns HTTP 500 for internal server errors
- Logs errors for debugging
- Gracefully handles exceptions without exposing sensitive information

## Performance Considerations

- Results are sorted by ID descending for optimal performance
- Pagination limits the number of records returned per request
- Specifications allow for efficient database queries with proper indexing
- Default page size of 10 provides a good balance between performance and usability
