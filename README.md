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

🔁 service-discovery (Eureka Server)
server:
  port: 8761
✅ This service will run on port 8761 (you can access Eureka UI at http://localhost:8761).

spring:
  application:
    name: service-discovery
✅ Gives your application a name (service-discovery) so other services can refer to it.

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
✅ Since this is the Eureka Server, it doesn’t register itself or fetch other services.

👤 user-service (Calls department-service)
server:
  port: 9091
✅ Runs on port 9091.

spring:
  application:
    name: user-service
✅ Registers itself to Eureka with the name user-service.

  datasource:
    url: jdbc:mysql://localhost:3306/employee_db
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
✅ Connects to MySQL using the given credentials and employee_db database.

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
✅ Enables auto schema update, SQL logging, and sets the MySQL 8 dialect.

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/
✅ Registers to Eureka server running at http://localhost:8761/eureka.

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
✅ Enables actuator endpoints like /actuator/health and /actuator/info.

🛡️ Resilience4j Configuration in user-service
✅ Circuit Breaker
resilience4j:
  circuitbreaker:
    instances:
      department-service:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        wait-duration-in-open-state: 5s
        failure-rate-threshold: 50
        automatic-transition-from-open-to-half-open-enabled: true
🧠 What it does:

Tracks the last 10 calls (sliding-window-size)

Breaks circuit if 50% of last 5 calls (minimum-number-of-calls) failed

Stays open for 5s before testing again with 3 calls (half-open)

Prevents overloading when department-service is down

✅ Rate Limiter
  ratelimiter:
    instances:
      userService:
        limit-for-period: 5
        limit-refresh-period: 10s
        timeout-duration: 0
🧠 What it does:

Allows only 5 API calls every 10 seconds for that method

If exceeded, immediately fails (timeout-duration: 0)

🏢 department-service
server:
  port: 9090
✅ Runs on 9090.

spring:
  application:
    name: department-service
✅ Registers as department-service.

  datasource:
    url: jdbc:mysql://localhost:3306/department_db
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
✅ Connects to the department_db in MySQL.

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
✅ Hibernate JPA settings for MySQL.

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/
✅ Registers with Eureka so user-service and api-gateway can discover it.

🌐 api-gateway
server:
  port: 8080
✅ API Gateway runs on port 8080.

spring:
  application:
    name: api-gateway
✅ Identifies itself as api-gateway.

✅ Route Configuration
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/user-service/**
        - id: department-service
          uri: lb://department-service
          predicates:
            - Path=/department-service/**
🧠 What it does:

When someone hits /user-service/**, the gateway routes the request to the user-service

Similarly, /department-service/** goes to department-service

lb:// means it uses Eureka LoadBalancer

✅ Eureka Client
eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/
✅ Registers with Eureka and fetches other service info.

✅ Summary
Microservice	Port	Responsibilities
service-discovery	8761	Registers and discovers services (Eureka Server)
user-service	9091	Handles user CRUD, calls department-service
department-service	9090	Stores and retrieves department data
api-gateway	8080	Routes incoming requests to appropriate services



