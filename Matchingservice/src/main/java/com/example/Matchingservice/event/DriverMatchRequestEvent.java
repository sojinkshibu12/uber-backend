package com.example.Matchingservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverMatchRequestEvent {
  private String rideId;
  private String riderId;
  private String driverId;
  private double pickupLatitude;
  private double pickupLongitude;
  private String pickupAddress;
  private double dropLatitude;
  private double dropLongitude;
  private String dropAdress;
  private Double driverLatitude;
  private Double driverLongitude;
  private Double distancekm;
}
