# Healthcare Management System - Deployment Guide

This guide covers deployment of the Healthcare Management System across different environments.

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Local Development Setup](#local-development-setup)
3. [Docker Deployment](#docker-deployment)
4. [Cloud Deployment](#cloud-deployment)
5. [Production Deployment](#production-deployment)
6. [Environment Configuration](#environment-configuration)
7. [Monitoring and Logging](#monitoring-and-logging)
8. [Security Checklist](#security-checklist)

---

## 🔧 Prerequisites

### System Requirements
- **Java**: 17 or higher
- **Maven**: 3.6 or higher
- **PostgreSQL**: 12 or higher
- **Docker**: 20.10 or higher (optional)
- **Git**: Latest version

### Required Accounts
- **Cashfree**: Payment gateway account
- **Twilio**: Video calling service account
- **Email Provider**: Gmail or other SMTP provider

---

## 🏠 Local Development Setup

### 1. Clone and Setup
```bash
# Clone the repository
git clone <repository-url>
cd HealthCare

# Create environment file
cp .env.example .env
```

### 2. Database Setup
```bash
# Install PostgreSQL (Ubuntu/Debian)
sudo apt update
sudo apt install postgresql postgresql-contrib

# Create database and user
sudo -u postgres psql
CREATE DATABASE healthcare_db;
CREATE USER healthcare_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE healthcare_db TO healthcare_user;
\q
```

### 3. Environment Configuration
Edit `.env` file with your local settings:
```env
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/healthcare_db
DB_USERNAME=healthcare_user
DB_PASSWORD=your_password

# JWT Configuration
JWT_SECRET=your_super_secret_jwt_key_for_development_only

# Email Configuration
EMAIL_ID=your_email@gmail.com
EMAIL_PASSWORD=your_app_password

# Cashfree Configuration (Sandbox)
APP_ID=test_app_id
SECRET_KEY=test_secret_key

# Application Configuration
SERVER_PORT=8080
LOGGING_LEVEL=DEBUG
```

### 4. Build and Run
```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run

# Or using Java directly
java -jar target/HealthCare-0.0.1-SNAPSHOT.jar
```

### 5. Verify Installation
- Application: http://localhost:8080
- Health Check: http://localhost:8080/actuator/health
- API Documentation: http://localhost:8080/api

---

## 🐳 Docker Deployment

### 1. Create Dockerfile
```dockerfile
# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Create runtime image
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy built jar
COPY --from=0 /app/target/HealthCare-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Create docker-compose.yml
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DATABASE_URL=jdbc:postgresql://db:5432/healthcare_db
      - DB_USERNAME=healthcare_user
      - DB_PASSWORD=healthcare_password
      - JWT_SECRET=your_jwt_secret
      - EMAIL_ID=${EMAIL_ID}
      - EMAIL_PASSWORD=${EMAIL_PASSWORD}
      - APP_ID=${APP_ID}
      - SECRET_KEY=${SECRET_KEY}
    depends_on:
      - db
    networks:
      - healthcare-network

  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=healthcare_db
      - POSTGRES_USER=healthcare_user
      - POSTGRES_PASSWORD=healthcare_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - healthcare-network

volumes:
  postgres_data:

networks:
  healthcare-network:
    driver: bridge
```

### 3. Deploy with Docker
```bash
# Build and start services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop services
docker-compose down
```

---

## ☁️ Cloud Deployment

### AWS Deployment

#### 1. EC2 Deployment
```bash
# Launch EC2 instance (t3.medium recommended)
# Install Java and Maven
sudo yum update -y
sudo yum install java-17-amazon-corretto maven -y

# Clone and deploy
git clone <repository-url>
cd HealthCare
mvn clean package -DskipTests
java -jar target/HealthCare-0.0.1-SNAPSHOT.jar
```

#### 2. RDS Database Setup
```sql
-- Create database in RDS PostgreSQL
CREATE DATABASE healthcare_db;
CREATE USER healthcare_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE healthcare_db TO healthcare_user;
```

#### 3. Environment Variables
```bash
export DATABASE_URL=jdbc:postgresql://your-rds-endpoint:5432/healthcare_db
export DB_USERNAME=healthcare_user
export DB_PASSWORD=secure_password
export JWT_SECRET=your_production_jwt_secret
export EMAIL_ID=your_email@gmail.com
export EMAIL_PASSWORD=your_app_password
export APP_ID=your_cashfree_app_id
export SECRET_KEY=your_cashfree_secret_key
```

### Google Cloud Platform (GCP)

#### 1. App Engine Deployment
Create `app.yaml`:
```yaml
runtime: java17
env: standard

instance_class: F1

automatic_scaling:
  target_cpu_utilization: 0.6
  min_instances: 1
  max_instances: 10

env_variables:
  DATABASE_URL: "jdbc:postgresql://your-cloud-sql-instance:5432/healthcare_db"
  DB_USERNAME: "healthcare_user"
  DB_PASSWORD: "secure_password"
  JWT_SECRET: "your_production_jwt_secret"
  EMAIL_ID: "your_email@gmail.com"
  EMAIL_PASSWORD: "your_app_password"
  APP_ID: "your_cashfree_app_id"
  SECRET_KEY: "your_cashfree_secret_key"
```

Deploy:
```bash
gcloud app deploy
```

### Azure Deployment

#### 1. Azure App Service
```bash
# Create App Service
az appservice plan create --name healthcare-plan --resource-group healthcare-rg --sku B1
az webapp create --name healthcare-app --resource-group healthcare-rg --plan healthcare-plan --runtime "JAVA:17-java17"

# Deploy application
az webapp deployment source config-local-git --name healthcare-app --resource-group healthcare-rg
git remote add azure <azure-git-url>
git push azure main
```

---

## 🚀 Production Deployment

### 1. Production Environment Checklist

- [ ] **Database**: Production PostgreSQL with backup strategy
- [ ] **SSL/TLS**: HTTPS enabled with valid certificates
- [ ] **Load Balancer**: Configured for high availability
- [ ] **Monitoring**: Application and infrastructure monitoring
- [ ] **Logging**: Centralized logging solution
- [ ] **Backup**: Automated database and file backups
- [ ] **Security**: Firewall, intrusion detection, and security scanning

### 2. Production Configuration
```properties
# application-prod.properties
spring.profiles.active=prod

# Database
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate

# Security
jwt.secret=${JWT_SECRET}
jwt.expiration.ms=86400000

# Logging
logging.level.root=WARN
logging.level.com.ashwani.HealthCare=INFO
logging.file.name=logs/application.log
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Performance
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# Health checks
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

### 3. Deployment Script
```bash
#!/bin/bash
# deploy.sh

set -e

echo "Starting deployment..."

# Pull latest code
git pull origin main

# Build application
mvn clean package -DskipTests -Pprod

# Stop existing application
sudo systemctl stop healthcare-app

# Backup current version
sudo cp /opt/healthcare/app.jar /opt/healthcare/app.jar.backup.$(date +%Y%m%d_%H%M%S)

# Deploy new version
sudo cp target/HealthCare-0.0.1-SNAPSHOT.jar /opt/healthcare/app.jar

# Start application
sudo systemctl start healthcare-app

# Health check
sleep 30
curl -f http://localhost:8080/actuator/health || exit 1

echo "Deployment completed successfully!"
```

### 4. Systemd Service
```ini
# /etc/systemd/system/healthcare-app.service
[Unit]
Description=Healthcare Management System
After=network.target

[Service]
Type=simple
User=healthcare
WorkingDirectory=/opt/healthcare
ExecStart=/usr/bin/java -Xms512m -Xmx1024m -jar app.jar
ExecReload=/bin/kill -HUP $MAINPID
Restart=always
RestartSec=10

Environment=DATABASE_URL=jdbc:postgresql://localhost:5432/healthcare_db
Environment=DB_USERNAME=healthcare_user
Environment=DB_PASSWORD=secure_password
Environment=JWT_SECRET=your_production_jwt_secret
Environment=EMAIL_ID=your_email@gmail.com
Environment=EMAIL_PASSWORD=your_app_password
Environment=APP_ID=your_cashfree_app_id
Environment=SECRET_KEY=your_cashfree_secret_key

[Install]
WantedBy=multi-user.target
```

---

## ⚙️ Environment Configuration

### Development Environment
```env
# .env.dev
NODE_ENV=development
DATABASE_URL=jdbc:postgresql://localhost:5432/healthcare_dev
DB_USERNAME=dev_user
DB_PASSWORD=dev_password
JWT_SECRET=dev_jwt_secret
EMAIL_ID=dev@example.com
EMAIL_PASSWORD=dev_password
APP_ID=test_app_id
SECRET_KEY=test_secret_key
LOGGING_LEVEL=DEBUG
```

### Staging Environment
```env
# .env.staging
NODE_ENV=staging
DATABASE_URL=jdbc:postgresql://staging-db:5432/healthcare_staging
DB_USERNAME=staging_user
DB_PASSWORD=staging_password
JWT_SECRET=staging_jwt_secret
EMAIL_ID=staging@example.com
EMAIL_PASSWORD=staging_password
APP_ID=staging_app_id
SECRET_KEY=staging_secret_key
LOGGING_LEVEL=INFO
```

### Production Environment
```env
# .env.prod
NODE_ENV=production
DATABASE_URL=jdbc:postgresql://prod-db:5432/healthcare_prod
DB_USERNAME=prod_user
DB_PASSWORD=prod_secure_password
JWT_SECRET=prod_jwt_secret
EMAIL_ID=prod@example.com
EMAIL_PASSWORD=prod_password
APP_ID=prod_app_id
SECRET_KEY=prod_secret_key
LOGGING_LEVEL=WARN
```

---

## 📊 Monitoring and Logging

### 1. Application Monitoring
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

### 2. Prometheus Configuration
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'healthcare-app'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
```

### 3. Logging Configuration
```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

---

## 🔒 Security Checklist

### Pre-Deployment Security
- [ ] **Environment Variables**: All secrets stored in environment variables
- [ ] **Database Security**: Strong passwords, limited user permissions
- [ ] **SSL/TLS**: HTTPS enabled with valid certificates
- [ ] **Firewall**: Configured to allow only necessary ports
- [ ] **JWT Secret**: Strong, unique JWT secret key
- [ ] **Input Validation**: All inputs validated and sanitized
- [ ] **SQL Injection**: Prepared statements used
- [ ] **XSS Protection**: Output encoding implemented

### Runtime Security
- [ ] **Regular Updates**: Keep dependencies updated
- [ ] **Security Scanning**: Regular vulnerability scans
- [ ] **Access Logging**: Monitor access patterns
- [ ] **Backup Encryption**: Encrypt database backups
- [ ] **Rate Limiting**: Implement API rate limiting
- [ ] **CORS Configuration**: Proper CORS settings
- [ ] **Health Checks**: Regular health check monitoring

### Compliance
- [ ] **HIPAA Compliance**: If applicable for US healthcare
- [ ] **GDPR Compliance**: If applicable for EU users
- [ ] **Data Encryption**: Encrypt sensitive data at rest and in transit
- [ ] **Audit Logging**: Log all access to sensitive data
- [ ] **Data Retention**: Implement proper data retention policies

---

## 🆘 Troubleshooting

### Common Issues

#### 1. Database Connection Issues
```bash
# Check database connectivity
psql -h your-db-host -U your-username -d your-database

# Check application logs
tail -f logs/application.log | grep -i database
```

#### 2. Memory Issues
```bash
# Monitor memory usage
jstat -gc <pid>

# Increase heap size
java -Xms1g -Xmx2g -jar app.jar
```

#### 3. Port Conflicts
```bash
# Check port usage
netstat -tulpn | grep :8080

# Kill process using port
sudo kill -9 <pid>
```

### Performance Optimization
- Use connection pooling (HikariCP)
- Implement caching (Redis)
- Optimize database queries
- Use CDN for static resources
- Enable compression (gzip)

---

## 📞 Support

For deployment issues:
1. Check application logs: `tail -f logs/application.log`
2. Verify environment variables: `env | grep -E "(DATABASE|JWT|EMAIL|CASHFREE)"`
3. Test database connectivity
4. Check system resources: `htop`, `df -h`, `free -h`
5. Contact the development team with error logs 