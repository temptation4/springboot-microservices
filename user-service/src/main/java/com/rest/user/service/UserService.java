package com.rest.user.service;

import com.rest.user.dto.DepartmentDto;
import com.rest.user.dto.ResponseDto;
import com.rest.user.entity.User;

public interface UserService {
    User saveUser(User user);

    ResponseDto getUser(Long userId);
    public DepartmentDto getDepartmentByUserRate(Long departmentId);
    public void testRateLimiter();
}
