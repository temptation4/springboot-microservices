package com.rest.user.dto;

public record UserDto(
        Long id,
        String firstName,
        String lastName,
        String email
) {}
