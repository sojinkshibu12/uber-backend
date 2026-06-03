package com.example.Matchingservice.service;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.Matchingservice.dto.NearByDriverResponse;
import com.example.Matchingservice.event.DriverMatchRequestEvent;
import com.example.Matchingservice.event.DriverMatchResponseEvent;
import com.example.Matchingservice.event.NearbyDriversEvent;
import com.example.Matchingservice.event.RideMatchedEvent;
import com.example.Matchingservice.event.RideRequestEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchingService {
  private static final String DRIVER_MATCH_REQUESTS_TOPIC = "driver.match.requests";
  private static final String RIDE_MATCHED_TOPIC = "ride.matched";

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final JsonMapper jsonMapper;
  private final Queue<String> waitingRideIds = new ArrayDeque<>();
  private final Map<String, RideRequestEvent> ridesById = new HashMap<>();
  private final Map<String, Set<String>> requestedDriversByRideId = new HashMap<>();
  private final Set<String> matchedRideIds = new HashSet<>();

  public synchronized void handleRideRequest(String rideId, RideRequestEvent rideRequest) {
    if (rideId == null || rideId.isBlank()) {
      log.warn("ride request ignored because kafka message key rideId is missing");
      return;
    }

    ridesById.put(rideId, rideRequest);
    waitingRideIds.add(rideId);
    log.info("ride request {} stored for matching", rideId);
  }

  public synchronized void handleNearbyDrivers(NearbyDriversEvent event) {
    if (event == null || event.getRideId() == null || event.getRideId().isBlank()) {
      log.warn("nearby drivers event ignored because rideId is missing");
      return;
    }

    List<NearByDriverResponse> nearbyDrivers = event.getDrivers();
    if (nearbyDrivers == null || nearbyDrivers.isEmpty()) {
      log.info("nearby drivers event ignored for ride {} because list is empty", event.getRideId());
      return;
    }

    RideRequestEvent rideRequest = ridesById.get(event.getRideId());
    if (rideRequest == null) {
      log.warn("nearby drivers event received for unknown ride {}", event.getRideId());
      return;
    }

    Set<String> requestedDrivers = requestedDriversByRideId.computeIfAbsent(event.getRideId(), key -> new HashSet<>());

    for (NearByDriverResponse driver : nearbyDrivers) {
      if (driver.getDriverid() == null || requestedDrivers.contains(driver.getDriverid())) {
        continue;
      }

      DriverMatchRequestEvent request = buildDriverRequest(event.getRideId(), rideRequest, driver);
      kafkaTemplate.send(DRIVER_MATCH_REQUESTS_TOPIC, driver.getDriverid(), toJson(request));
      requestedDrivers.add(driver.getDriverid());
      log.info("match request sent to driver {} for ride {}", driver.getDriverid(), event.getRideId());
    }
  }

  public synchronized void handleDriverResponse(DriverMatchResponseEvent response) {
    if (response == null || response.getRideId() == null || response.getDriverId() == null) {
      log.warn("driver match response ignored because rideId or driverId is missing");
      return;
    }

    if (!response.isAccepted()) {
      log.info("driver {} rejected ride {}", response.getDriverId(), response.getRideId());
      return;
    }

    if (matchedRideIds.contains(response.getRideId())) {
      log.info("driver {} accepted ride {}, but ride is already matched", response.getDriverId(), response.getRideId());
      return;
    }

    RideRequestEvent rideRequest = ridesById.get(response.getRideId());
    if (rideRequest == null) {
      log.warn("driver {} accepted unknown ride {}", response.getDriverId(), response.getRideId());
      return;
    }

    matchedRideIds.add(response.getRideId());
    waitingRideIds.remove(response.getRideId());

    RideMatchedEvent event = RideMatchedEvent.builder()
        .rideId(response.getRideId())
        .riderId(rideRequest.getRiderId())
        .driverId(response.getDriverId())
        .build();

    kafkaTemplate.send(RIDE_MATCHED_TOPIC, response.getRideId(), toJson(event));
    log.info("ride {} matched with driver {}", response.getRideId(), response.getDriverId());
  }

  private DriverMatchRequestEvent buildDriverRequest(
      String rideId,
      RideRequestEvent rideRequest,
      NearByDriverResponse driver) {
    return DriverMatchRequestEvent.builder()
        .rideId(rideId)
        .riderId(rideRequest.getRiderId())
        .driverId(driver.getDriverid())
        .pickupLatitude(rideRequest.getPickupLatitude())
        .pickupLongitude(rideRequest.getPickupLongitude())
        .pickupAddress(rideRequest.getPickupAddress())
        .dropLatitude(rideRequest.getDropLatitude())
        .dropLongitude(rideRequest.getDropLongitude())
        .dropAdress(rideRequest.getDropAdress())
        .driverLatitude(driver.getLatitude())
        .driverLongitude(driver.getLongitude())
        .distancekm(driver.getDistancekm())
        .build();
  }

  private String toJson(Object event) {
    try {
      return jsonMapper.writeValueAsString(event);
    } catch (Exception exception) {
      throw new RuntimeException("failed to serialize kafka event", exception);
    }
  }
}
