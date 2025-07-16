# Spring Boot Microservices Architecture

This project demonstrates a microservices architecture using Spring Boot 3, Spring Cloud, and Netflix OSS tools. It includes:

- **Department Service**
- **User Service**
- **API Gateway**
- **Service Discovery (Eureka Server)**
- **Circuit Breaker with Resilience4j**

---

## 🧩 Technologies Used

- Java 19
- Spring Boot 3.2.x
- Spring Cloud 2023.0.x
- Eureka Discovery Server
- Spring Cloud Gateway
- WebClient + Resilience4j
- Maven
- MySQL (for User Service)
- Actuator + Micrometer

---

## 📦 Microservices Overview

### 🧭 1. Service Discovery (Eureka Server)
- Acts as a **central registry** where microservices register themselves.
- Enables dynamic service lookup, avoiding hardcoded URLs.
- All microservices (User, Department, API Gateway) **register** with Eureka.
- URL: `http://localhost:8761`

### 🌐 2. API Gateway (Spring Cloud Gateway)
- Serves as a **single entry point** to all backend microservices.
- Uses Eureka to **dynamically route** traffic to appropriate services.
- **Load balances** requests across multiple instances of services.
- Handles **routing**, **authentication**, **rate limiting**, and more.
- Example routes:
  - `http://localhost:8080/api/users/{id}`
  - `http://localhost:8080/api/departments/{id}`

### 👤 3. User Service
- Exposes REST APIs to manage user data.
- Stores data in **MySQL**.
- Calls the Department Service via **WebClient**.
- Implements **Resilience4j Circuit Breaker** for fault tolerance.

### 🏢 4. Department Service
- Simple microservice that manages department data.
- Exposes API to retrieve department by ID.

---

## ⚙️ Circuit Breaker with Resilience4j

The User Service uses Resilience4j to avoid cascading failures if the Department Service is down or slow.

### ✅ Working Example

```java
@CircuitBreaker(name = "department-service", fallbackMethod = "departmentFallback")
public DepartmentDto getDepartmentByUser(Long departmentId) {
    return webClientBuilder.build()
        .get()
        .uri("http://department-service/api/departments/" + departmentId)
        .retrieve()
        .bodyToMono(DepartmentDto.class)
        .block();
}
