package com.example.Matchingservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Matchingservice.event.DriverMatchResponseEvent;
import com.example.Matchingservice.service.MatchingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/matching")
@RequiredArgsConstructor
public class DriverMatchController {
  private final MatchingService matchingService;

  @PostMapping("drivers/respond")
  public ResponseEntity<String> respondToMatchRequest(@RequestBody DriverMatchResponseEvent response) {
    matchingService.handleDriverResponse(response);
    return ResponseEntity.ok("driver response received");
  }
}
