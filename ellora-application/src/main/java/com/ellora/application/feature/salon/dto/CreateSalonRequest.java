package com.ellora.application.feature.salon.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSalonRequest(
    @NotBlank(message = "Tên salon không được để trống")
    @Size(max = 255, message = "Tên salon tối đa 255 ký tự")
    String name,

    @NotBlank(message = "Địa chỉ không được để trống")
    String address,

    String city,
    String district,
    String phone,
    String email,
    String description,
    Double latitude,
    Double longitude
) {
    public CreateSalonRequest(String name, String address, String city, String district, String phone, String email, String description) {
        this(name, address, city, district, phone, email, description, null, null);
    }
}
