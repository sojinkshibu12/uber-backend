# Ride-Sharing Backend System
[![Ask DeepWiki](https://devin.ai/assets/askdeepwiki.png)](https://deepwiki.com/sojinkshibu12/uber-backend.git)

## Overview
This repository contains the backend for a ride-sharing application, inspired by Uber. It is built using a microservices architecture, with services communicating asynchronously via Kafka. The system handles ride requests, driver location tracking, and matching riders with nearby drivers.

## Architecture
The system is composed of three core microservices, a message broker, and two databases, all orchestrated via Docker Compose.

*   **Ride Service**: The primary service for managing the lifecycle of a ride. It handles creating ride requests, updating ride status (e.g., started, completed, canceled), and calculating fares. It persists ride data in a MySQL database.
*   **Location Service**: Responsible for tracking the real-time geographic location of drivers. It uses Redis with its geospatial capabilities to efficiently store and query driver locations, providing a list of nearby drivers upon request.
*   **Matching Service**: The core logic engine. It listens for new ride requests and queries the Location Service for nearby drivers. It then orchestrates the process of sending match requests to drivers and handles their responses to find a suitable match for a ride.

### System Components
*   **Backend Services**: Java & Spring Boot
*   **Event Streaming**: Apache Kafka
*   **Driver Location Storage**: Redis
*   **Ride Data Storage**: MySQL
*   **Infrastructure Management**: Docker & Docker Compose

## Event Flow
The primary workflow for booking a ride involves coordination between all three services via Kafka events.

1.  **Ride Request**: `Rideservice` receives a ride request via its API, saves the ride to MySQL with a `MATCHING` status, and publishes a `ride.requested` event to Kafka with the `rideId` as the key.
2.  **Store Request**: `Matchingservice` consumes the `ride.requested` event and stores the ride details in memory, awaiting driver matching.
3.  **Find Nearby Drivers**: A client application calls the `Locationservice` `GET /api/v1/location/nearby?rideId=...` endpoint. The service finds nearby drivers in Redis and publishes a `nearby.drivers` event containing the `rideId` and a list of drivers.
4.  **Send Match Requests**: `Matchingservice` consumes the `nearby.drivers` event. For the given `rideId`, it sends individual match requests to each nearby driver by publishing messages to the `driver.match.requests` topic.
5.  **Driver Response**: A driver's client would respond to the match request. An accepted match is sent to the `Matchingservice` API, which then publishes to the `driver.match.responses` topic.
6.  **Confirm Match**: `Matchingservice` listens for responses. The first driver to accept wins. The service then publishes a `ride.matched` event containing the `rideId` and the selected `driverId`.
7.  **Update Ride**: `Rideservice` consumes the `ride.matched` event, updates the corresponding ride in its MySQL database with the `driverId`, and changes the status to `ACCEPTED`.

## Getting Started

### Prerequisites
*   Git
*   Docker and Docker Compose
*   Java Development Kit (JDK), version 17 or higher
*   Maven

### Setup Instructions

1.  **Clone the Repository**
    ```sh
    git clone https://github.com/sojinkshibu12/uber-backend.git
    cd uber-backend
    ```

2.  **Start Infrastructure Services**
    This command starts Kafka, Zookeeper, Redis, and MySQL using Docker Compose. Ensure Docker is running before executing.
    ```sh
    docker-compose up -d
    ```

3.  **Run the Microservices**
    Open three separate terminal windows, one for each service directory. Run the following command in each to start the Spring Boot applications.

    *   **Location Service**
        ```sh
        cd Locationservice
        ./mvnw spring-boot:run
        ```
        The service will be available on port `8081`.

    *   **Ride Service**
        ```sh
        cd Rideservice
        ./mvnw spring-boot:run
        ```
        The service will be available on port `8082`.

    *   **Matching Service**
        ```sh
        cd Matchingservice
        ./mvnw spring-boot:run
        ```
        The service will be available on port `8083`.

## API Endpoints

### Ride Service (`http://localhost:8082`)

*   **Request a Ride**: `POST /api/v1/rides/request`
*   **Get Ride Details**: `GET /api/v1/rides/{rideid}`
*   **Get All Rides for a Rider**: `GET /api/v1/rides/getallrides/{riderid}`
*   **Start a Ride**: `PUT /api/v1/rides/{rideid}/startride`
*   **Complete a Ride**: `PUT /api/v1/rides/{rideid}/completeride`
*   **Cancel a Ride**: `PUT /api/v1/rides/{rideid}/cancel`

### Location Service (`http://localhost:8081`)

*   **Update Driver Location**: `POST /api/v1/location/drivers/update`
*   **Find Nearby Drivers**: `GET /api/v1/location/nearby` (Params: `rideId`, `latitude`, `longitude`, `radius`)
*   **Remove a Driver**: `DELETE /api/v1/location/drivers/{driverid}`

### Matching Service (`http://localhost:8083`)

*   **Driver Responds to Request**: `POST /api/v1/matching/drivers/respond`

## Kafka Topics
The services communicate through the following Kafka topics:

| Topic                    | Publisher           | Consumer             | Purpose                                                    |
| ------------------------ | ------------------- | -------------------- | ---------------------------------------------------------- |
| `ride.requested`         | `Rideservice`       | `Matchingservice`    | Signals a new ride has been requested.                     |
| `nearby.drivers`         | `Locationservice`   | `Matchingservice`    | Provides a list of available drivers near the rider.       |
| `driver.match.requests`  | `Matchingservice`   | (Driver App)         | Sends a ride offer to a specific driver.                   |
| `driver.match.responses` | `Matchingservice`   | `Matchingservice`    | Communicates a driver's response to a ride offer.          |
| `ride.matched`           | `Matchingservice`   | `Rideservice`        | Confirms a match has been made and provides the driver ID. |
