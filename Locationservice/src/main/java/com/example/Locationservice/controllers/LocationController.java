package com.example.Locationservice.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Locationservice.DTO.DriverLocationRequest;
import com.example.Locationservice.DTO.NearByDriverResponse;
import com.example.Locationservice.services.LocationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/location")
@Slf4j
@RequiredArgsConstructor
public class LocationController {
  private final LocationService locationService;

  // this is called every 3 seconds
  @PostMapping("drivers/update")
  public ResponseEntity<String> updatedriver(@RequestBody DriverLocationRequest request) {
    locationService.updatelocation(request);
    return ResponseEntity.ok("location updated");
  }

  // matching service call this to find the nearest drivers
  @GetMapping("/nearby")
  public ResponseEntity<List<NearByDriverResponse>> getnearbydrivers(
      @RequestParam(required = false) String rideId,
      @RequestParam double latitude,
      @RequestParam double longitude,
      @RequestParam(defaultValue = "5.0") double radius) {
    return ResponseEntity.ok(locationService.findnearbydrivers(rideId, latitude, longitude, radius));
  }

  @DeleteMapping("drivers/{driverid}")
  public ResponseEntity<String> removedriver(@PathVariable String driverid) {
    locationService.removedriver(driverid);
    return ResponseEntity.ok("Removed driver " + driverid);
  }
}
