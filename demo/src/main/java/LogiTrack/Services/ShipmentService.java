package LogiTrack.Services;

import LogiTrack.Dto.DriverLocationUpdateDto;
import LogiTrack.Dto.ShipmentDto;
import LogiTrack.Entity.Driver;
import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.TrackingUpdate;
import LogiTrack.Entity.User;
import LogiTrack.Enums.Role;
import LogiTrack.Enums.Status;
import LogiTrack.Exceptions.DriverNotFoundException;
import LogiTrack.Exceptions.ShipmentNotFoundException;
import LogiTrack.Exceptions.UserNotFoundException;
import LogiTrack.Repository.DriverRepository;
import LogiTrack.Repository.ShipmentRepository;
import LogiTrack.Repository.TrackingRepository;
import LogiTrack.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TrackingRepository trackingRepository;
    private final StatusTransitionValidator validator;

    @Transactional(readOnly = true)
    public List<Shipment> getShipmentsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
        return user.getShipments();
    }

    @Transactional
    public Shipment createShipment(ShipmentDto dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        Shipment shipment = new Shipment();
        shipment.setSenderAddress(dto.getSenderAddress());
        shipment.setReceiverAddress(dto.getReceiverAddress());
        shipment.setWeight(dto.getWeight());
        shipment.setTrackingNumber(generateTrackingId());
        shipment.setStatus(Status.PENDING);
        shipment.setUser(user);
        shipment.setDeliveryAttempts(0);

        if (dto.getDriverId() != null) {
            Driver driver = driverRepository.findById(dto.getDriverId())
                    .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + dto.getDriverId()));
            shipment.setDriver(driver);
            shipment.setStatus(Status.ASSIGNED);
        }

        return shipmentRepository.save(shipment);
    }

    @Transactional
    public void updateStatus(String trackingNumber, Status newStatus, Long currentUserId, Role currentUserRole) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + trackingNumber));

        Status currentStatus = shipment.getStatus();
        if (currentStatus == newStatus) {
            return;
        }

        validator.validateTransition(currentStatus, newStatus, currentUserRole);

        if (currentUserRole == Role.DRIVER) {
            if (shipment.getDriver() == null || !shipment.getDriver().getId().equals(currentUserId)) {
                throw new AccessDeniedException("You are not assigned to this shipment.");
            }
        }

        if (newStatus == Status.DELIVERY_ATTEMPTED) {
            int attempts = shipment.getDeliveryAttempts() + 1;
            shipment.setDeliveryAttempts(attempts);

            if (attempts >= 3) {
                newStatus = Status.RETURNED;
                emailService.sendEmail(shipment.getUser().getEmail(), "Delivery Failed",
                        "Your shipment " + trackingNumber + " is being returned after 3 failed attempts.");
            }
        }

        shipment.setStatus(newStatus);
        shipmentRepository.save(shipment);

        TrackingUpdate historyLog = trackingRepository.findById(trackingNumber)
                .orElseGet(() -> {
                    TrackingUpdate newLog = new TrackingUpdate();
                    newLog.setTrackingNumber(trackingNumber);
                    newLog.setShipment(shipment);
                    newLog.setCreationTime(LocalDateTime.now());
                    return newLog;
                });

        LocalDateTime now = LocalDateTime.now();
        historyLog.setLastUpdate(now);
        historyLog.setStatus(newStatus);
        historyLog.getUpdates().put(now, newStatus);
        trackingRepository.save(historyLog);

        if (newStatus == Status.CANCELLED) {
            emailService.sendEmail(shipment.getUser().getEmail(), "Shipment Cancelled",
                    "Your shipment has been cancelled.");
        } else if (newStatus == Status.DELIVERED) {
            emailService.sendEmail(shipment.getUser().getEmail(), "Shipment Delivered",
                    "Your shipment has been delivered.");
        }
    }

    @Transactional
    public void assignDriver(Long shipmentId, Long driverId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + shipmentId));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + driverId));

        shipment.setDriver(driver);
        if (shipment.getStatus() == Status.PENDING || shipment.getStatus() == Status.CREATED) {
            shipment.setStatus(Status.ASSIGNED);
        }
        shipmentRepository.save(shipment);
    }

    @Transactional
    public void updateDriverLocation(
            Long shipmentId,
            DriverLocationUpdateDto locationDto,
            Long currentUserId,
            Role currentUserRole
    ) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found with ID: " + shipmentId));

        if (currentUserRole == Role.DRIVER) {
            if (shipment.getDriver() == null || !shipment.getDriver().getId().equals(currentUserId)) {
                throw new AccessDeniedException("You are not assigned to this shipment.");
            }
        } else if (currentUserRole != Role.ADMIN) {
            throw new AccessDeniedException("Only the assigned driver or an admin can update shipment location.");
        }

        shipment.setCurrentLatitude(locationDto.getLatitude());
        shipment.setCurrentLongitude(locationDto.getLongitude());

        if (shipment.getStatus() == Status.PENDING || shipment.getStatus() == Status.ASSIGNED) {
            shipment.setStatus(Status.IN_TRANSIT);
        }

        if (shipment.getDestinationLatitude() != null && shipment.getDestinationLongitude() != null) {
            double distanceKm = calculateDistance(
                    locationDto.getLatitude(), locationDto.getLongitude(),
                    shipment.getDestinationLatitude(), shipment.getDestinationLongitude()
            );

            double speedKmH = 40.0;
            int totalMinutes = (int) ((distanceKm / speedKmH) * 60);

            if (totalMinutes < 60) {
                shipment.setEstimatedTimeArrival(totalMinutes + " mins");
            } else {
                int hours = totalMinutes / 60;
                int mins = totalMinutes % 60;
                shipment.setEstimatedTimeArrival(hours + " hr " + mins + " mins");
            }
        }

        shipmentRepository.save(shipment);
    }

    public Shipment getShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + id));
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double radiusKm = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radiusKm * c;
    }

    private String generateTrackingId() {
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
