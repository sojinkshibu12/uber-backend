package com.example.Rideservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

import com.example.Rideservice.DTO.*;
import com.example.Rideservice.model.*;
import com.example.Rideservice.repository.*;
import com.example.Rideservice.event.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {
  private final RideRepo repo;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final JsonMapper jsonMapper;
  private final String RIDE_REQUESTED_TOPIC = "ride.requested";

  public RideResponse RequestRide(RideRequest request) {
    Ride ride = Ride.builder()
        .riderId(request.getRiderId())
        .pickupLatitude(request.getPickupLatitude())
        .pickupLongitude(request.getPickupLongitude())
        .pickupAddress(request.getPickupAddress())
        .dropLatitude(request.getDropLatitude())
        .dropLongitude(request.getDropLongitude())
        .dropAdress(request.getDropAdress())
        .driverId("")
        .estimatedFare(estimatefare(request))
        .status(Ridestatus.MATCHING)
        .build();
    Ride savedride = repo.save(ride);
    RideRequestEvent event = new RideRequestEvent(
        savedride.getRiderId(),
        savedride.getDriverId(),
        savedride.getPickupLatitude(),
        savedride.getPickupLongitude(),
        savedride.getPickupAddress(),
        savedride.getDropLatitude(),
        savedride.getDropLongitude(),
        savedride.getDropAdress());

    kafkaTemplate.send(RIDE_REQUESTED_TOPIC, savedride.getId(), toJson(event));
    log.info("RideRequestEvent published to kafka for ride {}", savedride.getId());
    return mapToRideResponse(savedride);
  }

  public static void estimatefare() {

  };

  // -----------------------------------------------------

  // it is called by matching servie ....

  public void updateRideWithDriver(String rideid, String driverid) {
    Ride ride = repo.findById(rideid)
        .orElseThrow(() -> new RuntimeException("ride not found"));

    ride.setStatus(Ridestatus.ACCEPTED);
    ride.setDriverId(driverid);
    repo.save(ride);
  }
  // -------------------------------------------------------

  public RideResponse StartRide(String rideid) {
    Ride ride = repo.findById(rideid)
        .orElseThrow(() -> new RuntimeException("ride not found"));
    if (ride.getStatus() != Ridestatus.ACCEPTED) {
      throw new RuntimeException("cannot start ride because ride not accepted");
    }

    ride.setStatus(Ridestatus.RIDE_STARTED);

    repo.save(ride);
    return mapToRideResponse(ride);
  }

  public RideResponse CompleteRide(String rideid) {
    Ride ride = repo.findById(rideid)
        .orElseThrow(() -> new RuntimeException("ride not found"));
    if (ride.getStatus() != Ridestatus.RIDE_STARTED) {
      throw new RuntimeException("cannot complete ride because ride not started");
    }

    ride.setStatus(Ridestatus.COMPLETED);

    repo.save(ride);
    return mapToRideResponse(ride);
  }

  public RideResponse GetRide(String rideid) {
    Ride ride = repo.findById(rideid)
        .orElseThrow(() -> new RuntimeException("ride not found"));
    return mapToRideResponse(ride);

  }

  public List<RideResponse> GetAllRide(String riderid) {
    List<Ride> rides = new ArrayList<>();
    rides = repo.findByRiderId(riderid);
    List<RideResponse> newrides = new ArrayList<>();

    for (Ride ride : rides) {
      RideResponse r = mapToRideResponse(ride);
      newrides.add(r);
    }

    return newrides;
  }

  public RideResponse CancelRide(String rideid) {
    Ride ride = repo.findById(rideid)
        .orElseThrow(() -> new RuntimeException("ride not found"));

    ride.setStatus(Ridestatus.CANCELLED);
    repo.save(ride);
    return mapToRideResponse(ride);

  }

  public static RideResponse mapToRideResponse(Ride ride) {
    return RideResponse.builder()
        .Id(ride.getId())
        .riderId(ride.getRiderId())
        .driverId(ride.getDriverId())
        .pickupLatitude(ride.getPickupLatitude())
        .pickupLongitude(ride.getPickupLongitude())
        .pickupAddress(ride.getPickupAddress())
        .dropLatitude(ride.getDropLatitude())
        .dropLongitude(ride.getDropLongitude())
        .dropAdress(ride.getDropAdress())
        .status(ride.getStatus())
        .estimatedFare(ride.getEstimatedFare())
        .actualFare(ride.getActualFare())
        .createdAt(ride.getCreatedAt())
        .updatedAt(ride.getUpdatedAt())
        .rideStartedTime(ride.getRideStartedTime())
        .rideEndedTime(ride.getRideEndedTime())
        .build();
  }

  private double estimatefare(RideRequest request) {
    // Simplified Haversine distance calculation
    double lat1 = Math.toRadians(request.getPickupLatitude());
    double lat2 = Math.toRadians(request.getDropLatitude());

    double lon1 = Math.toRadians(request.getPickupLongitude());
    double lon2 = Math.toRadians(request.getDropLongitude());

    double dLat = lat2 - lat1;
    double dLon = lon2 - lon1;

    double a = Math.pow(Math.sin(dLat / 2), 2)
        + Math.cos(lat1) * Math.cos(lat2)
            * Math.pow(Math.sin(dLon / 2), 2);

    double c = 2 * Math.asin(Math.sqrt(a));
    double dustanceKm = 6371 * c;

    // Base fare: 50Rs + 12Rs. perKm
    double fare = 50 + (dustanceKm * 12);
    return Math.round(fare * 100.0) / 100.0;
  }

  private String toJson(Object event) {
    try {
      return jsonMapper.writeValueAsString(event);
    } catch (Exception exception) {
      throw new RuntimeException("failed to serialize kafka event", exception);
    }
  }
}
