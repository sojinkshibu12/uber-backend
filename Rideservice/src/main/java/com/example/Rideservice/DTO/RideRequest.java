package com.example.Rideservice.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RideRequest {
  @NotBlank(message = "riderId is required")
  private String riderId;

  @NotNull(message = "pickupLatitude is required")
  private double pickupLatitude;

  @NotNull(message = "pickupLongitude is required")
  private double pickupLongitude;

  @NotBlank(message = "pickupAddress is required")
  private String pickupAddress;

  @NotNull(message = "dropLatitudeis required")
  private double dropLatitude;

  @NotNull(message = "dropLongitude is required")
  private double dropLongitude;

  @NotBlank(message = "dropAdress is required")
  private String dropAdress;

}
