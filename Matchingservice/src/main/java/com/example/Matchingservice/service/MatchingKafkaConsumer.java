package com.example.Matchingservice.service;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.Matchingservice.dto.NearByDriverResponse;
import com.example.Matchingservice.event.DriverMatchResponseEvent;
import com.example.Matchingservice.event.NearbyDriversEvent;
import com.example.Matchingservice.event.RideRequestEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class MatchingKafkaConsumer {
  private final MatchingService matchingService;
  private final JsonMapper jsonMapper;

  @KafkaListener(topics = "ride.requested", groupId = "matching-service-group")
  public void handleRideRequested(ConsumerRecord<String, String> record) throws Exception {
    RideRequestEvent rideRequest = jsonMapper.readValue(record.value(), RideRequestEvent.class);
    matchingService.handleRideRequest(record.key(), rideRequest);
  }

  @KafkaListener(topics = "nearby.drivers", groupId = "matching-service-group")
  public void handleNearbyDrivers(String payload) throws Exception {
    try {
      NearbyDriversEvent event = jsonMapper.readValue(payload, NearbyDriversEvent.class);
      matchingService.handleNearbyDrivers(event);
    } catch (Exception exception) {
      List<NearByDriverResponse> oldFormatDrivers = jsonMapper.readValue(
          payload,
          new TypeReference<List<NearByDriverResponse>>() {});
      log.warn("ignoring legacy nearby.drivers event with {} drivers because it has no rideId", oldFormatDrivers.size());
    }
  }

  @KafkaListener(topics = "driver.match.responses", groupId = "matching-service-group")
  public void handleDriverResponse(String payload) throws Exception {
    DriverMatchResponseEvent response = jsonMapper.readValue(payload, DriverMatchResponseEvent.class);
    matchingService.handleDriverResponse(response);
  }
}
