package com.ellora.application.feature.salon.dto;

public record EmployeeResponse(
    Long id,
    Long salonId,
    String fullName,
    String phone,
    String avatarUrl,
    String bio,
    boolean active
) {}
