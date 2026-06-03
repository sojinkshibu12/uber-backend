package com.example.Rideservice.DTO;

import java.time.LocalDateTime;

import com.example.Rideservice.model.Ridestatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RideResponse {
  private String Id;
  private String riderId;
  private String driverId;
  private double pickupLatitude;
  private double pickupLongitude;
  private String pickupAddress;
  private double dropLatitude;
  private double dropLongitude;
  private String dropAdress;
  private Ridestatus status;
  private double estimatedFare;
  private double actualFare;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime rideStartedTime;
  private LocalDateTime rideEndedTime;

}
