package com.rest.user.dto;

public record DepartmentDto(
        Long id,
        String departmentName,
        String departmentAddress,
        String departmentCode
) {}
