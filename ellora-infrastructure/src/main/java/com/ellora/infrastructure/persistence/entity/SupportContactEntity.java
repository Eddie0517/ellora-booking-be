package com.ellora.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "platform_support_contacts")
@Getter
@Setter
public class SupportContactEntity {
    @Id
    private Long id = 1L;

    @Column(nullable = false, length = 254)
    private String email = "";

    @Column(nullable = false, length = 30)
    private String phone = "";

    @Column(name = "facebook_url", nullable = false, length = 500)
    private String facebookUrl = "";
}
