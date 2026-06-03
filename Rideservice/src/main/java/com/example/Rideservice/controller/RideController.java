package com.example.Rideservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.Rideservice.service.RideService;

import jakarta.validation.Valid;

import com.example.Rideservice.DTO.*;

@RestController
@RequestMapping("/api/v1/rides")
@Slf4j
@RequiredArgsConstructor
public class RideController {
  private final RideService service;

  @PostMapping("/request")
  public ResponseEntity<RideResponse> requestrider(@Valid @RequestBody RideRequest request) {
    log.info("getting request from riderid : {}", request.getRiderId());

    return ResponseEntity.status(HttpStatus.CREATED).body(
        service.RequestRide(request));
  }

  @GetMapping("/{rideid}")
  public ResponseEntity<RideResponse> getride(@PathVariable String rideid) {
    return ResponseEntity.ok(service.GetRide(rideid));

  }

  @GetMapping("/getallrides/{riderid}")
  public ResponseEntity<List<RideResponse>> getallride(@PathVariable String riderid) {
    return ResponseEntity.ok(service.GetAllRide(riderid));
  }

  @PutMapping("{rideid}/startride")
  public ResponseEntity<RideResponse> startride(@PathVariable String rideid) {
    return ResponseEntity.ok(service.StartRide(rideid));
  }

  @PutMapping("{rideid}/completeride")
  public ResponseEntity<RideResponse> completeride(@PathVariable String rideid) {
    return ResponseEntity.ok(service.CompleteRide(rideid));
  }

  @PutMapping("{rideid}/cancel")
  public ResponseEntity<RideResponse> cancelride(@PathVariable String rideid) {
    return ResponseEntity.ok(service.CancelRide(rideid));
  }

}
