package com.example.Rideservice.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.example.Rideservice.event.RideMatchedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class RideMatchedConsumer {
  private final RideService rideService;
  private final JsonMapper jsonMapper;

  @KafkaListener(topics = "ride.matched", groupId = "ride-service-group")
  public void handleRideMatched(String payload) throws Exception {
    RideMatchedEvent event = jsonMapper.readValue(payload, RideMatchedEvent.class);
    rideService.updateRideWithDriver(event.getRideId(), event.getDriverId());
    log.info("ride {} updated with matched driver {}", event.getRideId(), event.getDriverId());
  }
}
