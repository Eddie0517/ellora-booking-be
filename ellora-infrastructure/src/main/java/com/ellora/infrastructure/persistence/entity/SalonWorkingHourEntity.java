package com.ellora.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "salon_working_hours")
@Getter
@Setter
public class SalonWorkingHourEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "salon_id", nullable = false) private Long salonId;
    @Column(name = "day_of_week", nullable = false) private String dayOfWeek;
    @Column(name = "open_time") private LocalTime openTime;
    @Column(name = "close_time") private LocalTime closeTime;
    @Column(nullable = false) private boolean closed;
}
