package com.rest.user.dto;

public record ResponseDto(
        UserDto user,
        DepartmentDto department
) {}
