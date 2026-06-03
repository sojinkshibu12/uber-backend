package com.example.Matchingservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RideMatchedEvent {
  private String rideId;
  private String riderId;
  private String driverId;
}
