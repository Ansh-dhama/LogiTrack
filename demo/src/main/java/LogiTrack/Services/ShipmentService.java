package LogiTrack.Services;

import LogiTrack.Dto.ShipmentDto;
import LogiTrack.Entity.Driver;
import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.TrackingUpdate;
import LogiTrack.Entity.User;
import LogiTrack.Enums.Role;
import LogiTrack.Enums.Status;
import LogiTrack.Exceptions.*;
import LogiTrack.Repository.DriverRepository;
import LogiTrack.Repository.ShipmentRepository;
import LogiTrack.Repository.TrackingRepository;
import LogiTrack.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        shipment.setStatus(Status.PENDING); // Default status
        shipment.setUser(user);
        shipment.setDeliveryAttempts(0); // Initialize attempts

        // Assign Driver if provided
        if (dto.getDriverId() != null) {
            Driver driver = driverRepository.findById(dto.getDriverId())
                    .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + dto.getDriverId()));
            shipment.setDriver(driver);
            shipment.setStatus(Status.ASSIGNED); // Auto-update status if driver is assigned immediately
        }

        return shipmentRepository.save(shipment);
    }

    // ✅ 3. Update Status (The Secure, Validated Version)
    @Transactional
    public void updateStatus(String trackingNumber, Status newStatus, Long currentUserId, Role currentUserRole) {

        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + trackingNumber));

        Status currentStatus = shipment.getStatus();

        // 1. Idempotency Check (Optimization)
        if (currentStatus == newStatus) {
            return;
        }

        // 2. Validate Transition (The Roadmap)
        validator.validateTransition(currentStatus, newStatus, currentUserRole);

        // 3. Driver Ownership Check (Security)
        if (currentUserRole == Role.DRIVER) {
            if (shipment.getDriver() == null || !shipment.getDriver().getId().equals(currentUserId)) {
                throw new SecurityException("Access Denied: You are not assigned to this shipment.");
            }
        }

        // 4. "Three Strikes" Logic (Delivery Attempts)
        if (newStatus == Status.DELIVERY_ATTEMPTED) {
            int attempts = shipment.getDeliveryAttempts() + 1;
            shipment.setDeliveryAttempts(attempts);

            log.info("Delivery attempt #{} for shipment {}", attempts, trackingNumber);

            // If 3rd failure, force status to RETURNED
            if (attempts >= 3) {
                newStatus = Status.RETURNED;
                log.warn("Shipment {} reached max delivery attempts. Marked as RETURNED.", trackingNumber);

                // Notify User about RTO
                emailService.sendEmail(shipment.getUser().getEmail(), "Delivery Failed",
                        "Your shipment " + trackingNumber + " could not be delivered after 3 attempts and is being returned.");
            }
        }

        // 5. Update & Save
        shipment.setStatus(newStatus);
        shipmentRepository.save(shipment);

        // 6. Log History
        TrackingUpdate historyLog = new TrackingUpdate();
        historyLog.setShipment(shipment);
        historyLog.setTrackingNumber(shipment.getTrackingNumber());
        historyLog.setStatus(newStatus);
        historyLog.setCreationTime(LocalDateTime.now());
        trackingRepository.save(historyLog);

        // 7. Notifications
        if (newStatus == Status.CANCELLED) {
            emailService.sendEmail(shipment.getUser().getEmail(), "Shipment Cancelled",
                    "Your shipment " + trackingNumber + " has been cancelled.");
        } else if (newStatus == Status.DELIVERED) {
            emailService.sendEmail(shipment.getUser().getEmail(), "Shipment Delivered",
                    "Your shipment " + trackingNumber + " has been successfully delivered!");
        } else if (newStatus == Status.RETURNED) {
            emailService.sendEmail(shipment.getUser().getEmail(), "Shipment Returned",
                    "Your shipment " + trackingNumber + " has been marked as returned.");
        }
    }

    // ✅ 4. Assign Driver (For Admin Use)
    @Transactional
    public void assignDriver(Long shipmentId, Long driverId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + shipmentId));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + driverId));

        shipment.setDriver(driver);

        // Auto-update status if it was just CREATED/PENDING
        if (shipment.getStatus() == Status.PENDING || shipment.getStatus() == Status.CREATED) {
            shipment.setStatus(Status.ASSIGNED);
        }

        shipmentRepository.save(shipment);
    }

    // ✅ 5. Helper
    private String generateTrackingId() {
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}