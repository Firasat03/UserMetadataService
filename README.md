# User Metadata Service

A comprehensive Spring Boot application for managing user metadata, featuring resilience patterns (Resilience4j), circuit breakers, and monitoring.

## Prerequisites
- **Java 17** or higher
- **Maven** 3.6+
- **Docker** (optional, for containerized deployment)

## Quick Start (Local)

1. **Build the Application**
   mvn clean install


2. **Run the Application**
   java -jar target/user-service-v2-0.0.1-SNAPSHOT.jar
   # Or using Maven directly:
   mvn spring-boot:run
   
   The application will start on **port 8080**.

## Quick Start (Docker)

1. **Build Docker Image**
   docker build -t user-service .

2. **Run Docker Container**
   docker run -d -p 8080:8080 --name user-service user-service

## Verification & Testing
   A script is provided to test the main endpoints:
   sh test.sh

## API Documentation

| Resource | URL | Description |
| :--- | :--- | :--- |
| **Swagger UI** | [http://localhost:8080/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html) | Interactive API documentation |
| **Actuator Health** | [http://localhost:8080/actuator/health](http://localhost:8082/actuator/health) | Application health status |
| **H2 Console** | [http://localhost:8080/h2-console](http://localhost:8082/h2-console) | In-memory database access |

### H2 Database Credentials
- **Driver Class**: `org.h2.Driver`
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **User Name**: `sa`
- **Password**: `password`

## Key Features
- **Idempotency**: Prevents duplicate user creation based on email.
- **Circuit Breaker**: Resilient database interactions using Resilience4j.
- **Monitoring**: Prometheus metrics exposed via Actuator.
