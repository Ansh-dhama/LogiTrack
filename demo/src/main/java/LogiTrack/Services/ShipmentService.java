package LogiTrack.Services;

import LogiTrack.Dto.ShipmentDto;
<<<<<<< HEAD
import LogiTrack.Dto.StatusDto;
=======
>>>>>>> c8ec02f (initial commit for LogiTrack)
import LogiTrack.Entity.Driver;
import LogiTrack.Entity.Shipment;
import LogiTrack.Entity.TrackingUpdate;
import LogiTrack.Entity.User;
<<<<<<< HEAD
=======
import LogiTrack.Enums.Role;
>>>>>>> c8ec02f (initial commit for LogiTrack)
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

<<<<<<< HEAD
=======
    // ✅ 1. Inject the Validator
    private final StatusTransitionValidator validator;

>>>>>>> c8ec02f (initial commit for LogiTrack)
    @Transactional(readOnly = true)
    public List<Shipment> getShipmentsByUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
<<<<<<< HEAD

        List<Shipment> shipments = user.getShipments();
        shipments.size();
        return shipments;
=======
        return user.getShipments();
>>>>>>> c8ec02f (initial commit for LogiTrack)
    }

    @Transactional
    public Shipment createShipment(ShipmentDto dto, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + email));

        Shipment shipment = new Shipment();
<<<<<<< HEAD

        shipment.setSenderAddress(dto.getSenderAddress());
        shipment.setReceiverAddress(dto.getReceiverAddress());
        shipment.setWeight(dto.getWeight());

        shipment.setTrackingNumber(generateTrackingId());
        shipment.setStatus(Status.PENDING);
        shipment.setUser(user);
        if (dto.getDriverId() != null) {
            Driver driver = driverRepository.findById(dto.getDriverId())
                    .orElse(null); // or handle error
=======
        shipment.setSenderAddress(dto.getSenderAddress());
        shipment.setReceiverAddress(dto.getReceiverAddress());
        shipment.setWeight(dto.getWeight());
        shipment.setTrackingNumber(generateTrackingId());
        shipment.setStatus(Status.PENDING);
        shipment.setUser(user);

        // Initial Delivery Attempts is 0
        shipment.setDeliveryAttempts(0);

        if (dto.getDriverId() != null) {
            Driver driver = driverRepository.findById(dto.getDriverId()).orElse(null);
>>>>>>> c8ec02f (initial commit for LogiTrack)
            shipment.setDriver(driver);
        }

        return shipmentRepository.save(shipment);
    }

<<<<<<< HEAD
    @Transactional
    public void updateStatus(StatusDto statusDto) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(statusDto.getTrackingNumber())
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found" + statusDto.getTrackingNumber()));

        Status currentStatus = shipment.getStatus();
        Status newStatus = statusDto.getStatus();

        // 1. Return Logic
        if (newStatus == Status.RETURNED) {
            if (currentStatus != Status.DELIVERED) {
                throw new InvalidStatusTransitionException( "Can only return items that have been delivered.");
            }
            shipment.setStatus(Status.RETURNED);
            emailService.sendEmail(shipment.getUser().getEmail(), "Shipment Returned",
                    "Your shipment " + shipment.getTrackingNumber() + " has been marked as returned.");
        }
        // 2. Cancellation Logic
        else if (newStatus == Status.CANCELLED) {
            if (currentStatus == Status.DELIVERED || currentStatus == Status.RETURNED) {
                throw new InvalidStatusTransitionException("Cannot cancel a shipment that is already delivered or returned.");
            }
            shipment.setStatus(Status.CANCELLED);
            emailService.sendEmail(shipment.getUser().getEmail(), "Shipment Cancelled",
                    "Your shipment " + shipment.getTrackingNumber() + " has been cancelled.");
        }
        // 3. Normal Status Update
        else {
            shipment.setStatus(newStatus);
        }

        // FIX 3: Centralized save (prevents bugs where some statuses aren't saved)
        shipmentRepository.save(shipment);

        // Log History
=======
    /**
     * ✅ UPDATED: Now requires User ID and Role for security checks.
     * Controller must extract these from the JWT/Session.
     */
    @Transactional
    public void updateStatus(String trackingNumber, Status newStatus, Long currentUserId, Role currentUserRole) {

        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + trackingNumber));

        Status currentStatus = shipment.getStatus();

        // 1. Idempotency Check (Optimization)
        // If status is already what we want, do nothing.
        if (currentStatus == newStatus) {
            return;
        }

        // 2. Validate Transition (The Roadmap)
        // This throws Exception if the path or role is invalid
        validator.validateTransition(currentStatus, newStatus, currentUserRole);

        // 3. Driver Ownership Check (Security)
        // Prevent Driver A from updating Driver B's shipment
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

        // 5. Reset attempts on success? (Optional, but usually we keep the history)
        if (newStatus == Status.DELIVERED) {
            // attempts remain as record of how hard it was
        }

        // 6. Update & Save
        shipment.setStatus(newStatus);
        shipmentRepository.save(shipment);

        // 7. Log History
>>>>>>> c8ec02f (initial commit for LogiTrack)
        TrackingUpdate historyLog = new TrackingUpdate();
        historyLog.setShipment(shipment);
        historyLog.setTrackingNumber(shipment.getTrackingNumber());
        historyLog.setStatus(newStatus);
        historyLog.setCreationTime(LocalDateTime.now());
<<<<<<< HEAD

        trackingRepository.save(historyLog);
=======
        trackingRepository.save(historyLog);

        // 8. Notifications (Kept from your original code)
        if (newStatus == Status.CANCELLED) {
            emailService.sendEmail(shipment.getUser().getEmail(), "Shipment Cancelled",
                    "Your shipment " + trackingNumber + " has been cancelled.");
        }
        else if (newStatus == Status.DELIVERED) {
            emailService.sendEmail(shipment.getUser().getEmail(), "Shipment Delivered",
                    "Your shipment " + trackingNumber + " has been successfully delivered!");
        }
>>>>>>> c8ec02f (initial commit for LogiTrack)
    }

    @Transactional
    public void assignDriver(Long shipmentId, Long driverId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
<<<<<<< HEAD
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found" + shipmentId));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found" +  driverId));

        shipment.setDriver(driver);
=======
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + shipmentId));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + driverId));

        shipment.setDriver(driver);

        // Auto-update status if it was CREATED
        if (shipment.getStatus() == Status.CREATED) {
            shipment.setStatus(Status.ASSIGNED);
        }

>>>>>>> c8ec02f (initial commit for LogiTrack)
        shipmentRepository.save(shipment);
    }

    private String generateTrackingId() {
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}