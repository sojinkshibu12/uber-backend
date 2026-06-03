package com.example.Matchingservice.event;

import java.util.List;

import com.example.Matchingservice.dto.NearByDriverResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class NearbyDriversEvent {
  private String rideId;
  private List<NearByDriverResponse> drivers;
}
