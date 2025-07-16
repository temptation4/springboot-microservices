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

🧭 1. service-discovery – Eureka Server
server:
  port: 8761

spring:
  application:
    name: service-discovery

eureka:
  client:
    register-with-eureka: false  # This app is the Eureka server, so it doesn't register itself
    fetch-registry: false
✅ What it does:

Runs on port 8761

Acts as a central registry where other services will register

Visit: http://localhost:8761

👤 2. user-service – Calls Department Service via WebClient
server:
  port: 9091

spring:
  application:
    name: user-service

  datasource:
    url: jdbc:mysql://localhost:3306/employee_db
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always

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

  ratelimiter:
    instances:
      userService:
        limit-for-period: 5
        limit-refresh-period: 10s
        timeout-duration: 0
✅ What it does:

Runs on 9091

Registers with Eureka (service-discovery)

Uses MySQL

Uses Circuit Breaker when calling Department Service

RateLimiter allows only 5 calls every 10 seconds

🏢 3. department-service
server:
  port: 9090

spring:
  application:
    name: department-service

  datasource:
    url: jdbc:mysql://localhost:3306/department_db
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
✅ What it does:

Runs on port 9090

Stores department data in MySQL

Registers with Eureka

🌐 4. api-gateway
server:
  port: 8080

spring:
  application:
    name: api-gateway

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

eureka:
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/

management:
  endpoints:
    web:
      exposure:
        include: "*"
✅ What it does:

Runs on port 8080

Routes traffic to services:

/user-service/** → user-service

/department-service/** → department-service

Uses load balancer (lb://) and Eureka for dynamic service discovery

✅ How to Test
🎯 User Service API via Gateway:
Method	Gateway URL
POST	http://localhost:8080/user-service/api/users/save
GET	http://localhost:8080/user-service/api/users/{id}
GET	http://localhost:8080/user-service/api/users/test-rate

🎯 Department Service API via Gateway:
Method	Gateway URL
POST	http://localhost:8080/department-service/api/departments
GET	http://localhost:8080/department-service/api/departments/{id}


