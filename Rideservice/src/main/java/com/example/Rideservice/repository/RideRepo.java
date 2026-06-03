package com.example.Rideservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Rideservice.model.Ride;

public interface RideRepo extends JpaRepository<Ride, String> {
  List<Ride> findByRiderId(String riderId);
}
