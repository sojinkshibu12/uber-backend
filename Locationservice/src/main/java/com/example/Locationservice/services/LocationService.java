package com.example.Locationservice.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.Locationservice.DTO.DriverLocationRequest;
import com.example.Locationservice.DTO.NearByDriverResponse;
import com.example.Locationservice.DTO.NearbyDriversEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {
  private final RedisTemplate<String, String> redisTemplate;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final JsonMapper jsonMapper;
  private static final String DRIVERS_GEO_KEY = "drivers:locations";
  private static final String NEARBY_DRIVERS_TOPIC = "nearby.drivers";

  public void updatelocation(DriverLocationRequest request) {
    log.info("updating location for driver:{}", request.getDriverid());
    Point driverpoint = new Point(request.getLongitude(), request.getLatitude());

    redisTemplate.opsForGeo().add(DRIVERS_GEO_KEY, driverpoint, request.getDriverid());
    log.info("updated location for driver:{}", request.getDriverid());
  }

  public List<NearByDriverResponse> findnearbydrivers(
      double latitude,
      double longitude,
      double radiuskm) {
    return findnearbydrivers(null, latitude, longitude, radiuskm);
  }

  // it find the nearby drivers from the location of the user......
  public List<NearByDriverResponse> findnearbydrivers(
      String rideId,
      double latitude,
      double longitude,
      double radiuskm) {
    log.info("finding drivers within radius {}", radiuskm);
    Circle searchradius = new Circle(
        new Point(longitude, latitude),
        new Distance(radiuskm, Metrics.KILOMETERS));
    GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(
        DRIVERS_GEO_KEY,
        searchradius,
        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
            .includeCoordinates()
            .includeDistance()
            .sortAscending()
            .limit(10));
    List<NearByDriverResponse> nearbydrivers = new ArrayList<>();
    if (results != null) {
      results.getContent().forEach(result -> {
        RedisGeoCommands.GeoLocation<String> location = result.getContent();

        String driverId = location.getName();
        Point point = location.getPoint();
        Distance distance = result.getDistance();

        NearByDriverResponse response = new NearByDriverResponse();
        response.setDriverid(driverId);
        response.setLatitude(point.getY());
        response.setLongitude(point.getX());
        response.setDistancekm(distance.getValue());
        nearbydrivers.add(response);
      });
    }

    log.info("there are {} nearby drivers", nearbydrivers.size());
    publishNearbyDrivers(rideId, nearbydrivers);
    return nearbydrivers;
  }

  public void removedriver(String driverid) {
    redisTemplate.opsForGeo().remove(DRIVERS_GEO_KEY, driverid);
    log.info("{} driver removed ", driverid);
  }

  private void publishNearbyDrivers(String rideId, List<NearByDriverResponse> nearbydrivers) {
    try {
      NearbyDriversEvent event = NearbyDriversEvent.builder()
          .rideId(rideId)
          .drivers(nearbydrivers)
          .build();
      kafkaTemplate.send(NEARBY_DRIVERS_TOPIC, rideId, jsonMapper.writeValueAsString(event));
      log.info("nearby drivers event published to kafka for ride {} with {} drivers", rideId, nearbydrivers.size());
    } catch (Exception exception) {
      throw new RuntimeException("failed to publish nearby drivers event", exception);
    }
  }
}
