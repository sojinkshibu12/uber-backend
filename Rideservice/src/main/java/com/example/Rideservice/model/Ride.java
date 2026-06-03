package com.example.Rideservice.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rides")

public class Ride {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String Id;

  @Column(nullable = false)
  private String riderId;

  private String driverId;

  @Column(nullable = false)
  private double pickupLatitude;

  @Column(nullable = false)
  private double pickupLongitude;

  @Column(nullable = false)
  private String pickupAddress;

  @Column(nullable = false)
  private double dropLatitude;

  @Column(nullable = false)
  private double dropLongitude;

  @Column(nullable = false)
  private String dropAdress;

  @Enumerated(EnumType.STRING)
  private Ridestatus status;

  private double estimatedFare;

  private double actualFare;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  private LocalDateTime rideStartedTime;
  private LocalDateTime rideEndedTime;

}
