# Docker Setup Guide

This guide explains how to run the HealthCare application using Docker and Docker Compose.

## Prerequisites

- Docker Desktop (or Docker Engine + Docker Compose)
- Docker version 20.10 or higher
- Docker Compose version 2.0 or higher
- Aiven Cloud PostgreSQL database (or other PostgreSQL instance)
- Twilio account credentials (for video calling features)

## Quick Start

### 1. Set Up Environment Variables

Create a `.env` file in the project root with the following variables:

```bash
# Aiven Cloud PostgreSQL Configuration
DATABASE_URL=jdbc:postgresql://your-aiven-host:5432/your-database
DB_USERNAME=your-aiven-username
DB_PASSWORD=your-aiven-password

# RabbitMQ Configuration (optional - for local RabbitMQ)
RABBITMQ_USERNAME=rabbitmq_user
RABBITMQ_PASSWORD=rabbitmq_password
RABBITMQ_AMQP_PORT=5672
RABBITMQ_MGMT_PORT=15672
RABBITMQ_CONTAINER_NAME=healthcare-rabbitmq

# Application Configuration
APP_CONTAINER_NAME=healthcare-app
APP_PORT=8080
SPRING_PROFILE=docker

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production-min-256-bits

# Email Configuration (Gmail example)
EMAIL_ID=your-email@gmail.com
EMAIL_PASSWORD=your-app-specific-password

# Twilio Configuration (for video calling)
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_API_KEY=your-twilio-api-key
TWILIO_API_SECRET=your-twilio-api-secret

# Cashfree Configuration
APP_ID=your-cashfree-app-id
SECRET_KEY=your-cashfree-secret-key

# Frontend/Backend URLs
FRONTEND_BASE_URL=http://localhost:5173
BACKEND_BASE_URL=http://localhost:8080
```

### 2. Build and Run

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f healthcare-app

# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: This deletes database data)
docker-compose down -v
```

## Services

The Docker Compose setup includes:

1. **Aiven Cloud PostgreSQL Database** (External)
   - Configured via `DATABASE_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables
   - SSL is required and enabled by default for cloud database connections
   - Ensure your Aiven PostgreSQL instance is accessible from your Docker network

2. **RabbitMQ Message Broker** (Optional - Local)
   - AMQP Port: Configurable via `RABBITMQ_AMQP_PORT` (default: 5672)
   - Management UI: Configurable via `RABBITMQ_MGMT_PORT` (default: 15672)
   - Username: Configurable via `RABBITMQ_USERNAME` (default: `rabbitmq_user`)
   - Password: Configurable via `RABBITMQ_PASSWORD` (default: `rabbitmq_password`)
   - **Note**: If you're using a cloud RabbitMQ service, you can remove this service and update `RABBITMQ_URL` to point to your cloud instance

3. **HealthCare Application** (port 8080)
   - API: http://localhost:8080 (configurable via `APP_PORT`)
   - Uses Docker profile for configuration
   - Connects to Aiven PostgreSQL and local/cloud RabbitMQ

## Useful Commands

```bash
# Rebuild the application after code changes
docker-compose build healthcare-app
docker-compose up -d healthcare-app

# View logs for all services
docker-compose logs -f

# View logs for a specific service
docker-compose logs -f rabbitmq
docker-compose logs -f healthcare-app

# Execute commands in a running container
docker-compose exec healthcare-app sh

# Check service status
docker-compose ps

# Restart a specific service
docker-compose restart healthcare-app
```

## Database Access

Since the application uses Aiven Cloud PostgreSQL, you can connect to it using:

```bash
# Using psql with SSL
psql "host=your-aiven-host port=5432 dbname=your-database user=your-username password=your-password sslmode=require"

# Or using connection string from Aiven dashboard
# Aiven provides a ready-to-use connection string in their dashboard
```

**Note**: Aiven PostgreSQL requires SSL connections. The Docker profile automatically enables SSL for database connections.

## RabbitMQ Management UI

If using local RabbitMQ, access the Management UI at:
- URL: http://localhost:15672 (or your configured `RABBITMQ_MGMT_PORT`)
- Username: Configurable via `RABBITMQ_USERNAME` (default: `rabbitmq_user`)
- Password: Configurable via `RABBITMQ_PASSWORD` (default: `rabbitmq_password`)

**Note**: If you're using a cloud RabbitMQ service (e.g., CloudAMQP, AWS MQ), access the management UI through your cloud provider's dashboard.

## Troubleshooting

### Application won't start

1. Check if all services are healthy:
   ```bash
   docker-compose ps
   ```

2. Check application logs:
   ```bash
   docker-compose logs healthcare-app
   ```

3. Verify environment variables are set correctly in `.env` file

### Database connection issues

1. Verify your Aiven PostgreSQL credentials are correct:
   - Check `DATABASE_URL`, `DB_USERNAME`, and `DB_PASSWORD` in your `.env` file
   - Ensure the Aiven host is accessible from your network
   - Verify SSL is enabled (required for Aiven)

2. Test database connectivity:
   ```bash
   # Test connection from your local machine
   psql "host=your-aiven-host port=5432 dbname=your-database user=your-username password=your-password sslmode=require"
   ```

3. Check application logs for database connection errors:
   ```bash
   docker-compose logs healthcare-app | grep -i "database\|postgres\|connection"
   ```

