package com.ellora.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "salon_closures")
@Getter
@Setter
public class SalonClosureEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "salon_id", nullable = false) private Long salonId;
    @Column(name = "closed_date", nullable = false) private LocalDate date;
    @Column(length = 500) private String reason;
}
