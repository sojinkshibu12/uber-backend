package com.example.Rideservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideRequestEvent {
  private String riderId;
  private String driverId;
  private double pickupLatitude;
  private double pickupLongitude;
  private String pickupAddress;
  private double dropLatitude;
  private double dropLongitude;
  private String dropAdress;

}
