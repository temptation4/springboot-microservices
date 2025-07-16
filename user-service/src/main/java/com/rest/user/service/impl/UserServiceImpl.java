package com.rest.user.service.impl;

import com.rest.user.dto.DepartmentDto;
import com.rest.user.dto.ResponseDto;
import com.rest.user.dto.UserDto;
import com.rest.user.entity.User;
import com.rest.user.repository.UserRepository;
import com.rest.user.service.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
   // private final RestTemplate restTemplate;

    private static final String DEPARTMENT_SERVICE = "department-service";

    private WebClient.Builder webClientBuilder;

    public UserServiceImpl(UserRepository userRepository, WebClient.Builder webClientBuilder) {
        this.userRepository = userRepository;
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public ResponseDto getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + userId));

        // Map User entity to UserDto record
        UserDto userDto = new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );

      /*  // Get DepartmentDto from other microservice
        ResponseEntity<DepartmentDto> responseEntity = restTemplate.getForEntity(
                "http://localhost:9090/api/departments/" + user.getDepartmentId(),
                DepartmentDto.class
        );*/

        DepartmentDto departmentDto = getDepartmentByUser(user.getDepartmentId());

       // DepartmentDto departmentDto = responseEntity.getBody();

     //   System.out.println("Department service response: " + responseEntity.getStatusCode());

        // Create final ResponseDto using records
        return new ResponseDto(userDto, departmentDto);
    }

    @CircuitBreaker(name = "department-service", fallbackMethod = "departmentFallback")
    public DepartmentDto getDepartmentByUser(Long departmentId) {
        return webClientBuilder.build()
                .get()
                .uri("http://department-service/api/departments/" + departmentId)
                .retrieve()
                .bodyToMono(DepartmentDto.class)
                .onErrorResume(t -> {
                    System.out.println("Inline fallback: " + t.getMessage());
                    return Mono.just(departmentFallback(departmentId, t));
                })
                .block();
        // Don't catch exception manually
    }

    public DepartmentDto departmentFallback(Long departmentId, Throwable t) {
        System.out.println("Fallback triggered due to: " + t.getMessage());
        return new DepartmentDto(departmentId, "Default Department", "Fallback Address", "123");
    }

    @Override
    @RateLimiter(name = DEPARTMENT_SERVICE, fallbackMethod = "rateLimitFallback")
    public DepartmentDto getDepartmentByUserRate(Long departmentId) {
        return webClientBuilder.build()
                .get()
                .uri("http://department-service/api/departments/" + departmentId)
                .retrieve()
                .bodyToMono(DepartmentDto.class)
                .block();
    }

    @Override
    public void testRateLimiter() {
        for (int i = 0; i < 10; i++) {
            DepartmentDto dto = getDepartmentByUser(1L);
            System.out.println("Response: " + dto.departmentName());
        }
    }

    public DepartmentDto rateLimitFallback(Long departmentId, Throwable t) {
        System.out.println("Rate limiter fallback triggered due to: " + t.getMessage());
        return new DepartmentDto(departmentId, "Rate Limited Dept", "Rate Limited Address", "999");
    }

}
