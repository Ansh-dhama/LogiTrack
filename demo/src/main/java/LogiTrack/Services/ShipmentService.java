package LogiTrack.Services;

import LogiTrack.Dto.ShipmentDto;
import LogiTrack.Entity.Driver;
import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.TrackingEvent;
import LogiTrack.Entity.User;
import LogiTrack.Enums.Role;
import LogiTrack.Enums.Status;

import LogiTrack.Exceptions.DriverNotFoundException;
import LogiTrack.Exceptions.ShipmentNotFoundException;
import LogiTrack.Exceptions.UserNotFoundException;
import LogiTrack.Repository.DriverRepository;
import LogiTrack.Repository.ShipmentRepository;
import LogiTrack.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import LogiTrack.Dto.DriverLocationUpdateDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final StatusTransitionValidator validator;
    private final TrackingEventService trackingEventService;
    private final  AssigningService assigningService;

    // ✅ 1. Get Shipments
    @Transactional(readOnly = true)
    public List<Shipment> getShipmentsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
        return user.getShipments();
    }

    // ✅ 2. Create Shipment
    @Transactional
    public Shipment createShipment(ShipmentDto dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
        Shipment shipment = new Shipment();

        shipment.setSenderAddress(dto.getSenderAddress());
        shipment.setReceiverAddress(dto.getReceiverAddress());
        shipment.setWeight(dto.getWeight());
        shipment.setTrackingNumber(generateTrackingId());
        shipment.setStatus(Status.CREATED);
        shipment.setUser(user);
        shipment.setDeliveryAttempts(0);

        shipment = shipmentRepository.save(shipment); // ✅ save first

        trackingEventService.logStatus(
                shipment.getTrackingNumber(),
                Status.CREATED,
                user.getRole(),
                user.getId(),
                "SHIPMENT_CREATED"
        );
        assigningService.autoAssignShipments();
        return shipment;
    }
    @Transactional
    public void updateStatus(String trackingNumber, Status newStatus, Long currentUserId, Role currentUserRole) {

        // 1. Find the Shipment
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("shipment not found: " + trackingNumber));

        Status currentStatus = shipment.getStatus();

        // 2. Idempotency Check (If status is same, do nothing)
        if (currentStatus == newStatus) {
            return;
        }

        // 3. Validate Transition (Your Roadmap logic)
        validator.validateTransition(currentStatus, newStatus, currentUserRole);

        // 4. Driver Ownership Check (Security)
        if (currentUserRole == Role.DRIVER) {
            if (shipment.getDriver() == null || !shipment.getDriver().getId().equals(currentUserId)) {
                throw new SecurityException("Access Denied: You are not assigned to this shipment.");
            }
        }

        // 5. "Three Strikes" Logic
        if (newStatus == Status.DELIVERY_ATTEMPTED) {
            int attempts = shipment.getDeliveryAttempts() + 1;
            shipment.setDeliveryAttempts(attempts);

            // If 3rd failure, force status to RETURNED
            if (attempts >= 3) {
                newStatus = Status.RETURNED;
                emailService.sendEmail(shipment.getUser().getEmail(), "Delivery Failed",
                        "Your shipment " + trackingNumber + " is being returned after 3 failed attempts.");
            }
        }

        // 6. Update Shipment Status
        shipment.setStatus(newStatus);
        shipmentRepository.save(shipment);

        trackingEventService.logStatus(
                trackingNumber, newStatus, currentUserRole, currentUserId, "STATUS_CHANGED"
        );

        // 8. Notifications
        if (newStatus == Status.CANCELLED) {
            emailService.sendEmail(shipment.getUser().getEmail(), "Shipment Cancelled", "Your shipment has been cancelled.");
        } else if (newStatus == Status.DELIVERED) {
            emailService.sendEmail(shipment.getUser().getEmail(), "Shipment Delivered", "Your shipment has been delivered.");
        }
    }

    @Transactional
    public void assignDriver(String trackingNumber, Long driverId, Role actorRole, Long actorId) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + trackingNumber));
        if (shipment.getDriver() != null) {
            throw new IllegalStateException("Shipment already assigned to driverId=" + shipment.getDriver().getId());
        }
        Status current = shipment.getStatus();
        if (current == Status.PENDING) {
            current = Status.CREATED; // normalize only in logic
            shipment.setStatus(Status.CREATED); // optional: persist cleanup
        }

        validator.validateTransition(current, Status.ASSIGNED, actorRole);
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + driverId));
        if (!Boolean.TRUE.equals(driver.getIsAvailable())) {
            throw new IllegalStateException("Driver is not available: " + driverId);
        }
        shipment.setDriver(driver);
        shipment.setStatus(Status.ASSIGNED);

        shipmentRepository.save(shipment);

        trackingEventService.logStatus(
                shipment.getTrackingNumber(),
                Status.ASSIGNED,
                actorRole,     // ADMIN / USER / SYSTEM
                actorId,       // who triggered assignment
                "ASSIGNED_TO_DRIVER:" + driverId
        );

        driver.setIsAvailable(false);
        driverRepository.save(driver);
    }


    public void updateDriverLocation(Long shipmentId, DriverLocationUpdateDto locationDto) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found with ID: " + shipmentId));

        shipment.setCurrentLatitude(locationDto.getLatitude());
        shipment.setCurrentLongitude(locationDto.getLongitude());

        if (shipment.getStatus() == Status.PENDING) {
            shipment.setStatus(Status.IN_TRANSIT);
        }

        if (shipment.getDestinationLatitude() != null && shipment.getDestinationLongitude() != null) {
            double distanceKm = calculateDistance(
                    locationDto.getLatitude(), locationDto.getLongitude(),
                    shipment.getDestinationLatitude(), shipment.getDestinationLongitude()
            );

            double speedKmH = 40.0;
            double hoursLeft = distanceKm / speedKmH;
            int totalMinutes = (int) (hoursLeft * 60);

            if (totalMinutes < 60) {
                shipment.setEstimatedTimeArrival(totalMinutes + " mins");
            } else {
                int hours = totalMinutes / 60;
                int mins = totalMinutes % 60;
                shipment.setEstimatedTimeArrival(hours + " hr " + mins + " mins");
            }
        }
          trackingEventService.logLocation(shipment.getTrackingNumber(),shipment.getDriver().getRole(),shipment.getUser().getId(),
                  locationDto.getLatitude(),locationDto.getLongitude());
        shipmentRepository.save(shipment);
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    private String generateTrackingId() {
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Shipment findBytrackingNumber(String trackingNumber) {
        return shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
    }
    @Transactional
    public void driverStartTrip(String trackingNumber, Long driverId) {
        Shipment s1 = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        if (s1.getDriver() == null || !s1.getDriver().getId().equals(driverId)) {
            throw new SecurityException("Not assigned to this driver");
        }
        if (s1.getStatus() != Status.ASSIGNED) {
            throw new IllegalStateException("Trip can start only when shipment is ASSIGNED");
        }

        s1.setStatus(Status.IN_TRANSIT);
        shipmentRepository.save(s1);

        trackingEventService.logStatus(
                trackingNumber,
                Status.IN_TRANSIT,
                Role.DRIVER,
                driverId,
                "DRIVER_START_TRIP"
        );
    }
    private void assignDriverToShipment(Shipment shipment, Driver driver) {
        shipment.setDriver(driver);
        shipment.setStatus(Status.ASSIGNED);
        shipmentRepository.save(shipment);
        trackingEventService.logStatus(
                shipment.getTrackingNumber(),
                Status.ASSIGNED,
                Role.ADMIN,
                0L,                // or null
                "AUTO_ASSIGNED"
        );

        notifyParties(shipment, driver);
    }
    private void notifyParties(Shipment shipment, Driver driver) {
        emailService.sendEmail(shipment.getUser().getEmail(),"Delivery update","Your shipment is assgined to over delivery patner");

        emailService.sendEmail(driver.getEmail(),"New shipment", "You get new shipment details are "+ shipment.getSenderAddress() + " to "
            + shipment.getReceiverAddress());
    }

}