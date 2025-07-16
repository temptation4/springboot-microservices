package com.rest.user.controller;

import com.rest.user.dto.DepartmentDto;
import com.rest.user.dto.ResponseDto;
import com.rest.user.entity.User;
import com.rest.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "User Service is running";
    }

    @PostMapping("/save")
    public ResponseEntity<User> saveUser(@RequestBody User user){
        User savedUser = userService.saveUser(user);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @GetMapping("{id}")
    public ResponseEntity<ResponseDto> getUser(@PathVariable("id") Long userId){
        ResponseDto responseDto = userService.getUser(userId);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/test-rate")
    public ResponseEntity<String> testRateLimiter() {
        userService.testRateLimiter();
        return ResponseEntity.ok("Rate limiter test completed");
    }

}