4. Verify network connectivity:
   - Ensure your Aiven PostgreSQL allows connections from your IP address
   - Check firewall rules if applicable

### Port conflicts

If ports 5672, 15672, or 8080 are already in use, you can configure them via environment variables:

```bash
# In your .env file
APP_PORT=8081
RABBITMQ_AMQP_PORT=5673
RABBITMQ_MGMT_PORT=15673
```

### RabbitMQ connection issues

1. If using local RabbitMQ, ensure it's running:
   ```bash
   docker-compose ps rabbitmq
   ```

2. If using cloud RabbitMQ, verify:
   - `RABBITMQ_URL` points to your cloud instance
   - Credentials are correct
   - Network access is allowed

3. Check RabbitMQ logs:
   ```bash
   docker-compose logs rabbitmq
   ```

## Production Considerations

For production deployment:

1. **Use managed services**:
   - Aiven PostgreSQL (already configured) or other managed PostgreSQL service
   - Cloud RabbitMQ (AWS MQ, CloudAMQP, etc.) instead of local RabbitMQ
   - Remove local RabbitMQ service from docker-compose.yml if using cloud service

2. **Security**:
   - **Never commit** `.env` file to version control
   - **Use secrets management** for sensitive environment variables (AWS Secrets Manager, HashiCorp Vault, Docker Secrets, etc.)
   - **Change all default passwords** and use strong, unique passwords
   - **Enable SSL/TLS** for all connections (already enabled for Aiven PostgreSQL)

3. **Configuration**:
   - Set `SPRING_PROFILE=production` for production-specific settings
   - Configure proper resource limits for containers
   - Use environment-specific values for `FRONTEND_BASE_URL` and `BACKEND_BASE_URL`

4. **Backup and Monitoring**:
   - Set up automated backups for Aiven PostgreSQL (usually handled by Aiven)
   - Configure proper logging and monitoring (CloudWatch, Datadog, etc.)
   - Set up health checks and alerts

5. **Infrastructure**:
   - Use a reverse proxy (nginx, Traefik, AWS ALB) for production
   - Configure proper firewall rules
   - Use container orchestration (Kubernetes, ECS) for better scalability
   - Implement proper CI/CD pipelines

## Volumes

Data is persisted in Docker volumes:
- `rabbitmq_data`: RabbitMQ data and configuration (only if using local RabbitMQ)

**Note**: Since PostgreSQL is hosted on Aiven Cloud, database backups are typically handled by Aiven's automated backup system. However, you can still create manual backups:

To backup the Aiven database:
```bash
# Using pg_dump with SSL
pg_dump "host=your-aiven-host port=5432 dbname=your-database user=your-username password=your-password sslmode=require" > backup.sql
```

To restore:
```bash
# Using psql with SSL
psql "host=your-aiven-host port=5432 dbname=your-database user=your-username password=your-password sslmode=require" < backup.sql
```

## Using Cloud RabbitMQ

If you want to use a cloud RabbitMQ service instead of the local one:

1. **Remove or comment out** the `rabbitmq` service in `docker-compose.yml`
2. **Update** `RABBITMQ_URL` in your `.env` file to point to your cloud RabbitMQ instance:
   ```bash
   RABBITMQ_URL=amqps://your-cloud-rabbitmq-host:5671
   RABBITMQ_USERNAME=your-cloud-username
   RABBITMQ_PASSWORD=your-cloud-password
   ```
3. **Remove** the RabbitMQ dependency from the `healthcare-app` service's `depends_on` section
4. **Remove** the `rabbitmq_data` volume if not needed

## Environment Variable Reference

All configurable environment variables:

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DATABASE_URL` | Aiven PostgreSQL connection URL | - | Yes |
| `DB_USERNAME` | Database username | - | Yes |
| `DB_PASSWORD` | Database password | - | Yes |
| `RABBITMQ_URL` | RabbitMQ connection URL | `amqp://rabbitmq:5672` | Yes |
| `RABBITMQ_USERNAME` | RabbitMQ username | `rabbitmq_user` | No |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `rabbitmq_password` | No |
| `RABBITMQ_AMQP_PORT` | RabbitMQ AMQP port | `5672` | No |
| `RABBITMQ_MGMT_PORT` | RabbitMQ Management UI port | `15672` | No |
| `TWILIO_ACCOUNT_SID` | Twilio Account SID | - | Yes |
| `TWILIO_AUTH_TOKEN` | Twilio Auth Token | - | Yes |
| `TWILIO_API_KEY` | Twilio API Key | - | No |
| `TWILIO_API_SECRET` | Twilio API Secret | - | No |
| `JWT_SECRET` | JWT signing secret | - | Yes |
| `EMAIL_ID` | Email address for sending emails | - | Yes |
| `EMAIL_PASSWORD` | Email password/app password | - | Yes |
| `APP_ID` | Cashfree App ID | - | Yes |
| `SECRET_KEY` | Cashfree Secret Key | - | Yes |
| `FRONTEND_BASE_URL` | Frontend application URL | `http://localhost:5173` | No |
| `BACKEND_BASE_URL` | Backend API URL | `http://localhost:8080` | No |
| `APP_PORT` | Application port | `8080` | No |
| `APP_CONTAINER_NAME` | Container name | `healthcare-app` | No |
| `SPRING_PROFILE` | Spring profile to use | `docker` | No |

