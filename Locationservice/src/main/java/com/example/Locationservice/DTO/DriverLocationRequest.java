package com.example.Locationservice.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DriverLocationRequest {
  private String driverid;
  private Double latitude;
  private Double longitude;
}
