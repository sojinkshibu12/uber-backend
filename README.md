# Uber Backend Runtime Test Notes

Test date: 2026-06-03

## Services Tested

All three services compile and their Spring context tests pass:

- `Rideservice`: `./mvnw test` passed
- `Locationservice`: `./mvnw test` passed
- `Matchingservice`: `./mvnw test` passed

## Issues Fixed

### 1. Ride request failed before Kafka publishing

Previous error:

```text
Column 'driver_id' cannot be null
```

Fixes applied:

- `Ride.driverId` is no longer marked as a non-null JPA column.
- New rides are created with an empty `driverId` placeholder so the already-created MySQL schema with `driver_id NOT NULL` can still accept the insert.
- New rides are saved directly with status `MATCHING`.
- `RideRequestEvent` is now serialized manually as JSON text and published to `ride.requested`.
- `RideResponse` now includes the generated ride id.

### 2. Location driver id JSON binding used uppercase field name

Previous behavior:

```text
updating location for driver:null
```

Fix applied:

- `DriverLocationRequest` now uses `driverid` instead of `Driverid`.
- `NearByDriverResponse` now also uses `driverid` consistently.

### 3. Nearby drivers endpoint was not exposed over HTTP

Previous behavior:

```text
GET /api/v1/location/nearby -> 404
```

Fix applied:

- Added `GET /api/v1/location/nearby`.
- It accepts `rideId`, `latitude`, `longitude`, and optional `radius`.
- It returns the nearby drivers list and publishes the correlated Kafka event.

Example:

```bash
curl 'http://localhost:8081/api/v1/location/nearby?rideId=<ride-id>&latitude=12.9716&longitude=77.5946&radius=5'
```

### 4. Kafka JsonSerializer failed with Jackson class mismatch

Previous error:

```text
NoClassDefFoundError: com/fasterxml/jackson/databind/JavaType
```

Fixes applied:

- `Rideservice`, `Locationservice`, and `Matchingservice` now use Kafka `StringSerializer` for producer values.
- Consumers use `StringDeserializer`.
- Services manually serialize and deserialize event JSON with Spring Boot 4's `tools.jackson.databind.json.JsonMapper`.

This avoids the Spring Kafka `JsonSerializer` dependency on old Jackson 2 classes.

### 5. Nearby driver matching correlation was unsafe

Previous behavior:

- `nearby.drivers` contained only a list of drivers.
- Matchingservice paired it with the oldest pending ride, which could be wrong when multiple rides were active.

Fixes applied:

- `nearby.drivers` now publishes an event object with `rideId` and `drivers`.
- Matchingservice matches nearby drivers using the event `rideId`.
- Matchingservice also tolerates old list-only `nearby.drivers` messages already present in Kafka and ignores them instead of retrying forever.

New event shape:

```json
{
  "rideId": "...",
  "drivers": [
    {
      "driverid": "driver-1",
      "latitude": 12.9716,
      "longitude": 77.5946,
      "distancekm": 0.2
    }
  ]
}
```

### 6. Ride matched event was not applied back to Rideservice

Fix applied:

- Added a `ride.matched` Kafka consumer in Rideservice.
- When Matchingservice publishes an accepted match, Rideservice updates the ride with the selected driver and status `ACCEPTED`.

### 7. Docker compose obsolete version warning

Previous warning:

```text
the attribute `version` is obsolete
```

Fix applied:

- Removed the top-level `version` attribute from `docker-compose.yml`.

## Current Flow

1. Rideservice creates a ride and publishes `ride.requested` with the ride id as Kafka key.
2. Matchingservice consumes `ride.requested` and stores the ride by id.
3. Locationservice `GET /api/v1/location/nearby?rideId=...` finds nearby drivers and publishes `nearby.drivers` with the same `rideId`.
4. Matchingservice consumes `nearby.drivers`, sends requests to each nearby driver through `driver.match.requests`, and waits for `driver.match.responses`.
5. First accepted driver wins. Matchingservice publishes `ride.matched`.
6. Rideservice consumes `ride.matched` and updates the ride with the selected driver.
